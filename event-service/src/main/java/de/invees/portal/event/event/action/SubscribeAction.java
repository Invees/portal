package de.invees.portal.event.event.action;

import com.google.gson.JsonObject;
import de.invees.portal.event.event.EventHandler;

import java.util.List;

public class SubscribeAction implements Action {

  public static final String SERVICE_STATUS = "service/status";

  public static final List<String> AVAILABLE_EVENTS = List.of(
      SERVICE_STATUS
  );

  @Override
  public JsonObject execute(EventHandler handler, JsonObject message) {
    if (!message.has("event")) {
      JsonObject errorResponse = new JsonObject();
      errorResponse.addProperty("result", "error");
      errorResponse.addProperty("message", "Specified event does not exist!");
      return errorResponse;
    }

    String event = message.get("event").getAsString();
    if (event == null || !AVAILABLE_EVENTS.contains(event)) {
      JsonObject errorResponse = new JsonObject();
      errorResponse.addProperty("result", "error");
      errorResponse.addProperty("message", "Specified event does not exist!");
      return errorResponse;
    }
    handler.addSubscription(event);
    return new JsonObject();
  }

}
