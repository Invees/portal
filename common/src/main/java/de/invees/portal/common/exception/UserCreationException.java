package de.invees.portal.common.exception;

import com.google.gson.JsonObject;

public class UserCreationException extends IllegalArgumentException {

  private final String msg;

  public UserCreationException(String msg) {
    super(msg);
    this.msg = msg;
  }

  public JsonObject json() {
    JsonObject response = new JsonObject();
    response.addProperty("result", "error");
    response.addProperty("responseTime", System.currentTimeMillis());
    response.addProperty("message", msg);
    return response;
  }
}
