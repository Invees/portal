package de.invees.portal.processing.master;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.nats.NatsService;
import de.invees.portal.common.utils.service.ServiceRegistry;
import de.invees.portal.processing.master.configuration.Configuration;
import de.invees.portal.processing.master.nats.ConnectionMessageHandler;
import de.invees.portal.processing.master.worker.WorkerRegistryService;

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
    loadWorkerRegistry();
    loadDataSource(this.configuration.getDataSource());
    loadNatsService(this.configuration.getNats());
    loadMessageHandler();
    while (true) ;
  }

  public void loadWorkerRegistry() {
    ServiceRegistry.register(WorkerRegistryService.class, new WorkerRegistryService());
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
    ServiceRegistry.access(NatsService.class)
        .subscribe("CONNECTION", new ConnectionMessageHandler(ServiceRegistry.access(WorkerRegistryService.class)));
  }

}
