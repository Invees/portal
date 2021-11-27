package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.v1.Model;
import de.invees.portal.common.model.v1.invoice.InvoiceFile;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.types.Binary;

public class InvoiceFileDataSource implements DataSource<InvoiceFile> {

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

  @Override
  public Document map(Object object) {
    if (!(object instanceof InvoiceFile)) {
      throw new IllegalArgumentException("Expected InvoiceFile but got " + object.getClass());
    }
    InvoiceFile invoiceFile = (InvoiceFile) object;
    return new Document()
        .append(InvoiceFile.ID, invoiceFile.getId())
        .append(InvoiceFile.DATA, new Binary(invoiceFile.getData()));
  }

  @Override
  public <T extends Model> T map(Document document, Class<T> target) {
    if (!target.equals(InvoiceFile.class)) {
      throw new IllegalArgumentException("Expected InvoiceFile but got " + target);
    }
    return (T) new InvoiceFile(
        document.get(InvoiceFile.ID, Long.class),
        document.get(InvoiceFile.DATA, Binary.class).getData()
    );
  }

}
