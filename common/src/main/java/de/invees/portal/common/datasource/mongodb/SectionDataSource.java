package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.section.Section;
import de.invees.portal.common.model.section.SectionPrototype;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;

public class SectionDataSource implements DataSource {

  @Getter
  private MongoCollection<Document> collection;

  @Override
  public void init(MongoCollection<Document> collection) {
    this.collection = collection;
  }

  public List<SectionPrototype> getSections() {
    return collection.find(
            Filters.eq(Section.ACTIVE, true)
        )
        .projection(Projections.include(SectionPrototype.projection()))
        .map(document -> this.map(document, SectionPrototype.class))
        .into(new ArrayList<>());
  }

  public Section getSection(String id) {
    return collection.find(Filters.eq(Section.ID, id))
        .projection(Projections.include(Section.projection()))
        .map(document -> this.map(document, Section.class))
        .first();
  }
}
