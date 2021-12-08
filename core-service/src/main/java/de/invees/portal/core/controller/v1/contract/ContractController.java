package de.invees.portal.core.controller.v1.contract;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ContractDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.v1.contract.ContractStatusV1;
import de.invees.portal.common.model.v1.contract.ContractTypeV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.contract.PrototypeContractV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.invoice.InvoiceUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.utils.CoreTokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.List;

import static spark.Spark.get;
import static spark.Spark.post;
import static spark.Spark.delete;

public class ContractController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public ContractController() {
    get("/v1/contract/:contract/", this::getContract);
    get("/v1/contract/", this::list);
    post("/v1/contract/preview/", this::previewOrder);
    post("/v1/contract/", this::createContract);
    post("/v1/contract/:contract/cancel/", this::cancel);
  }

  private Object getContract(Request req, Response res) {
    ContractV1 contract = contract(contractDataSource(), req);
    if (!isSameUser(req, contract.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(contract);
  }

  public Object list(Request req, Response resp) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        list(
            contractDataSource(),
            req,
            PrototypeContractV1.class,
            Filters.eq(ContractV1.BELONGS_TO, user.getId().toString()),
            Sorts.descending(ContractV1.CREATED_AT)
        )
    );
  }

  public Object previewOrder(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    List<OrderV1> orders = new ArrayList<>();
    for (JsonElement ele : body.get("orders").getAsJsonArray()) {
      orders.add(GsonUtils.GSON.fromJson(ele, OrderV1.class));
    }
    return GsonUtils.GSON.toJson(InvoiceUtils.calculate(-1, null, orders));
  }

  private Object cancel(Request req, Response resp) {
    ContractV1 contract = contract(contractDataSource(), req);
    if (!isSameUser(req, contract.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    contract.setInCancellation(true);
    contractDataSource().update(contract);
    return GsonUtils.toJson(contract);
  }

  public Object createContract(Request req, Response resp) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    List<OrderV1> orders = new ArrayList<>();
    for (JsonElement ele : body.get("orders").getAsJsonArray()) {
      orders.add(GsonUtils.GSON.fromJson(ele, OrderV1.class));
    }

    InvoiceV1 invoice = InvoiceUtils.create(user.getId(), orders);

    for (OrderV1 order : orders) {
      ContractV1 contract = new ContractV1(
          contractDataSource().nextSequence(),
          user.getId(),
          ContractTypeV1.DEFAULT,
          System.currentTimeMillis(),
          order,
          ContractStatusV1.PAYMENT_REQUIRED,
          -1,
          false
      );
      invoice.getContractList().add(contract.getId());
      contractDataSource().create(contract);
    }
    invoiceDataSource().create(invoice);
    return GsonUtils.toJson(invoice);
  }

  private ContractDataSourceV1 contractDataSource() {
    return this.connection.get().access(ContractDataSourceV1.class);
  }

  private static InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class)
        .access(InvoiceDataSourceV1.class);
  }
}
