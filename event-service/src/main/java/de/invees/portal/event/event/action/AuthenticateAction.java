package de.invees.portal.event.event.action;

import com.google.gson.JsonObject;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.TokenUtils;
import de.invees.portal.event.event.EventHandler;

public class AuthenticateAction implements Action {

  @Override
  public JsonObject execute(EventHandler handler, JsonObject message) {
    JsonObject response = new JsonObject();
    if (!message.has("token")) {
      response.addProperty("result", "error");
      response.addProperty("message", "UNAUTHORIZED");
      return response;
    }
    String parsedToken = message.get("token").getAsString();
    UserV1 user = TokenUtils.parseToken(parsedToken, null);
    if (user == null) {
      response.addProperty("result", "error");
      response.addProperty("message", "UNAUTHORIZED");
      return response;
    }
    handler.setUser(user);
    return response;
  }

}
