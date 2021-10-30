package de.invees.portal.common;

import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.configuration.NatsConfiguration;
import de.invees.portal.common.datasource.MongoService;
import de.invees.portal.common.nats.NatsService;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.service.ServiceRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileReader;

public class BasicApplication {

  public static final String VERSION = "1.0.0";
  public static final Logger LOGGER = LoggerFactory.getLogger("application");

  public <T> T loadConfiguration(Class<T> type) throws FileNotFoundException {
    try {
      return GsonUtils.GSON.fromJson(new FileReader("configuration.json"), type);
    } catch (FileNotFoundException e) {
      throw e;
    }
  }

  public void loadDataSource(DataSourceConfiguration configuration) {
    LOGGER.info("Loading DataSourceConnectionService..");
    ServiceRegistry.register(
        MongoService.class,
        new MongoService(configuration)
    );
  }

  public void loadNatsService(NatsConfiguration configuration) {
    LOGGER.info("Loading NATS Service..");
    ServiceRegistry.register(
        NatsService.class,
        new NatsService(configuration)
    );

  }
}
