package de.invees.portal.event.nats;

import com.google.gson.JsonObject;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ServiceDataSourceV1;
import de.invees.portal.common.model.v1.service.DisplayServiceV1;
import de.invees.portal.common.model.v1.service.status.ServiceStatusV1;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.message.Message;
import de.invees.portal.common.nats.message.status.ServiceStatusMessage;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.event.Application;
import de.invees.portal.event.event.EventHandler;
import de.invees.portal.event.event.EventServerProvider;
import de.invees.portal.event.event.action.SubscribeAction;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Data
public class StatusMessageHandler implements MessageHandler {

  private Map<UUID, String> statusCache = new HashMap<>();

  @Override
  public void handle(Message message) {
    if (message instanceof ServiceStatusMessage) {
      execHandle((ServiceStatusMessage) message);
    }
  }

  private void execHandle(ServiceStatusMessage message) {
    EventServerProvider eventServerProvider = ProviderRegistry.access(EventServerProvider.class);
    for (ServiceStatusV1 status : message.getStatus()) {
      DisplayServiceV1 service = serviceDataSource().byId(status.getService(), DisplayServiceV1.class);
      String cachedStatus = GsonUtils.toJson(status);
      if (statusCache.containsKey(status.getService()) && statusCache.get(status.getService()).equals(cachedStatus)) {
        return;
      }
      statusCache.put(status.getService(), GsonUtils.toJson(status));
      for (EventHandler handler : eventServerProvider.getHandlers()) {
        try {
          if (handler.getUser() == null) {
            continue;
          }
          if (!handler.getSubscriptions().contains(SubscribeAction.SERVICE_STATUS)) {
            continue;
          }
          if (!handler.getUser().getId().equals(service.getBelongsTo())) {
            continue;
          }
          JsonObject object = new JsonObject();
          object.addProperty("type", "event");
          object.addProperty("event", SubscribeAction.SERVICE_STATUS);
          object.add("status", GsonUtils.toJsonElement(status));
          handler.send(object);
        } catch (Exception e) {
          Application.LOGGER.error("", e);
        }
      }
    }
  }

  private ServiceDataSourceV1 serviceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ServiceDataSourceV1.class);
  }

}
