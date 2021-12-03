package de.invees.portal.common.exception;

import com.google.gson.JsonObject;

public class MissingWorkerException extends RuntimeException {

  public JsonObject json() {
    JsonObject response = new JsonObject();
    response.addProperty("result", "error");
    response.addProperty("responseTime", System.currentTimeMillis());
    response.addProperty("message", "WORKER_NOT_AVAILABLE");
    return response;
  }

}
