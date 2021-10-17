package de.invees.portal.core.controller.invoice;

import de.invees.portal.common.datasource.ConnectionService;
import de.invees.portal.common.datasource.mongodb.InvoiceDataSource;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.model.user.permission.PermissionType;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.service.LazyLoad;
import de.invees.portal.core.utils.TokenUtils;
import spark.Request;
import spark.Response;

import java.util.UUID;

import static spark.Spark.get;

public class InvoiceController {

  private final LazyLoad<ConnectionService> connection = new LazyLoad<>(ConnectionService.class);

  public InvoiceController() {
    get("/invoice/", this::getInvoices);
  }

  public Object getInvoices(Request req, Response resp) {
    if (req.queryParams("userId") != null) {
      System.out.println(req.queryParams("userId"));
      return getInvoicesForUser(req, resp);
    } else {
      return getAllInvoices(req, resp);
    }
  }

  private Object getInvoicesForUser(Request req, Response resp) {
    User user = TokenUtils.parseToken(req);
    if(user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    UUID userId = UUID.fromString(req.queryParams("userId"));
    if (!user.isPermitted(PermissionType.VIEW_USER_INVOICES, userId.toString()) && !userId.equals(user.getId())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(invoiceDataSource().listForUser(userId, Invoice.class));
  }

  private Object getAllInvoices(Request req, Response resp) {
    User user = TokenUtils.parseToken(req);
    if(user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    if (!user.isPermitted(PermissionType.VIEW_USER_INVOICES, "*")) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(invoiceDataSource().list(Invoice.class));
  }

  private InvoiceDataSource invoiceDataSource() {
    return this.connection.get().access(InvoiceDataSource.class);
  }

}
