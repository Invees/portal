package de.invees.portal.common.datasource;

import com.mongodb.client.MongoCollection;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.utils.gson.GsonUtils;
import org.bson.Document;

public interface DataSource {

  MongoCollection getCollection();

  void init(MongoCollection<Document> collection);

  default Document map(Object object) {
    return Document.parse(GsonUtils.toJson(object));
  }

  default <T extends Model> T map(Document document, Class<T> target) {
    return GsonUtils.GSON.fromJson(document.toJson(), target);
  }

}
