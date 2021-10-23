package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.paypal.orders.Order;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.invoice.Invoice;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

public class GatewayDataSource implements DataSource<Order> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public void init(MongoCollection<Document> collection, MongoCollection<Document> sequenceCollection) {
    this.collection = collection;
    this.sequenceCollection = sequenceCollection;
  }

  @Override
  public Bson listFilter() {
    return null;
  }

}
