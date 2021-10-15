package de.invees.portal.common.utils.gson;

import com.google.gson.*;

public class GsonUtils {

  public static final Gson GSON_PRETTY = new GsonBuilder().setPrettyPrinting().create();
  public static final Gson GSON = new GsonBuilder().create();

  private GsonUtils() {
  }

  public static String toJson(Object object) {
    return toJson(object, false);
  }

  public static String toJson(Object object, boolean pretty) {
    if (pretty) {
      return GSON_PRETTY.toJson(object);
    }
    return GSON.toJson(object);
  }

  public static JsonElement toJsonElement(Object object) {
    return GSON.toJsonTree(object);
  }

  public static JsonObject fromJson(String body) {
    return JsonParser.parseString(body).getAsJsonObject();
  }

}
