package de.invees.portal.common.datasource.mongodb.v1;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.datasource.response.PagedResponse;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.section.SectionV1;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;

public class ProductDataSourceV1 implements DataSource<ProductV1> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public String getName() {
    return "ProductDataSource";
  }

  @Override
  public void init(MongoCollection<Document> collection, MongoCollection<Document> sequenceCollection) {
    this.collection = collection;
    this.sequenceCollection = sequenceCollection;
  }

  @Override
  public Bson listFilter() {
    return Filters.eq(SectionV1.ACTIVE, true);
  }

  public <Y extends Model> PagedResponse<Y> listForSection(String sectionId, Class<Y> type) {
    return new PagedResponse<>(
        -1,
        wrapped(
            collection.find(Filters.and(
                Filters.eq(ProductV1.SECTION, sectionId),
                Filters.eq(SectionV1.ACTIVE, true)
            )),
            type
        )
            .into(new ArrayList<>())
    );
  }

}
