package de.invees.portal.core.controller.v1.order;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceFileDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.OrderDataSourceV1;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.invoice.InvoiceFileV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.order.OrderStatusV1;
import de.invees.portal.common.model.v1.order.request.OrderRequestV1;
import de.invees.portal.common.model.v1.user.UserV1;
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
    get("/v1/order/:order/", this::getOrder);
    post("/v1/order/preview/", this::previewOrder);
    post("/v1/order/", this::placeOrder);
  }

  private Object getOrder(Request req, Response res) {
    OrderV1 order = order(orderDataSource(), req);
    if (!isSameUser(req, order.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(order);
  }

  public Object previewOrder(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    List<OrderRequestV1> orderRequests = new ArrayList<>();
    for (JsonElement ele : body.get("orderRequests").getAsJsonArray()) {
      orderRequests.add(GsonUtils.GSON.fromJson(ele, OrderRequestV1.class));
    }
    return GsonUtils.GSON.toJson(InvoiceUtils.calculate(-1, null, orderRequests));
  }

  public Object placeOrder(Request req, Response resp) {
    UserV1 user = TokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    List<OrderRequestV1> orderRequests = new ArrayList<>();
    for (JsonElement ele : body.get("orderRequests").getAsJsonArray()) {
      orderRequests.add(GsonUtils.GSON.fromJson(ele, OrderRequestV1.class));
    }

    InvoiceV1 invoice = InvoiceUtils.calculate(invoiceDataSource().nextSequence(), user.getId(), orderRequests);
    byte[] data = InvoiceUtils.createInvoiceFile(invoice);

    invoiceFileDataSource().create(new InvoiceFileV1(
        invoice.getId(),
        data
    ));

    for (OrderRequestV1 orderRequest : orderRequests) {
      OrderV1 order = new OrderV1(
          UUID.randomUUID(),
          user.getId(),
          invoice.getId(),
          System.currentTimeMillis(),
          orderRequest,
          OrderStatusV1.PAYMENT_REQUIRED,
          null
      );
      orderDataSource().create(order);
    }

    invoiceDataSource().create(invoice);

    return GsonUtils.toJson(invoice);
  }

  private OrderDataSourceV1 orderDataSource() {
    return this.connection.get().access(OrderDataSourceV1.class);
  }

  private InvoiceDataSourceV1 invoiceDataSource() {
    return this.connection.get().access(InvoiceDataSourceV1.class);
  }

  private InvoiceFileDataSourceV1 invoiceFileDataSource() {
    return this.connection.get().access(InvoiceFileDataSourceV1.class);
  }

}
