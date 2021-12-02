package de.invees.portal.common.datasource.mongodb.v1;

import com.mongodb.client.MongoCollection;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.v1.service.software.ServiceSoftwareV1;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

public class SoftwareDataSourceV1 implements DataSource<ServiceSoftwareV1> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public String getName() {
    return "SoftwareDataSource";
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

}
