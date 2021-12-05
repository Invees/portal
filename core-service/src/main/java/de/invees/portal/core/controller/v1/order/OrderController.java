package de.invees.portal.core.controller.v1.order;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.OrderDataSourceV1;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.order.OrderStatusV1;
import de.invees.portal.common.model.v1.order.OrderTypeV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.order.PrototypeOrderV1;
import de.invees.portal.common.model.v1.order.request.OrderRequestV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.invoice.InvoiceUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.core.utils.CoreTokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;

public class OrderController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public OrderController() {
    get("/v1/order/:order/", this::getOrder);
    get("/v1/order/", this::list);
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

  public Object list(Request req, Response resp) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        list(
            orderDataSource(),
            req,
            PrototypeOrderV1.class,
            Filters.eq(OrderV1.BELONGS_TO, user.getId().toString()),
            Sorts.descending(OrderV1.ORDER_TIME)
        )
    );
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
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    List<OrderRequestV1> orderRequests = new ArrayList<>();
    for (JsonElement ele : body.get("orderRequests").getAsJsonArray()) {
      orderRequests.add(GsonUtils.GSON.fromJson(ele, OrderRequestV1.class));
    }

    InvoiceV1 invoice = InvoiceUtils.create(user.getId(), orderRequests);

    for (OrderRequestV1 orderRequest : orderRequests) {
      OrderV1 order = new OrderV1(
          orderDataSource().nextSequence(),
          user.getId(),
          OrderTypeV1.ORDER,
          System.currentTimeMillis(),
          orderRequest,
          OrderStatusV1.PAYMENT_REQUIRED,
          -1
      );
      orderDataSource().create(order);
    }

    return GsonUtils.toJson(invoice);
  }

  private OrderDataSourceV1 orderDataSource() {
    return this.connection.get().access(OrderDataSourceV1.class);
  }

}
