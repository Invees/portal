package de.invees.portal.core.utils.controller;

import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.datasource.mongodb.v1.ContractDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.InvoiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.ServiceDataSourceV1;
import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.contract.ContractV1;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import de.invees.portal.common.model.v1.service.DisplayServiceV1;
import de.invees.portal.common.model.v1.service.ServiceV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.InputUtils;
import de.invees.portal.core.utils.CoreTokenUtils;
import org.bson.conversions.Bson;
import spark.Request;

import java.util.UUID;

public class Controller {

  public <Y extends Model> Object list(DataSource dataSource, Request req, Class<Y> type) {
    return list(dataSource, req, null);
  }

  public <Y extends Model> Object list(DataSource dataSource, Request req, Class<Y> type, Bson filter) {
    return list(dataSource, req, type, filter, null);
  }

  public <Y extends Model> Object list(DataSource dataSource, Request req, Class<Y> type, Bson filter, Bson sort) {
    if (hasLimitOrSkip(req)) {
      int skip = InputUtils.integerByString(req.queryParams("skip"), 0);
      int limit = InputUtils.integerByString(req.queryParams("limit"), 10);
      return dataSource.listPaged(skip, limit, type, filter, sort);
    }
    return dataSource.list(type, filter);
  }

  public boolean hasLimitOrSkip(Request req) {
    return !InputUtils.isEmpty(req.queryParams("skip")) || !InputUtils.isEmpty(req.queryParams("limit"));
  }

  public boolean isSameUser(Request req, UUID userId) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      return false;
    }
    return userId.equals(user.getId());
  }

  // Contract
  public ContractV1 contract(ContractDataSourceV1 dataSource, Request req) {
    ContractV1 contract = dataSource.byId(
        Integer.valueOf(req.params("contract")), ContractV1.class
    );
    if (contract == null) {
      throw new InputException("CONTRACT_NOT_FOUND");
    }
    return contract;
  }

  // Invoice
  public InvoiceV1 invoice(InvoiceDataSourceV1 dataSource, Request req) {
    InvoiceV1 invoice = dataSource.byId(
        InputUtils.integerByString(req.params("invoice"), -1), InvoiceV1.class
    );
    if (invoice == null) {
      throw new InputException("INVOICE_NOT_FOUND");
    }
    return invoice;
  }

  // Service
  public DisplayServiceV1 displayService(ServiceDataSourceV1 dataSource, Request req) {
    DisplayServiceV1 service = dataSource.byId(
        UUID.fromString(req.params("service")), DisplayServiceV1.class
    );
    if (service == null) {
      throw new InputException("SERVICE__NOT_FOUND");
    }
    return service;
  }

  public ServiceV1 service(ServiceDataSourceV1 dataSource, Request req) {
    ServiceV1 service = dataSource.byId(
        UUID.fromString(req.params("service")), ServiceV1.class
    );
    if (service == null) {
      throw new InputException("SERVICE__NOT_FOUND");
    }
    return service;
  }

}
