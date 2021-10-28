package de.invees.portal.core;

import de.invees.portal.common.BasicApplication;
import de.invees.portal.common.datasource.MongoService;
import de.invees.portal.common.gateway.paypal.PayPalGatewayService;
import de.invees.portal.common.utils.service.ServiceRegistry;
import de.invees.portal.core.configuration.Configuration;
import de.invees.portal.core.web.SparkServer;

public class Application extends BasicApplication {

  private Configuration configuration;

  public static void main(String[] args) {
    new Application();
  }

  private Application() {
    LOGGER.info("Starting Invees/Portal/Backend v" + VERSION);
    if (!loadConfiguration()) {
      return;
    }
    loadDataSource(configuration.getDataSource());
    loadPayPal();
    startWebServer();
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
    ServiceRegistry.register(PayPalGatewayService.class, new PayPalGatewayService(
        configuration.getPaypal()
    ));
  }

  public void startWebServer() {
    LOGGER.info("Starting Web Server..");
    new SparkServer();
  }

}
