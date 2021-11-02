package de.invees.portal.core.controller.invoice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.GatewayDataDataSource;
import de.invees.portal.common.datasource.mongodb.InvoiceDataSource;
import de.invees.portal.common.datasource.mongodb.InvoiceFileDataSource;
import de.invees.portal.common.datasource.mongodb.OrderDataSource;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.gateway.paypal.PayPalGatewayProvider;
import de.invees.portal.common.model.gateway.GatewayData;
import de.invees.portal.common.model.gateway.GatewayDataType;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.invoice.InvoiceFile;
import de.invees.portal.common.model.invoice.InvoiceStatus;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.order.OrderStatus;
import de.invees.portal.common.model.user.permission.PermissionType;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.payment.PaymentMessage;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.Application;
import de.invees.portal.core.utils.TokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class InvoiceController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public InvoiceController() {
    get("/invoice/", this::list);
    get("/invoice/:invoiceId/", this::getInvoice);
    get("/invoice/:invoiceId/file/", this::getFile);
    get("/invoice/:invoiceId/paypal/", this::getPaymentData);
    post("/invoice/:invoiceId/paypal/callback/", this::capturePaymentData);
  }

  private Object capturePaymentData(Request req, Response resp) throws IOException {
    Invoice invoice = invoice(invoiceDataSource(), req);
    if (!isSameUser(req, invoice.getUserId())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    JsonObject object = JsonParser.parseString(req.body()).getAsJsonObject();
    com.paypal.orders.Order orderResponse = payPalGateway().validate(object.get("orderId").getAsString()).result();

    if (!orderResponse.purchaseUnits().get(0).referenceId().equalsIgnoreCase(invoice.getId() + "")) {
      Application.LOGGER.error("------------------------------------");
      Application.LOGGER.error("Invalid invoice for payment. Did the user tryed to d some illegal stuff?");
      Application.LOGGER.error("orderResponse = " + GsonUtils.toJson(orderResponse));
      Application.LOGGER.error("user = " + GsonUtils.toJson(TokenUtils.parseToken(req)));
      Application.LOGGER.error("invoice = " + GsonUtils.toJson(invoice));
      Application.LOGGER.error("------------------------------------");
      throw new UnauthorizedException("INVALID_PAYMENT_INVOICE");
    }
    if (orderResponse.status().equalsIgnoreCase("COMPLETED")) {
      invoice.setStatus(InvoiceStatus.PAID);
      invoiceDataSource().update(invoice);
      gatewayDataSource().create(new GatewayData(
          UUID.randomUUID(),
          GatewayDataType.PAYPAL,
          orderResponse
      ));
      List<Order> orders = orderDataSource().byInvoiceId(invoice.getId());

      for (Order order : orders) {
        order.setStatus(OrderStatus.PROCESSING);
        orderDataSource().update(order);
      }
      // NOW WE SEND A NATS MESSAGE TO CREATE THE SERVICES FROM ORDERS
      ProviderRegistry.access(NatsProvider.class).send(Subject.PAYMENT, new PaymentMessage(invoice.getId()));
    }
    return GsonUtils.toJson(orderResponse);
  }

  private Object getPaymentData(Request req, Response resp) throws IOException {
    Invoice invoice = invoice(invoiceDataSource(), req);
    if (!isSameUser(req, invoice.getUserId())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        payPalGateway().createOrder(invoice).result().id()
    );
  }

  private Object getInvoice(Request req, Response resp) throws IOException {
    Invoice invoice = invoice(invoiceDataSource(), req);
    if (!isPermitted(req, PermissionType.VIEW_INVOICE, invoice.getId() + "", invoice.getUserId().toString())
        && !isSameUser(req, invoice.getUserId())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        invoiceDataSource().byId(Integer.valueOf(req.params("invoiceId")), Invoice.class)
    );
  }

  private Object getFile(Request req, Response resp) throws IOException {
    Invoice invoice = invoice(invoiceDataSource(), req);
    if (!isPermitted(req, PermissionType.VIEW_INVOICE, invoice.getId() + "", invoice.getUserId().toString())
        && !isSameUser(req, invoice.getUserId())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    byte[] data = invoiceFileDataSource().byId(invoice.getId(), InvoiceFile.class).getData();

    resp.header("Content-Disposition", String.format("attachment; filename=\"%s.pdf\"", "invoice-" + invoice.getId()));
    resp.type("OCTET_STREAM");
    resp.raw().setContentLength(data.length);
    resp.status(200);

    resp.raw().getOutputStream().write(data);
    resp.raw().getOutputStream().close();
    return null;
  }

  public Object list(Request req, Response resp) {
    if (req.queryParams("userId") != null) {
      return listForUser(req, resp);
    } else {
      return listAll(req, resp);
    }
  }

  private Object listForUser(Request req, Response resp) {
    UUID userId = UUID.fromString(req.queryParams("userId"));
    if (!isPermitted(req, PermissionType.VIEW_INVOICE, userId.toString()) && !isSameUser(req, userId)) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        list(
            invoiceDataSource(),
            req,
            Invoice.class,
            Filters.eq(Invoice.USER_ID, userId.toString()),
            Sorts.descending(Invoice.DATE)
        )
    );
  }

  private Object listAll(Request req, Response resp) {
    if (isPermitted(req, PermissionType.VIEW_INVOICE, "*")) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        list(invoiceDataSource(), req, Invoice.class)
    );
  }

  private OrderDataSource orderDataSource() {
    return this.connection.get().access(OrderDataSource.class);
  }

  private InvoiceDataSource invoiceDataSource() {
    return this.connection.get().access(InvoiceDataSource.class);
  }

  private InvoiceFileDataSource invoiceFileDataSource() {
    return this.connection.get().access(InvoiceFileDataSource.class);
  }

  private GatewayDataDataSource gatewayDataSource() {
    return this.connection.get().access(GatewayDataDataSource.class);
  }

  private PayPalGatewayProvider payPalGateway() {
    return ProviderRegistry.access(PayPalGatewayProvider.class);
  }

}
