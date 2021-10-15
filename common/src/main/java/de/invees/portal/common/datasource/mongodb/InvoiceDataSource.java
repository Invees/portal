package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.invoice.Invoice;
import de.invees.portal.common.model.order.Order;
import lombok.Getter;
import org.bson.Document;

public class InvoiceDataSource implements DataSource {

  @Getter
  private MongoCollection<Document> collection;

  @Override
  public void init(MongoCollection<Document> collection) {
    this.collection = collection;
  }

  public void create(Invoice invoice) {
    this.collection.insertOne(this.map(invoice));
  }

}
