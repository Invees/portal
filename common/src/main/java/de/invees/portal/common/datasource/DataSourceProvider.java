package de.invees.portal.common.datasource;

import com.mongodb.*;
import com.mongodb.client.MongoDatabase;
import de.invees.portal.common.configuration.DataSourceConfiguration;
import de.invees.portal.common.datasource.mongodb.v1.*;
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

    dataSourceMap.put(SectionDataSourceV1.class, new SectionDataSourceV1());
    dataSourceMap.put(ProductDataSourceV1.class, new ProductDataSourceV1());
    dataSourceMap.put(UserDataSourceV1.class, new UserDataSourceV1());
    dataSourceMap.put(UserAuthenticationDataSourceV1.class, new UserAuthenticationDataSourceV1());
    dataSourceMap.put(OrderDataSourceV1.class, new OrderDataSourceV1());
    dataSourceMap.put(InvoiceDataSourceV1.class, new InvoiceDataSourceV1());
    dataSourceMap.put(InvoiceFileDataSourceV1.class, new InvoiceFileDataSourceV1());
    dataSourceMap.put(GatewayDataDataSourceV1.class, new GatewayDataDataSourceV1());
    dataSourceMap.put(ServiceDataSourceV1.class, new ServiceDataSourceV1());
    dataSourceMap.put(SoftwareDataSourceV1.class, new SoftwareDataSourceV1());
    dataSourceMap.put(NetworkAddressDataSourceV1.class, new NetworkAddressDataSourceV1());

    dataSourceMap.forEach((k, d) -> d.init(
        database.getCollection(d.getName().replace("DataSource", "")),
        database.getCollection("Sequence")
    ));
  }

  public <T extends DataSource> T access(@NonNull Class<T> type) {
    return (T) dataSourceMap.get(type);
  }
}
