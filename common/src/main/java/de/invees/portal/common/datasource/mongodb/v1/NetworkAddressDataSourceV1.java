package de.invees.portal.common.datasource.mongodb.v1;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.v1.service.network.NetworkAddressV1;
import lombok.Getter;
import org.bson.BsonNull;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class NetworkAddressDataSourceV1 implements DataSource<NetworkAddressV1> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public String getName() {
    return "NetworkAddressDataSource";
  }

  @Override
  public void init(MongoCollection<Document> collection, MongoCollection<Document> sequenceCollection) {
    this.collection = collection;
    this.sequenceCollection = sequenceCollection;
  }

  @Override
  public Bson listFilter() {
    return null;
  }

  public NetworkAddressV1 applyNextAddress(UUID service) {
    Document document = getCollection().find(Filters.eq(NetworkAddressV1.SERVICE, BsonNull.VALUE)).first();
    if (document == null) {
      throw new IllegalStateException("Can't find any address to apply!");
    }
    NetworkAddressV1 address = map(
        getCollection().find(document).first(),
        NetworkAddressV1.class
    );
    address.setService(service);
    this.getCollection().replaceOne(Filters.eq(NetworkAddressV1.ID, address.getId().toString()), map(address));
    return address;
  }

  public List<NetworkAddressV1> getAddressesOfService(UUID service) {
    return getCollection().find(Filters.eq(NetworkAddressV1.SERVICE, service.toString()))
        .map(document -> map(document, NetworkAddressV1.class))
        .into(new ArrayList<>());
  }

}
