package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.product.ProductPrototype;
import de.invees.portal.common.model.section.Section;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class ProductDataSource implements DataSource {

  @Getter
  private MongoCollection<Document> collection;

  @Override
  public void init(MongoCollection<Document> collection) {
    this.collection = collection;
  }

  public List<ProductPrototype> getProducts() {
    return collection.find(
            Filters.eq(Section.ACTIVE, true)
        )
        .projection(Projections.include(ProductPrototype.projection()))
        .map(document -> this.map(document, ProductPrototype.class))
        .into(new ArrayList<>());
  }

  public List<Product> getProductsForSection(String sectionId) {
    return collection.find(Filters.and(
            Filters.eq(Product.SECTION_ID, sectionId),
            Filters.eq(Section.ACTIVE, true)
        ))
        .projection(Projections.include(Product.projection()))
        .map(document -> this.map(document, Product.class))
        .into(new ArrayList<>());
  }

  public Product getProduct(String id) {
    return collection.find(Filters.eq(Product.ID, id))
        .projection(Projections.include(Product.projection()))
        .map(document -> this.map(document, Product.class))
        .first();
  }
}
