package de.invees.portal.processing.worker;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.model.v1.service.ServiceType;
import de.invees.portal.common.model.v1.worker.ProcessingWorker;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.HandshakeMessage;
import de.invees.portal.common.nats.message.processing.KeepAliveMessage;
import de.invees.portal.common.nats.message.status.ServiceStatusMessage;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.worker.configuration.Configuration;
import de.invees.portal.processing.worker.nats.ProcessingMessageHandler;
import de.invees.portal.processing.worker.nats.StatusMessageHandler;
import de.invees.portal.processing.worker.service.provider.ServiceProvider;
import de.invees.portal.processing.worker.service.provider.proxmox.ProxmoxServiceProvider;
import lombok.Getter;

public class Application extends BasicApplication {

  @Getter
  private Configuration configuration;
  private NatsProvider natsProvider;

  private boolean ready = false;

  public static void main(String[] args) {
    new Application();
  }

  private Application() {
    LOGGER.info("Starting Invees/Processing/Worker v" + VERSION);
    if (!loadConfiguration()) {
      return;
    }

    // Nats
    loadNatsProvider(this.configuration.getNats());
    this.natsProvider = ProviderRegistry.access(NatsProvider.class);
    loadMessageHandler();
    executeHandshake();
  }

  public void postInitialize() {
    if (ready) {
      return;
    }
    ready = true;
    loadDataSource(this.configuration.getDataSource());
    loadServiceProvider();
    LOGGER.info("-------- SERVICE STARTED --------");
    new Thread(() -> {
      while (true) {
        try {
          this.natsProvider.send(Subject.STATUS, new ServiceStatusMessage(
              ProviderRegistry.access(ServiceProvider.class).getAllServiceStatus()
          ));
        } catch (Exception e) {
          LOGGER.error("", e);
        }
        try {
          this.natsProvider.send(Subject.PROCESSING, new KeepAliveMessage(
              configuration.getId(),
              ProviderRegistry.access(ServiceProvider.class).getUsage()
          ));
          Thread.sleep(2500);
        } catch (Exception e) {
          LOGGER.error("", e);
        }
      }
    }).start();
  }

  private void loadServiceProvider() {
    if (ServiceType.valueOf(this.configuration.getServiceType()) == ServiceType.VIRTUAL_SERVER) {
      ProxmoxServiceProvider provider = new ProxmoxServiceProvider(configuration);
      ProviderRegistry.register(ServiceProvider.class, provider);
      ProviderRegistry.register(ProxmoxServiceProvider.class, provider);
    }
  }

  public boolean loadConfiguration() {
    try {
      LOGGER.info("Loading configuration..");
      this.configuration = this.loadConfiguration(Configuration.class);
      return true;
    } catch (Exception e) {
      LOGGER.warn("Error while reading configuration", e);
      return false;
    }
  }

  public void loadMessageHandler() {
    ProviderRegistry.access(NatsProvider.class)
        .subscribe(Subject.PROCESSING, new ProcessingMessageHandler(
            this,
            ProviderRegistry.access(NatsProvider.class)
        ))
        .subscribe(Subject.STATUS, new StatusMessageHandler(
            this,
            ProviderRegistry.access(NatsProvider.class)
        ));
  }

  public void executeHandshake() {
    natsProvider.send(Subject.PROCESSING, new HandshakeMessage(
        new ProcessingWorker(configuration.getId(), ServiceType.valueOf(configuration.getServiceType()))
    ));
  }
}
