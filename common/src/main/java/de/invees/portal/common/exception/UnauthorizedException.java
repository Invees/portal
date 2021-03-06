package de.invees.portal.common.exception;

import com.google.gson.JsonObject;

public class UnauthorizedException extends IllegalArgumentException {

  private final String msg;

  public UnauthorizedException() {
    this("UNAUTHORIZED");
  }

  public UnauthorizedException(String msg) {
    super();
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
