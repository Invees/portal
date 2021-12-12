package de.invees.portal.core.controller.v1.contract;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ContractCancellationDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.ContractDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.v1.contract.ContractStatusV1;
import de.invees.portal.common.model.v1.contract.ContractTypeV1;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.contract.PrototypeContractV1;
import de.invees.portal.common.model.v1.contract.cancellation.ContractCancellationV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.order.ContractUpgradeV1;
import de.invees.portal.common.model.v1.order.OrderV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.UpgradeContractMessage;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.invoice.ContractUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.utils.CoreTokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class ContractController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public ContractController() {
    get("/v1/contract/:contract/", this::getContract);
    get("/v1/contract/", this::list);
    post("/v1/contract/preview/", this::previewOrder);
    post("/v1/contract/", this::createContract);
    post("/v1/contract/:contract/cancel/", this::cancel);
    post("/v1/contract/:contract/upgrade/", this::upgrade);
    get("/v1/contract/:contract/cancel/", this::getCancel);
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
    return GsonUtils.GSON.toJson(ContractUtils.calculate(-1, null, orders));
  }

  private Object upgrade(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    ContractV1 contract = contract(contractDataSource(), req);
    if (!isSameUser(req, contract.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    if (contract.getStatus() == ContractStatusV1.COMPLETED
        || contract.getStatus() == ContractStatusV1.CANCELED) {
      throw new InputException("CONTRACT_IS_COMPLETED");
    }
    List<ContractUpgradeV1> upgrades = new ArrayList<>();
    for (JsonElement ele : body.get("upgrades").getAsJsonArray()) {
      upgrades.add(GsonUtils.GSON.fromJson(ele, ContractUpgradeV1.class));
    }

    ContractUtils.applyUpgradeToContract(contract, upgrades);
    InvoiceV1 invoice = ContractUtils.createByUpgrades(contract, upgrades);
    invoiceDataSource().create(invoice);
    contractDataSource().update(contract);
    ProviderRegistry.access(NatsProvider.class)
        .send(Subject.PROCESSING, new UpgradeContractMessage(contract.getId(), upgrades));

    return GsonUtils.GSON.toJson(contract);
  }

  private Object cancel(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    ContractV1 contract = contract(contractDataSource(), req);
    if (!isSameUser(req, contract.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    if (contract.getStatus() == ContractStatusV1.COMPLETED
        || contract.getStatus() == ContractStatusV1.CANCELED) {
      throw new InputException("CONTRACT_IS_COMPLETED");
    }
    long nextPaymentDate = -1;
    if (body.get("cancel").getAsBoolean()) {
      System.out.println(new Date(ContractUtils.getNextPaymentDate(contract)));
      nextPaymentDate = ContractUtils.getNextPaymentDate(contract);
    }

    ContractCancellationV1 cancellation = new ContractCancellationV1(
        UUID.randomUUID(),
        contract.getId(),
        System.currentTimeMillis(),
        nextPaymentDate,
        body.get("cancel").getAsBoolean()
    );
    contractCancellationDataSource().create(cancellation);
    return GsonUtils.toJson(cancellation);
  }

  private Object getCancel(Request req, Response resp) {
    ContractV1 contract = contract(contractDataSource(), req);
    if (!isSameUser(req, contract.getBelongsTo())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.toJson(contractCancellationDataSource().getLastCancellation(contract.getId()));
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

    InvoiceV1 invoice = ContractUtils.create(user.getId(), orders);

    for (OrderV1 order : orders) {
      ContractV1 contract = new ContractV1(
          contractDataSource().nextSequence(),
          user.getId(),
          ContractTypeV1.DEFAULT,
          System.currentTimeMillis(),
          order,
          ContractStatusV1.PAYMENT_REQUIRED,
          -1
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

  private ContractCancellationDataSourceV1 contractCancellationDataSource() {
    return this.connection.get().access(ContractCancellationDataSourceV1.class);
  }

  private static InvoiceDataSourceV1 invoiceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class)
        .access(InvoiceDataSourceV1.class);
  }
}
