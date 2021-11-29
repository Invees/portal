package de.invees.portal.event;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.event.configuration.Configuration;
import de.invees.portal.event.event.EventServerProvider;
import de.invees.portal.event.nats.StatusMessageHandler;
import lombok.Getter;

public class Application extends BasicApplication {

  @Getter
  private Configuration configuration;

  public static void main(String[] args) {
    new Application();
  }

  private Application() {
    LOGGER.info("Starting Invees/Portal/Event v" + VERSION);
    if (!loadConfiguration()) {
      return;
    }
    loadEventServerProvider();
    loadDataSource(this.configuration.getDataSource());
    loadNatsProvider(this.configuration.getNats());

    loadMessageHandler();
    LOGGER.info("-------- SERVICE STARTED --------");
    new Thread(() -> {
      while (true) ;
    }).start();
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

  public void loadEventServerProvider() {
    ProviderRegistry.register(EventServerProvider.class, new EventServerProvider(configuration));
  }

  public void loadMessageHandler() {
    ProviderRegistry.access(NatsProvider.class)
        .subscribe(Subject.STATUS, new StatusMessageHandler());
  }
}
