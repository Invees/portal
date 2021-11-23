package de.invees.portal.core;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.gateway.paypal.PayPalGatewayProvider;
import de.invees.portal.common.nats.NatsProvider;
import de.invees.portal.common.nats.Subject;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.configuration.Configuration;
import de.invees.portal.core.nats.StatusMessageHandler;
import de.invees.portal.core.service.ServiceProvider;
import de.invees.portal.core.web.HttpServerProvider;

public class Application extends BasicApplication {

  private Configuration configuration;

  public static void main(String[] args) {
    new Application();
  }

  private Application() {
    LOGGER.info("Starting Invees/Portal/Core v" + VERSION);
    if (!loadConfiguration()) {
      return;
    }
    loadDataSource(configuration.getDataSource());
    loadPayPal();
    loadNatsProvider(configuration.getNats());
    startWebServer();
    loadServiceProvider();
    loadMessageHandler();
    LOGGER.info("-------- SERVICE STARTED --------");
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

  public void loadPayPal() {
    LOGGER.info("Loading PayPal Gateway..");
    ProviderRegistry.register(PayPalGatewayProvider.class, new PayPalGatewayProvider(
        configuration.getPaypal()
    ));
  }

  public void startWebServer() {
    LOGGER.info("Starting Http Server Provider..");
    ProviderRegistry.register(HttpServerProvider.class, new HttpServerProvider(configuration));
  }

  public void loadServiceProvider() {
    LOGGER.info("Starting Service Provider..");
    ProviderRegistry.register(ServiceProvider.class, new ServiceProvider());
  }

  public void loadMessageHandler() {
    ProviderRegistry.access(NatsProvider.class)
        .subscribe(Subject.STATUS, new StatusMessageHandler(
            this,
            ProviderRegistry.access(NatsProvider.class)
        ));
  }
}
