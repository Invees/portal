package de.invees.portal.common.datasource.mongodb.v1;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.v1.invoice.InvoiceV1;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

public class InvoiceDataSourceV1 implements DataSource<InvoiceV1> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public String getName() {
    return "InvoiceDataSource";
  }

  @Override
  public void init(MongoCollection<Document> collection, MongoCollection<Document> sequenceCollection) {
    this.collection = collection;
    this.sequenceCollection = sequenceCollection;
    this.createSequence();
  }

  @Override
  public Bson listFilter() {
    return null;
  }

  public void update(InvoiceV1 invoice) {
    this.getCollection().replaceOne(
        Filters.eq(InvoiceV1.ID, invoice.getId()),
        this.map(invoice)
    );
  }
}
