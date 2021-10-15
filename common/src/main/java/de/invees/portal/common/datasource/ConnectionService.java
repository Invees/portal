package de.invees.portal.common.datasource;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.datasource.mongodb.*;
import de.invees.portal.common.utils.service.Service;
import lombok.NonNull;

import java.util.HashMap;
import java.util.Map;

public class ConnectionService implements Service {

  private final Map<Class<? extends DataSource>, DataSource> dataSourceMap = new HashMap<>();
  private final MongoClient client;
  private final MongoDatabase database;

  public ConnectionService(DataSourceConfiguration dataSource) {
    client = new MongoClient(
        new ServerAddress(dataSource.getHost(), dataSource.getPort()),
        MongoCredential.createCredential(
            dataSource.getUser(),
            dataSource.getAuthDatabase(),
            dataSource.getPassword().toCharArray()),
        new MongoClientOptions.Builder()
            .connectTimeout(dataSource.getConnectTimeout())
            .socketTimeout(dataSource.getSocketTimeOut())
            .maxConnectionIdleTime(dataSource.getMaxConnectionIdleTime())
            .heartbeatFrequency(dataSource.getHeartbeatFrequency())
            .maxWaitTime(dataSource.getMaxWaitTime())
            .build()
    );

    database = client.getDatabase(dataSource.getDatabase());
    database.runCommand(new BasicDBObject("ping", 1)); // ping

    dataSourceMap.put(SectionDataSource.class, new SectionDataSource());
    dataSourceMap.put(ProductDataSource.class, new ProductDataSource());
    dataSourceMap.put(UserDataSource.class, new UserDataSource());
    dataSourceMap.put(UserAuthenticationDataSource.class, new UserAuthenticationDataSource());
    dataSourceMap.put(OrderDataSource.class, new OrderDataSource());
    dataSourceMap.put(InvoiceDataSource.class, new InvoiceDataSource());

    dataSourceMap.forEach((k, d) -> d.init(database.getCollection(k.getSimpleName().replace("DataSource", ""))));
  }

  public <T extends DataSource> T access(@NonNull Class<T> model) {
    return (T) dataSourceMap.get(model);
  }
}
