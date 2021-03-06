package de.invees.portal.processing.worker.nats;

import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ServiceDataSourceV1;
import de.invees.portal.common.model.v1.service.ServiceTypeV1;
import de.invees.portal.common.model.v1.service.ServiceV1;
import de.invees.portal.common.model.v1.worker.ProcessingWorkerV1;
import de.invees.portal.common.nats.MessageHandler;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.Message;
import de.invees.portal.common.nats.message.processing.*;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.worker.Application;
import de.invees.portal.processing.worker.service.provider.ServiceProvider;
import lombok.Data;

@Data
public class ProcessingMessageHandler implements MessageHandler {

  private final Application application;
  private final NatsProvider natsProvider;

  @Override
  public void handle(Message message) {
    if (message instanceof WelcomeMessage) {
      execHandle((WelcomeMessage) message);
    } else if (message instanceof MasterStartedMessage) {
      execHandle((MasterStartedMessage) message);
    } else if (message instanceof ExecuteOrderMessage) {
      execHandle((ExecuteOrderMessage) message);
    } else if (message instanceof UpgradeContractMessage) {
      execHandle((UpgradeContractMessage) message);
    }
  }

  private void execHandle(WelcomeMessage message) {
    if (message.getId().equals(application.getConfiguration().getId())) {
      application.postInitialize();
    }
  }

  private void execHandle(MasterStartedMessage message) {
    Application.LOGGER.info("Found new master - Handshake!");
    natsProvider.send(Subject.PROCESSING, new HandshakeMessage(new ProcessingWorkerV1(
        application.getConfiguration().getId(),
        ServiceTypeV1.valueOf(application.getConfiguration().getServiceType())
    )));
  }

  private void execHandle(ExecuteOrderMessage message) {
    if (!message.getWorkerId().equals(application.getConfiguration().getId())) {
      return;
    }
    ProviderRegistry.access(ServiceProvider.class).create(message.getContract());
  }

  private void execHandle(UpgradeContractMessage message) {
    ServiceV1 service = serviceDataSource()
        .getCollection()
        .find(Filters.eq(ServiceV1.CONTRACT, message.getContract()))
        .map(d -> serviceDataSource().map(d, ServiceV1.class))
        .first();
    ProviderRegistry.access(ServiceProvider.class).applyUpgrade(service.getId(), message.getUpgrades());
  }

  private ServiceDataSourceV1 serviceDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(ServiceDataSourceV1.class);
  }

}
