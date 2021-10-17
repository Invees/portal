package de.invees.portal.common.exception;

import com.google.gson.JsonObject;

public class InputException extends IllegalArgumentException {

  private final String msg;

  public InputException(String msg) {
    super(msg);
    this.msg = msg;
  }

  public JsonObject json() {
    JsonObject response = new JsonObject();
    response.addProperty("result", "error");
    response.addProperty("message", msg);
    return response;
  }
}
