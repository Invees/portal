package de.invees.portal.core.controller.order;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.datasource.mongodb.InvoiceDataSource;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.order.OrderStatus;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.utils.service.LazyLoad;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.order.request.OrderRequest;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.core.utils.input.InvoiceUtils;
import de.invees.portal.core.utils.TokenUtils;
import de.invees.portal.common.datasource.ConnectionService;
import de.invees.portal.common.datasource.mongodb.OrderDataSource;
import spark.Request;
import spark.Response;

import java.util.UUID;

import static spark.Spark.post;

public class OrderController {

  private final LazyLoad<ConnectionService> connection = new LazyLoad<>(ConnectionService.class);

  public OrderController() {
    post("/order/preview/", this::preview);
    post("/order/", this::order);
  }

  public Object preview(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    OrderRequest orderRequest = GsonUtils.GSON.fromJson(body.get("orderRequest"), OrderRequest.class);
    return GsonUtils.GSON.toJson(InvoiceUtils.calculate(-1, null, null, orderRequest));
  }

  public Object order(Request req, Response resp) {
    User user = TokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    OrderRequest orderRequest = GsonUtils.GSON.fromJson(body.get("orderRequest"), OrderRequest.class);
    UUID orderId = UUID.randomUUID();
    Invoice invoice = InvoiceUtils.calculate(invoiceDataSource().nextSequence(), user.getId(), orderId, orderRequest);

    Order order = new Order(
        orderId,
        user.getId(),
        System.currentTimeMillis(),
        orderRequest,
        OrderStatus.PAYMENT_REQUIRED
    );

    orderDataSource().create(order);
    invoiceDataSource().create(invoice);

    return GsonUtils.GSON.toJson(orderRequest);
  }

  private OrderDataSource orderDataSource() {
    return this.connection.get().access(OrderDataSource.class);
  }

  private InvoiceDataSource invoiceDataSource() {
    return this.connection.get().access(InvoiceDataSource.class);
  }
}
