package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.datasource.response.PagedResponse;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.model.section.Section;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;

public class ProductDataSource implements DataSource<Product> {

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
    return Filters.eq(Section.ACTIVE, true);
  }

  public <Y extends Model> PagedResponse<Y> listForSection(String sectionId, Class<Y> type) {
    return new PagedResponse<>(
        -1,
        wrapped(
            collection.find(Filters.and(
                Filters.eq(Product.SECTION, sectionId),
                Filters.eq(Section.ACTIVE, true)
            )),
            type
        )
            .into(new ArrayList<>())
    );
  }

}
