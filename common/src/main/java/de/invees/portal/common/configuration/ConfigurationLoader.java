package de.invees.portal.common.configuration;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.utils.gson.GsonUtils;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

public class ConfigurationLoader {

  public <T> T loadFromEnvironment(Class<T> type) {
    JsonObject object = new JsonObject();
    loadFromEnvironment(object, "", type);
    return GsonUtils.GSON.fromJson(object, type);
  }

  public void loadFromEnvironment(JsonObject configuration, String parent, Class<?> type) {
    Field[] fields = type.getDeclaredFields();
    for (Field field : fields) {
      if (field.getType() == Boolean.class
          || field.getType() == boolean.class
          || field.getType() == Double.class
          || field.getType() == double.class
          || field.getType() == String.class
          || field.getType() == int.class
          || field.getType() == UUID.class
          || field.getType() == List.class) {
        String prefix = "";
        if (!parent.isEmpty()) {
          prefix = parent + "_";
        }
        String data = System.getProperty(prefix + field.getName());
        if (data == null) {
          data = System.getenv(prefix + field.getName());
        }
        if (data == null) {
          throw new IllegalArgumentException("Missing configuration entry for: " + prefix + field.getName());
        }
        if (data.contains(":") || data.contains("/")) {
          data = "\"" + data + "\"";
        }
        if (data != null) {
          configuration.add(field.getName(), JsonParser.parseString(data));
        } else {
          configuration.add(field.getName(), JsonParser.parseString(prefix + field.getName()));
        }
      } else {
        JsonObject subObject = new JsonObject();
        loadFromEnvironment(subObject, field.getName(), field.getType());
        configuration.add(field.getName(), subObject);
      }
    }
  }

}
