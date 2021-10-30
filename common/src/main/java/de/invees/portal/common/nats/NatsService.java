package de.invees.portal.common.nats;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.configuration.NatsConfiguration;
import de.invees.portal.common.nats.message.processing.Message;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.service.Service;
import io.nats.client.Connection;
import io.nats.client.Dispatcher;
import io.nats.client.Nats;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class NatsService implements Service {

  private final Connection connection;
  private final Map<String, MessageHandler> handlerMap = new HashMap<>();
  private Dispatcher dispatcher;

  public NatsService(NatsConfiguration configuration) {
    try {
      this.connection = Nats.connect(configuration.getUrl());
      dispatcher = this.connection.createDispatcher((msg) -> {
        MessageHandler handler = handlerMap.get(msg.getSubject());
        if (handler == null) {
          return;
        }
        String data = new String(msg.getData(), StandardCharsets.UTF_8);
        try {
          handler.handle(
              this.map(JsonParser.parseString(data).getAsJsonObject())
          );
        } catch (Exception e) {
          BasicApplication.LOGGER.error("", e);
        }
      });
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  public NatsService send(String subject, Message message) {
    this.connection.publish(subject, map(message).toString().getBytes(StandardCharsets.UTF_8));
    return this;
  }

  public NatsService subscribe(String subject, MessageHandler handler) {
    handlerMap.put(subject, handler);
    dispatcher.subscribe(subject);
    return this;
  }

  private JsonObject map(Message message) {
    JsonObject data = new JsonObject();
    data.addProperty("message", message.getClass().getName());
    data.add("data", GsonUtils.GSON.toJsonTree(message));
    return data;
  }

  public <T extends Message> T map(JsonObject data) throws Exception {
    try {
      return (T) GsonUtils.GSON.fromJson(
          data.get("data"),
          Class.forName(data.get("message").getAsString())
      );
    } catch (Exception e) {
      throw e;
    }
  }
}
