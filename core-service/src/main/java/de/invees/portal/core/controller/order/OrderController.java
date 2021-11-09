package de.invees.portal.core.controller.order;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.InvoiceDataSource;
import de.invees.portal.common.datasource.mongodb.InvoiceFileDataSource;
import de.invees.portal.common.datasource.mongodb.OrderDataSource;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.invoice.InvoiceFile;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.model.order.OrderStatus;
import de.invees.portal.common.model.order.request.OrderRequest;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.invoice.InvoiceUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.core.utils.TokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class OrderController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public OrderController() {
    get("/order/:order/", this::byId);
    post("/order/preview/", this::previewOrder);
    post("/order/", this::placeOrder);
  }

  private Object byId(Request req, Response res) {
    Order order = order(orderDataSource(), req);
    if (!isSameUser(req, order.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(order);
  }

  public Object previewOrder(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    List<OrderRequest> orderRequests = new ArrayList<>();
    for (JsonElement ele : body.get("orderRequests").getAsJsonArray()) {
      orderRequests.add(GsonUtils.GSON.fromJson(ele, OrderRequest.class));
    }
    return GsonUtils.GSON.toJson(InvoiceUtils.calculate(-1, null, orderRequests));
  }

  public Object placeOrder(Request req, Response resp) {
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
          OrderStatus.PAYMENT_REQUIRED,
          null
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
