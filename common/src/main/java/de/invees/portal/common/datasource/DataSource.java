package de.invees.portal.common.datasource;

import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.IndexOptions;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Projections;
import de.invees.portal.common.datasource.response.PagedResponse;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.utils.gson.GsonUtils;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.ArrayList;
import java.util.List;

public interface DataSource<T> {

  MongoCollection<Document> getCollection();

  MongoCollection<Document> getSequenceCollection();

  void init(MongoCollection<Document> collection, MongoCollection<Document> counterCollection);

  Bson listFilter();

  default <Y extends Model> MongoIterable<Y> wrapped(FindIterable<Document> iterable, Class<Y> type) {
    try {
      return iterable.projection(
              Projections.include((String[]) type.getDeclaredMethod("projection").invoke(null))
          )
          .map(document -> this.map(document, type));
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  default <Y extends Model> Y byId(Object id, Class<Y> type) {
    return wrapped(getCollection().find(Filters.eq("_id", id)), type)
        .first();
  }

  default <Y extends Model> List<Y> list(Class<Y> type) {
    return wrapped(getCollection().find(listFilter()), type)
        .into(new ArrayList<>());
  }

  default <Y extends Model> PagedResponse<Y> listPaged(int skip, int limit, Class<Y> type) {
    return new PagedResponse(
        this.getCollection().countDocuments(listFilter()),
        wrapped(this.getCollection().find()
            .skip(skip)
            .limit(limit), type)
            .into(new ArrayList<>())
    );
  }

  default Document map(Object object) {
    return Document.parse(GsonUtils.toJson(object));
  }

  default <T extends Model> T map(Document document, Class<T> target) {
    return GsonUtils.GSON.fromJson(
        document.toJson(JsonWriterSettings.builder().outputMode(JsonMode.RELAXED).build()),
        target
    );
  }

  default void create(T object) {
    this.getCollection().insertOne(this.map(object));
  }

  default int nextSequence() {
    BasicDBObject find = new BasicDBObject();
    find.put("_id", this.getCollection().getNamespace().getCollectionName());
    BasicDBObject update = new BasicDBObject();
    update.put("$inc", new BasicDBObject("seq", 1));
    Document obj = getSequenceCollection().findOneAndUpdate(find, update);
    return obj.get("seq", Integer.class);
  }

  default void createSequence() {
    Document document = this.getSequenceCollection().find(Filters.eq(
        "_id", this.getCollection().getNamespace().getCollectionName()
    )).first();
    if (document == null) {
      document = new Document()
          .append("_id", this.getCollection().getNamespace().getCollectionName())
          .append("seq", 1);
      this.getSequenceCollection().insertOne(document);
    }
  }

  default void createIndex(String field, boolean unique) {
    IndexOptions indexOptions = new IndexOptions().unique(unique);
    getCollection().createIndex(Indexes.ascending("name", "stars"), indexOptions);
  }

}