package de.invees.portal.common.datasource.mongodb.v1;

import com.mongodb.client.MongoCollection;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.invoice.InvoiceFileV1;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

public class InvoiceFileDataSourceV1 implements DataSource<InvoiceFileV1> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public String getName() {
    return "InvoiceFileDataSource";
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

  @Override
  public Document map(Object object) {
    if (!(object instanceof InvoiceFileV1)) {
      throw new IllegalArgumentException("Expected InvoiceFile but got " + object.getClass());
    }
    InvoiceFileV1 invoiceFile = (InvoiceFileV1) object;
    return new Document()
        .append(InvoiceFileV1.ID, invoiceFile.getId())
        .append(InvoiceFileV1.DATA, new Binary(invoiceFile.getData()));
  }

  @Override
  public <T extends Model> T map(Document document, Class<T> target) {
    if (!target.equals(InvoiceFileV1.class)) {
      throw new IllegalArgumentException("Expected InvoiceFile but got " + target);
    }
    return (T) new InvoiceFileV1(
        document.get(InvoiceFileV1.ID, Long.class),
        document.get(InvoiceFileV1.DATA, Binary.class).getData()
    );
  }

}
