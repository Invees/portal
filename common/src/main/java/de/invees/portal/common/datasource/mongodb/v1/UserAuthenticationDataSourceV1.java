package de.invees.portal.common.datasource.mongodb.v1;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.user.UserAuthenticationV1;
import de.invees.portal.common.model.v1.user.UserAuthenticationTypeV1;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserAuthenticationDataSourceV1 implements DataSource<UserAuthenticationV1> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public String getName() {
    return "UserAuthenticationDataSource";
  }

  @Override
  public void init(MongoCollection<Document> collection, MongoCollection<Document> sequenceCollection) {
    this.collection = collection;
    this.sequenceCollection = sequenceCollection;
  }

  @Override
  public Bson listFilter() {
    return null;
  }

  public <Y extends Model> List<Y> byUser(UUID userId, UserAuthenticationTypeV1 authenticationType, Class<Y> type) {
    return wrapped(
        collection.find(Filters.and(
            Filters.eq(UserAuthenticationV1.USER, userId.toString()),
            Filters.eq(UserAuthenticationV1.TYPE, authenticationType.toString())
        )),
        type
    )
        .into(new ArrayList<>());
  }

  public <Y extends Model> Y getAuthentication(String token, Class<Y> type) {
    return wrapped(
        collection.find(Filters.and(
            Filters.eq(UserAuthenticationV1.TYPE, UserAuthenticationTypeV1.TOKEN.toString()),
            Filters.eq(UserAuthenticationV1.DATA + "." + "token", token)
        )),
        type
    )
        .first();
  }
}
