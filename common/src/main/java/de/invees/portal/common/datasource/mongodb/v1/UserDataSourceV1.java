package de.invees.portal.common.datasource.mongodb.v1;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.exception.UserCreationException;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.model.v1.user.UserDetailsV1;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

public class UserDataSourceV1 implements DataSource<UserV1> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public String getName() {
    return "UserDataSource";
  }

  @Override
  public void init(MongoCollection<Document> collection, MongoCollection<Document> sequenceCollection) {
    this.collection = collection;
    this.sequenceCollection = sequenceCollection;
    this.createIndex(UserV1.EMAIL, true);
    this.createIndex(UserV1.NAME, true);
  }

  @Override
  public Bson listFilter() {
    return null;
  }

  public void createDisplayUser(UserDetailsV1 user) {
    if (byEmail(user.getEmail(), UserV1.class) != null) {
      throw new UserCreationException("EMAIL_TAKEN");
    }
    if (byName(user.getName(), UserV1.class) != null) {
      throw new UserCreationException("NAME_TAKEN");
    }
    this.collection.insertOne(this.map(user));
  }

  public <Y extends Model> Y byName(String name, Class<Y> type) {
    return wrapped(
        this.collection.find(Filters.eq(UserV1.NAME, name)),
        type
    )
        .first();
  }

  public <Y extends Model> Y byEmail(String email, Class<Y> type) {
    return wrapped(
        this.collection.find(Filters.eq(UserV1.EMAIL, email)),
        type
    )
        .first();
  }

}
