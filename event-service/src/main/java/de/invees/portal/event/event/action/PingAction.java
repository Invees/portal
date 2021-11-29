package de.invees.portal.event.event.action;

import com.google.gson.JsonObject;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.TokenUtils;
import de.invees.portal.event.event.EventHandler;

public class PingAction implements Action {

  @Override
  public JsonObject execute(EventHandler handler, JsonObject message) {
    JsonObject response = new JsonObject();
    response.addProperty("message", "pong");
    response.addProperty("time", System.currentTimeMillis());
    return response;
  }

}
