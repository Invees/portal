package de.invees.portal.event.event;

import com.google.gson.JsonObject;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.event.Application;
import de.invees.portal.event.event.action.Action;
import de.invees.portal.event.event.action.AuthenticateAction;
import de.invees.portal.event.event.action.PingAction;
import de.invees.portal.event.event.action.SubscribeAction;
import lombok.Getter;
import lombok.Setter;
import org.bson.json.JsonParseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventHandler extends WebSocketAdapter {

  public static final Map<String, Action> ACTION_MAP = Map.of(
      "subscribe", new SubscribeAction(),
      "authenticate", new AuthenticateAction(),
      "ping", new PingAction()
  );

  @Getter
  private final List<String> subscriptions = new ArrayList<>();
  @Getter
  @Setter
  private UserV1 user;

  @Override
  public void onWebSocketConnect(Session sess) {
    ProviderRegistry.access(EventServerProvider.class).append(this);
    super.onWebSocketConnect(sess);
  }

  @Override
  public void onWebSocketText(String message) {
    try {
      JsonObject object = GsonUtils.fromJson(message);
      String nonce = null;
      if (object.has("nonce")) {
        nonce = object.get("nonce").getAsString();
      }
      if (!ACTION_MAP.containsKey(object.get("action").getAsString())) {
        JsonObject response = new JsonObject();
        response.addProperty("nonce", nonce);
        response.addProperty("type", "response");
        response.addProperty("result", "error");
        response.addProperty("message", "Action not found!");
        this.getRemote().sendString(response.toString());
        return;
      }
      Action action = ACTION_MAP.get(object.get("action").getAsString());
      JsonObject response = action.execute(this, object);
      if (response != null) {
        response.addProperty("nonce", nonce);
        response.addProperty("type", "response");
        if (!response.has("result")) {
          response.addProperty("result", "success");
        }
        this.getRemote().sendString(response.toString());
      }
    } catch (JsonParseException | IllegalStateException e) {
      JsonObject response = new JsonObject();
      response.addProperty("result", "error");
      response.addProperty("type", "response");
      response.addProperty("message", "Invalid message!");
      this.send(response);
    } catch (Exception e) {
      JsonObject response = new JsonObject();
      response.addProperty("result", "error");
      response.addProperty("type", "response");
      response.addProperty("message", "Internal exception");
      this.send(response);
      Application.LOGGER.error("", e);
    }
  }

  @Override
  public void onWebSocketClose(int statusCode, String reason) {
    ProviderRegistry.access(EventServerProvider.class).remove(this);
  }

  @Override
  public void onWebSocketError(Throwable cause) {
  }

  public void addSubscription(String event) {
    subscriptions.add(event);
  }

  public void send(JsonObject object) {
    try {
      this.getRemote().sendString(object.toString());
    } catch (Exception e) {
      Application.LOGGER.error("", e);
    }
  }

}