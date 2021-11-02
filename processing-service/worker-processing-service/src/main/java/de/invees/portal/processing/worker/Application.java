package de.invees.portal.processing.worker;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.model.service.ServiceType;
import de.invees.portal.common.model.worker.ProcessingWorker;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.HandshakeMessage;
import de.invees.portal.common.nats.message.processing.KeepAliveMessage;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.worker.configuration.Configuration;
import de.invees.portal.processing.worker.nats.ProcessingMessageHandler;
import de.invees.portal.processing.worker.service.provider.proxmox.ProxmoxServiceProvider;
import lombok.Getter;

public class Application extends BasicApplication {

  @Getter
  private Configuration configuration;
  private NatsProvider natsProvidr;

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
    this.natsProvidr = ProviderRegistry.access(NatsProvider.class);
    loadMessageHandler();
    executeHandshake();

    while (true) {
      try {
        Thread.sleep(2500);
        this.natsProvidr.send(Subject.PROCESSING, new KeepAliveMessage(configuration.getId()));
      } catch (Exception e) {
        LOGGER.error("", e);
      }
    }
  }

  public void postInitialize() {
    if (ready) {
      return;
    }
    loadDataSource(this.configuration.getDataSource());
    loadServiceProvider();
  }

  private void loadServiceProvider() {
    if (this.configuration.getServiceType() == ServiceType.VIRTUAL_SERVER) {
      ProviderRegistry.register(ProxmoxServiceProvider.class, new ProxmoxServiceProvider(configuration.getProxmox()));
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
        ));
  }

  public void executeHandshake() {
    natsProvidr.send(Subject.PROCESSING, new HandshakeMessage(
        new ProcessingWorker(configuration.getId(), configuration.getServiceType())
    ));
  }
}
