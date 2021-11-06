package de.invees.portal.core.web;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.exception.*;
import de.invees.portal.common.utils.provider.Provider;
import de.invees.portal.core.Application;
import de.invees.portal.core.configuration.Configuration;
import de.invees.portal.core.controller.invoice.InvoiceController;
import de.invees.portal.core.controller.order.OrderController;
import de.invees.portal.core.controller.product.ProductController;
import de.invees.portal.core.controller.section.SectionController;
import de.invees.portal.core.controller.user.UserController;

import java.util.List;

import static spark.Spark.*;

public class HttpServerProvider implements Provider {

  public static final List<Class<?>> CONTROLLER = List.of(
      SectionController.class,
      OrderController.class,
      UserController.class,
      InvoiceController.class,
      ProductController.class
  );

  public HttpServerProvider(Configuration configuration) {
    Application.LOGGER.info("Starting HttpServer on [:]:" + configuration.getPort());
    port(configuration.getPort());
    before((request, response) -> response.type("application/json"));
    before((request, response) -> response.header(
        "Access-Control-Allow-Origin",
        configuration.getAccessControlAllowOrigin()
    ));
    before((request, response) -> response.header(
        "Access-Control-Allow-Headers",
        configuration.getAccessControlAllowHeaders()
    ));

    notFound((request, response) -> {
      JsonObject body = new JsonObject();
      body.addProperty("result", "error");
      body.addProperty("message", "Endpoint not found.");
      return body;
    });
    after((request, response) -> {
      JsonObject body = new JsonObject();
      body.addProperty("result", "success");
      body.addProperty("responseTime", System.currentTimeMillis());
      if (response.body() == null) {
        body.addProperty("result", "error");
        body.addProperty("message", "Endpoint not found.");
      } else {
        body.add("data", JsonParser.parseString(response.body()));
      }
      response.body(body.toString());
    });

    exception(CalculationException.class, (ex, request, response) -> response.body(ex.json().toString()));
    exception(MissingUserException.class, (ex, request, response) -> response.body(ex.json().toString()));
    exception(UserCreationException.class, (ex, request, response) -> response.body(ex.json().toString()));
    exception(UnauthorizedException.class, (ex, request, response) -> response.body(ex.json().toString()));
    exception(InputException.class, (ex, request, response) -> response.body(ex.json().toString()));

    exception(Exception.class, (ex, request, response) -> {
      JsonObject body = new JsonObject();
      body.addProperty("result", "error");
      body.addProperty("responseTime", System.currentTimeMillis());
      body.addProperty("message", "INTERNAL_ERROR");
      response.body(body.toString());
      Application.LOGGER.error("Error while handling request", ex);
    });

    loadController();
  }

  public void loadController() {
    Application.LOGGER.info("Loading controller..");
    for (Class controller : CONTROLLER) {
      try {
        Application.LOGGER.info("Loading " + controller.getSimpleName());
        controller.getConstructor().newInstance();
      } catch (Exception e) {
        Application.LOGGER.error("Error while loading controller " + controller.getSimpleName(), e);
      }
    }
  }

}
