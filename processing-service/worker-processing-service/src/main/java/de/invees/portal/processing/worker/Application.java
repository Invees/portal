package de.invees.portal.processing.worker;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.model.worker.ProcessingWorker;
import de.invees.portal.common.nats.NatsService;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.HandshakeMessage;
import de.invees.portal.common.nats.message.processing.KeepAliveMessage;
import de.invees.portal.common.utils.service.ServiceRegistry;
import de.invees.portal.processing.worker.configuration.Configuration;
import de.invees.portal.processing.worker.nats.ConnectionMessageHandler;
import lombok.Getter;

public class Application extends BasicApplication {

  @Getter
  private Configuration configuration;
  private NatsService natsService;

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
    loadNatsService(this.configuration.getNats());
    this.natsService = ServiceRegistry.access(NatsService.class);
    loadMessageHandler();
    executeHandshake();

    while (true) {
      try {
        Thread.sleep(2500);
        this.natsService.send(Subject.PROCESSING, new KeepAliveMessage(configuration.getId()));
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
  }

  public boolean loadConfiguration() {
    try {
      LOGGER.info("Loading configuration..");
      this.configuration = this.loadConfiguration(Configuration.class);
      return true;
    } catch (Exception e) {
      e.printStackTrace();
      LOGGER.warn("Error while reading configuration", e);
      return false;
    }
  }

  public void loadMessageHandler() {
    ServiceRegistry.access(NatsService.class)
        .subscribe(Subject.PROCESSING, new ConnectionMessageHandler(
            this,
            ServiceRegistry.access(NatsService.class)
        ));
  }

  public void executeHandshake() {
    natsService.send(Subject.PROCESSING, new HandshakeMessage(
        new ProcessingWorker(configuration.getId(), configuration.getServiceType())
    ));
  }
}
