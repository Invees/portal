package de.invees.portal.event.nats;

import com.google.gson.JsonObject;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ServiceDataSourceV1;
import de.invees.portal.common.model.v1.service.ServiceV1;
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

@Data
public class StatusMessageHandler implements MessageHandler {

  @Override
  public void handle(Message message) {
    if (message instanceof ServiceStatusMessage) {
      execHandle((ServiceStatusMessage) message);
    }
  }

  private void execHandle(ServiceStatusMessage message) {
    EventServerProvider eventServerProvider = ProviderRegistry.access(EventServerProvider.class);
    for (ServiceStatusV1 status : message.getStatus()) {
      ServiceV1 service = serviceDataSource().byId(status.getService(), ServiceV1.class);
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
