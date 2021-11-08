package de.invees.portal.core.utils.controller;

import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.datasource.mongodb.InvoiceDataSource;
import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.utils.InputUtils;
import de.invees.portal.core.utils.TokenUtils;
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

  public boolean isPermitted(Request req, String permissionType, String... contexts) {
    User user = TokenUtils.parseToken(req);
    if (user == null) {
      return false;
    }
    for (String context : contexts) {
      if (user.isPermitted(permissionType, context)) {
        return true;
      }
    }
    return false;
  }

  public boolean isSameUser(Request req, UUID userId) {
    User user = TokenUtils.parseToken(req);
    if (user == null) {
      return false;
    }
    if (!user.getId().equals(userId)) {
      return false;
    }
    return true;
  }

  // Invoice
  public Invoice invoice(InvoiceDataSource dataSource, Request req) {
    Invoice invoice = dataSource.byId(
        InputUtils.integerByString(req.params("invoice"), -1), Invoice.class
    );
    if (invoice == null) {
      throw new InputException("INVOICE_NOT_FOUND");
    }
    return invoice;
  }

}
