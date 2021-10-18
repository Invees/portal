package de.invees.portal.core.controller.invoice;

import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.ConnectionService;
import de.invees.portal.common.datasource.mongodb.InvoiceDataSource;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.model.user.permission.PermissionType;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.service.LazyLoad;
import de.invees.portal.core.utils.TokenUtils;
import de.invees.portal.core.utils.controller.ControllerUtils;
import spark.Request;
import spark.Response;

import java.util.UUID;

import static spark.Spark.get;

public class InvoiceController {

  private final LazyLoad<ConnectionService> connection = new LazyLoad<>(ConnectionService.class);

  public InvoiceController() {
    get("/invoice/", this::list);
  }

  public Object list(Request req, Response resp) {
    if (req.queryParams("userId") != null) {
      return listForUser(req, resp);
    } else {
      return listAll(req, resp);
    }
  }

  private Object listForUser(Request req, Response resp) {
    User user = TokenUtils.parseToken(req);
    if(user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    UUID userId = UUID.fromString(req.queryParams("userId"));
    if (!user.isPermitted(PermissionType.VIEW_USER_INVOICES, userId.toString()) && !userId.equals(user.getId())) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        ControllerUtils.list(invoiceDataSource(), req, Invoice.class, Filters.eq(Invoice.USER_ID, userId.toString()))
    );
  }

  private Object listAll(Request req, Response resp) {
    User user = TokenUtils.parseToken(req);
    if(user == null) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    if (!user.isPermitted(PermissionType.VIEW_USER_INVOICES, "*")) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        ControllerUtils.list(invoiceDataSource(), req, Invoice.class)
    );
  }

  private InvoiceDataSource invoiceDataSource() {
    return this.connection.get().access(InvoiceDataSource.class);
  }

}
