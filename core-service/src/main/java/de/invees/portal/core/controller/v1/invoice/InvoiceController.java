package de.invees.portal.core.controller.v1.invoice;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.GatewayDataDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceFileDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.ContractDataSourceV1;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.gateway.paypal.PayPalGatewayProvider;
import de.invees.portal.common.model.v1.gateway.GatewayDataTypeV1;
import de.invees.portal.common.model.v1.gateway.GatewayDataV1;
import de.invees.portal.common.model.v1.invoice.InvoiceFileV1;
import de.invees.portal.common.model.v1.invoice.InvoiceStatusV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.contract.ContractStatusV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.payment.PaymentMessage;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.Application;
import de.invees.portal.core.utils.CoreTokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class InvoiceController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public InvoiceController() {
    get("/v1/invoice/", this::list);
    get("/v1/invoice/:invoice/", this::getInvoice);
    get("/v1/invoice/:invoice/file/", this::getFile);
    get("/v1/invoice/:invoice/paypal/", this::getPaymentData);
    post("/v1/invoice/:invoice/paypal/callback/", this::capturePaymentData);
  }

  private Object capturePaymentData(Request req, Response resp) throws IOException {
    InvoiceV1 invoice = invoice(invoiceDataSource(), req);
    if (!isSameUser(req, invoice.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    JsonObject object = JsonParser.parseString(req.body()).getAsJsonObject();
    com.paypal.orders.Order orderResponse = payPalGateway().validate(object.get("orderID").getAsString()).result();

    if (!orderResponse.purchaseUnits().get(0).referenceId().equalsIgnoreCase(invoice.getId() + "")) {
      Application.LOGGER.error("------------------------------------");
      Application.LOGGER.error("Invalid invoice for payment. Did the user tryed to do some illegal stuff?");
      Application.LOGGER.error("orderResponse = " + GsonUtils.toJson(orderResponse));
      Application.LOGGER.error("user = " + GsonUtils.toJson(CoreTokenUtils.parseToken(req)));
      Application.LOGGER.error("invoice = " + GsonUtils.toJson(invoice));
      Application.LOGGER.error("IP = " + req.ip());
      Application.LOGGER.error("------------------------------------");
      throw new UnauthorizedException("INVALID_PAYMENT_INVOICE");
    }
    if (orderResponse.status().equalsIgnoreCase("COMPLETED")) {
      invoice.setStatus(InvoiceStatusV1.PAID);
      invoice.setPaidAt(System.currentTimeMillis());
      invoiceDataSource().update(invoice);

      gatewayDataSource().create(new GatewayDataV1(
          UUID.randomUUID(),
          GatewayDataTypeV1.PAYPAL,
          orderResponse
      ));
      List<ContractV1> contracts = new ArrayList<>();
      for (long contract : invoice.getContractList()) {
        contracts.add(contractDataSource().byId(contract, ContractV1.class));
      }

      for (ContractV1 contract : contracts) {
        if (contract.getStatus() != ContractStatusV1.PAYMENT_REQUIRED) {
          continue;
        }
        contract.setStatus(ContractStatusV1.PROCESSING);
        contractDataSource().update(contract);
      }

      ProviderRegistry.access(NatsProvider.class).send(Subject.PAYMENT, new PaymentMessage(invoice.getId()));
    }
    return GsonUtils.toJson(orderResponse);
  }

  private Object getPaymentData(Request req, Response resp) throws IOException {
    InvoiceV1 invoice = invoice(invoiceDataSource(), req);
    if (!isSameUser(req, invoice.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        payPalGateway().createOrder(invoice).result().id()
    );
  }

  private Object getInvoice(Request req, Response resp) {
    InvoiceV1 invoice = invoice(invoiceDataSource(), req);
    if (!isSameUser(req, invoice.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        invoice
    );
  }

  private Object getFile(Request req, Response resp) throws IOException {
    InvoiceV1 invoice = invoice(invoiceDataSource(), req);
    if (!isSameUser(req, invoice.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    byte[] data = invoiceFileDataSource().byId(invoice.getId(), InvoiceFileV1.class).getData();

    resp.header("Content-Disposition", String.format("attachment; filename=\"%s.pdf\"", "invoice-" + invoice.getId()));
    resp.type("OCTET_STREAM");
    resp.raw().setContentLength(data.length);
    resp.status(200);

    resp.raw().getOutputStream().write(data);
    resp.raw().getOutputStream().close();
    return null;
  }

  public Object list(Request req, Response resp) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        list(
            invoiceDataSource(),
            req,
            InvoiceV1.class,
            Filters.eq(InvoiceV1.BELONGS_TO, user.getId().toString()),
            Sorts.descending(InvoiceV1.CREATED_AT)
        )
    );
  }

  private ContractDataSourceV1 contractDataSource() {
    return this.connection.get().access(ContractDataSourceV1.class);
  }

  private InvoiceDataSourceV1 invoiceDataSource() {
    return this.connection.get().access(InvoiceDataSourceV1.class);
  }

  private InvoiceFileDataSourceV1 invoiceFileDataSource() {
    return this.connection.get().access(InvoiceFileDataSourceV1.class);
  }

  private GatewayDataDataSourceV1 gatewayDataSource() {
    return this.connection.get().access(GatewayDataDataSourceV1.class);
  }

  private PayPalGatewayProvider payPalGateway() {
    return ProviderRegistry.access(PayPalGatewayProvider.class);
  }

}
