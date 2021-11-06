package de.invees.portal.processing.master;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.nats.message.processing.MasterStartedMessage;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.processing.master.configuration.Configuration;
import de.invees.portal.processing.master.nats.PaymentMessageHandler;
import de.invees.portal.processing.master.nats.ProcessingMessageHandler;
import de.invees.portal.processing.master.worker.WorkerRegistryProvider;
import lombok.Getter;

public class Application extends BasicApplication {

  @Getter
  private Configuration configuration;
  private NatsProvider natsProvider;

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

    loadNatsProvider(this.configuration.getNats());
    this.natsProvider = ProviderRegistry.access(NatsProvider.class);
    loadMessageHandler();
    executeHandshake();
    LOGGER.info("-------- SERVICE STARTED --------");
    new Thread(() -> {
      while (true) ;
    }).start();
  }

  public void loadWorkerRegistry() {
    ProviderRegistry.register(WorkerRegistryProvider.class, new WorkerRegistryProvider());
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
            ProviderRegistry.access(WorkerRegistryProvider.class),
            ProviderRegistry.access(NatsProvider.class)
        ))
        .subscribe(Subject.PAYMENT, new PaymentMessageHandler(
            ProviderRegistry.access(WorkerRegistryProvider.class),
            this,
            ProviderRegistry.access(NatsProvider.class)
        ));
  }

  public void executeHandshake() {
    natsProvider.send(Subject.PROCESSING, new MasterStartedMessage());
  }
}
