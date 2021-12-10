package de.invees.portal.common.datasource.mongodb.v1;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Sorts;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.v1.contract.cancellation.ContractCancellationV1;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

public class ContractCancellationDataSourceV1 implements DataSource<ContractCancellationV1> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public String getName() {
    return "ContractCancellationDataSource";
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

  public ContractCancellationV1 getLastCancellation(long contract) {
    return getCollection()
        .find(Filters.eq(ContractCancellationV1.CONTRACT, contract))
        .sort(Sorts.descending(ContractCancellationV1.CREATED_AT))
        .map(d -> this.map(d, ContractCancellationV1.class))
        .first();
  }
}
