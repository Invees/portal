package de.invees.portal.core.controller.service;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.*;
import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.service.Service;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.UUID;

import static spark.Spark.get;

public class ServiceController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public ServiceController() {
    get("/service/", this::list);
  }

  public Object list(Request req, Response resp) {
    if (req.queryParams("belongsTo") != null) {
      return listForUser(req, resp);
    }
    throw new InputException("MISSING_ARGUMENT");
  }

  private Object listForUser(Request req, Response resp) {
    UUID user = UUID.fromString(req.queryParams("belongsTo"));
    if (!isSameUser(req, user)) {
      throw new UnauthorizedException("UNAUTHORIZED");
    }
    return GsonUtils.GSON.toJson(
        list(
            serviceDataSource(),
            req,
            Service.class,
            Filters.eq(Service.BELONGS_TO, user.toString())
        )
    );
  }

  private ServiceDataSource serviceDataSource() {
    return this.connection.get().access(ServiceDataSource.class);
  }

}
