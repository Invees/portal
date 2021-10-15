package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import de.invees.portal.common.model.order.Order;
import de.invees.portal.common.datasource.DataSource;
import lombok.Getter;
import org.bson.Document;

public class OrderDataSource implements DataSource {

  @Getter
  private MongoCollection<Document> collection;

  @Override
  public void init(MongoCollection<Document> collection) {
    this.collection = collection;
  }

  public void create(Order order) {
    this.collection.insertOne(this.map(order));
  }

}
