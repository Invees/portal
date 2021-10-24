package de.invees.portal.core;

import de.invees.portal.common.gateway.paypal.PayPalGatewayService;
import de.invees.portal.core.configuration.Configuration;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.service.ServiceRegistry;
import de.invees.portal.core.web.SparkServer;
import de.invees.portal.common.datasource.ConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileReader;

public class Application {

  public static final String VERSION = "1.0.0";
  public static final Logger LOGGER = LoggerFactory.getLogger(Application.class);

  private Configuration configuration;

  public static void main(String[] args) {
    new Application();
  }

  private Application() {
    LOGGER.info("Starting Invees/Portal/Backend v" + VERSION);
    if (!loadConfiguration()) {
      return;
    }
    loadDataSource();
    loadPayPal();
    startWebServer();
  }

  public boolean loadConfiguration() {
    try {
      LOGGER.info("Loading configuration..");
      this.configuration = GsonUtils.GSON.fromJson(new FileReader("configuration.json"), Configuration.class);
      return true;
    } catch (Exception e) {
      LOGGER.warn("Error while reading configuration", e);
      return false;
    }
  }

  public void loadDataSource() {
    LOGGER.info("Loading DataSourceConnectionService..");
    ServiceRegistry.register(
        ConnectionService.class,
        new ConnectionService(
            configuration.getDataSource()
        )
    );
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
