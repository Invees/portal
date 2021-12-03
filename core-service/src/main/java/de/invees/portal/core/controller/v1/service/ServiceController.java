package de.invees.portal.core.controller.v1.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ServiceDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.UserDataSourceV1;
import de.invees.portal.common.exception.LockedServiceException;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.v1.service.ServiceV1;
import de.invees.portal.common.model.v1.service.command.CommandResponseV1;
import de.invees.portal.common.model.v1.service.command.CommandV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusTypeV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;
import de.invees.portal.common.model.v1.user.UserNameDetailsV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.service.ServiceProvider;
import de.invees.portal.core.utils.CoreTokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class ServiceController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public ServiceController() {
    get("/v1/service/:service/", this::getService);
    get("/v1/service/:service/owner/", this::getOwner);
    post("/v1/service/:service/execute/", this::execute);
    post("/v1/service/:service/console/", this::createConsole);
    get("/v1/service/:service/status/", this::getStatus);
    get("/v1/service/", this::list);
  }

  private Object execute(Request req, Response resp) {
    ServiceV1 service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    UserV1 user = CoreTokenUtils.parseToken(req);
    body.addProperty("_id", UUID.randomUUID().toString());
    body.addProperty("executor", user.getId().toString());
    body.addProperty("service", service.getId().toString());

    CommandResponseV1 response = serviceProvider().sendCommand(GsonUtils.GSON.fromJson(body, CommandV1.class));
    if (!response.isSuccess()) {
      throw new RuntimeException(response.getMessage());
    }
    JsonObject json = new JsonObject();
    if (response.getMessage() != null) {
      json.addProperty("message", response.getMessage());
    }
    return json.toString();
  }

  private Object createConsole(Request req, Response resp) {
    ServiceV1 service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    ServiceStatusV1 status = serviceProvider().getStatus(service.getId());
    if (status == null) {
      throw new LockedServiceException("UNAVAILABLE_HOST");
    }
    if (status.getStatus() == ServiceStatusTypeV1.INSTALLING) {
      throw new LockedServiceException("INSTALLING");
    }
    if (status.getStatus() == ServiceStatusTypeV1.LOCKED) {
      throw new LockedServiceException("INSTALLING");
    }
    if (status.getStatus() == ServiceStatusTypeV1.STOPPED) {
      throw new LockedServiceException("STOPPED");
    }
    return GsonUtils.GSON.toJson(
        serviceProvider().createConsole(service.getId())
    );
  }

  private Object getOwner(Request req, Response resp) {
    ServiceV1 service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    UserNameDetailsV1 details = userDataSource().byId(service.getBelongsTo(), UserNameDetailsV1.class);
    return GsonUtils.GSON.toJson(
        details
    );
  }

  private Object getStatus(Request req, Response resp) {
    ServiceV1 service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    ServiceStatusV1 status = serviceProvider().getStatus(service.getId());
    if (status == null) {
      return GsonUtils.GSON.toJson(new ServiceStatusV1(
          service.getId(),
          new HashMap<>(),
          ServiceStatusTypeV1.UNAVAILABLE_HOST,
          new ArrayList<>(),
          0
      ));
    }
    return GsonUtils.GSON.toJson(
        serviceProvider().getStatus(service.getId())
    );
  }

  private Object getService(Request req, Response resp) {
    ServiceV1 service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        service
    );
  }

  public Object list(Request req, Response resp) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        list(
            serviceDataSource(),
            req,
            ServiceV1.class,
            Filters.eq(ServiceV1.BELONGS_TO, user.getId().toString())
        )
    );
  }

  private ServiceDataSourceV1 serviceDataSource() {
    return this.connection.get().access(ServiceDataSourceV1.class);
  }

  private UserDataSourceV1 userDataSource() {
    return this.connection.get().access(UserDataSourceV1.class);
  }

  private ServiceProvider serviceProvider() {
    return ProviderRegistry.access(ServiceProvider.class);
  }
}
