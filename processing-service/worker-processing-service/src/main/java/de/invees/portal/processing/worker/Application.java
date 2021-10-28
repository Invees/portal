package de.invees.portal.processing.worker;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.model.worker.ProcessingWorker;
import de.invees.portal.common.nats.NatsService;
import de.invees.portal.common.nats.message.HandshakeMessage;
import de.invees.portal.common.utils.service.ServiceRegistry;
import de.invees.portal.processing.worker.configuration.Configuration;

import java.util.UUID;

public class Application extends BasicApplication {

  private Configuration configuration;

  public static void main(String[] args) {
    new Application();
  }

  private Application() {
    LOGGER.info("Starting Invees/Processing/Master v" + VERSION);
    if (!loadConfiguration()) {
      return;
    }
    loadDataSource(this.configuration.getDataSource());
    loadNatsService(this.configuration.getNats());
    executeHandshake();
    while (true) ;
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

  public void executeHandshake() {
    ServiceRegistry.access(NatsService.class)
        .send("CONNECTION", new HandshakeMessage(
            new ProcessingWorker(UUID.randomUUID())
        ));
  }
}
