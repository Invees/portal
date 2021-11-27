package de.invees.portal.core.controller.v1.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.ServiceDataSource;
import de.invees.portal.common.datasource.mongodb.UserDataSource;
import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.v1.service.Service;
import de.invees.portal.common.model.v1.service.command.Command;
import de.invees.portal.common.model.v1.user.User;
import de.invees.portal.common.model.v1.user.UserNameDetails;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.status.ExecuteCommandMessage;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.service.ServiceProvider;
import de.invees.portal.core.utils.TokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

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
    get("/v1/service/:service/console/", this::getStatus);
    get("/v1/service/", this::list);
  }

  private Object execute(Request req, Response resp) {
    Service service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    User user = TokenUtils.parseToken(req);
    body.addProperty("_id", UUID.randomUUID().toString());
    body.addProperty("executor", user.getId().toString());
    body.addProperty("service", service.getId().toString());

    ProviderRegistry.access(NatsProvider.class).send(
        Subject.STATUS,
        new ExecuteCommandMessage(GsonUtils.GSON.fromJson(body, Command.class))
    );
    return body.toString();
  }

  private Object createConsole(Request req, Response resp) {
    Service service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    System.out.println("DO!");
    return GsonUtils.GSON.toJson(
        serviceProvider().createConsole(service.getId())
    );
  }

  private Object getOwner(Request req, Response resp) {
    Service service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    UserNameDetails details = userDataSource().byId(service.getBelongsTo(), UserNameDetails.class);
    return GsonUtils.GSON.toJson(
        details
    );
  }

  private Object getStatus(Request req, Response resp) {
    Service service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        serviceProvider().getStatus(service.getId())
    );
  }

  private Object getService(Request req, Response resp) {
    Service service = service(serviceDataSource(), req);
    if (!isSameUser(req, service.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        service
    );
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

  private UserDataSource userDataSource() {
    return this.connection.get().access(UserDataSource.class);
  }

  private ServiceProvider serviceProvider() {
    return ProviderRegistry.access(ServiceProvider.class);
  }
}
