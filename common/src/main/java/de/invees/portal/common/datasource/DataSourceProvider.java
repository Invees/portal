package de.invees.portal.common.datasource;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.datasource.mongodb.*;
import de.invees.portal.common.utils.provider.Provider;
import lombok.NonNull;
import org.bson.codecs.BinaryCodec;
import org.bson.codecs.configuration.CodecRegistries;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.HashMap;
import java.util.Map;

public class DataSourceProvider implements Provider {

  private final Map<Class<? extends DataSource>, DataSource> dataSourceMap = new HashMap<>();
  private final MongoClient client;
  private final MongoDatabase database;

  public DataSourceProvider(DataSourceConfiguration dataSource) {
    CodecRegistry codecRegistry = CodecRegistries.fromRegistries(
        CodecRegistries.fromCodecs(new BinaryCodec()),
        MongoClient.getDefaultCodecRegistry()
    );

    client = new MongoClient(
        new ServerAddress(dataSource.getHost(), dataSource.getPort()),
        MongoCredential.createCredential(
            dataSource.getUser(),
            dataSource.getAuthDatabase(),
            dataSource.getPassword().toCharArray()),
        new MongoClientOptions.Builder()
            .codecRegistry(codecRegistry)
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
    dataSourceMap.put(InvoiceFileDataSource.class, new InvoiceFileDataSource());
    dataSourceMap.put(GatewayDataDataSource.class, new GatewayDataDataSource());
    dataSourceMap.put(ServiceDataSource.class, new ServiceDataSource());

    dataSourceMap.forEach((k, d) -> d.init(
        database.getCollection(k.getSimpleName().replace("DataSource", "")),
        database.getCollection("Sequence")
    ));
  }

  public <T extends DataSource> T access(@NonNull Class<T> model) {
    return (T) dataSourceMap.get(model);
  }
}
