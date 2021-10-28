package de.invees.portal.core.controller.order;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.datasource.mongodb.InvoiceDataSource;
import de.invees.portal.common.datasource.mongodb.InvoiceFileDataSource;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.invoice.InvoiceFile;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.order.OrderStatus;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.utils.service.LazyLoad;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.order.request.OrderRequest;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.invoice.InvoiceUtils;
import de.invees.portal.core.utils.TokenUtils;
import de.invees.portal.common.datasource.MongoService;
import de.invees.portal.common.datasource.mongodb.OrderDataSource;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static spark.Spark.post;

public class OrderController {

  private final LazyLoad<MongoService> connection = new LazyLoad<>(MongoService.class);

  public OrderController() {
    post("/order/preview/", this::preview);
    post("/order/", this::order);
  }

  public Object preview(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    List<OrderRequest> orderRequests = new ArrayList<>();
    for (JsonElement ele : body.get("orderRequests").getAsJsonArray()) {
      orderRequests.add(GsonUtils.GSON.fromJson(ele, OrderRequest.class));
    }
    return GsonUtils.GSON.toJson(InvoiceUtils.calculate(-1, null, orderRequests));
  }

  public Object order(Request req, Response resp) {
    User user = TokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    List<OrderRequest> orderRequests = new ArrayList<>();
    for (JsonElement ele : body.get("orderRequests").getAsJsonArray()) {
      orderRequests.add(GsonUtils.GSON.fromJson(ele, OrderRequest.class));
    }

    Invoice invoice = InvoiceUtils.calculate(invoiceDataSource().nextSequence(), user.getId(), orderRequests);
    byte[] data = InvoiceUtils.createInvoiceFile(invoice);

    invoiceFileDataSource().create(new InvoiceFile(
        invoice.getId(),
        data
    ));

    for (OrderRequest orderRequest : orderRequests) {
      Order order = new Order(
          UUID.randomUUID(),
          user.getId(),
          invoice.getId(),
          System.currentTimeMillis(),
          orderRequest,
          OrderStatus.PAYMENT_REQUIRED
      );
      orderDataSource().create(order);
    }

    invoiceDataSource().create(invoice);

    return GsonUtils.toJson(invoice);
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

}
