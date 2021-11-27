package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.v1.order.Order;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;

public class OrderDataSource implements DataSource<Order> {

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

  public void update(Order order) {
    this.getCollection().replaceOne(
        Filters.eq(Order.ID, order.getId().toString()),
        this.map(order)
    );
  }

  public List<Order> byInvoice(long id) {
    return this.list(Order.class, Filters.eq(Order.INVOICE, id)).getItems();
  }
}
