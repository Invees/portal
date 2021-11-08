package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.user.UserAuthentication;
import de.invees.portal.common.model.user.UserAuthenticationType;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserAuthenticationDataSource implements DataSource<UserAuthentication> {

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
    return null;
  }

  public <Y extends Model> List<Y> byUser(UUID userId, UserAuthenticationType authenticationType, Class<Y> type) {
    return wrapped(
        collection.find(Filters.and(
            Filters.eq(UserAuthentication.USER, userId.toString()),
            Filters.eq(UserAuthentication.TYPE, authenticationType.toString())
        )),
        type
    )
        .into(new ArrayList<>());
  }

  public <Y extends Model> Y getAuthentication(String token, Class<Y> type) {
    return wrapped(
        collection.find(Filters.and(
            Filters.eq(UserAuthentication.TYPE, UserAuthenticationType.TOKEN.toString()),
            Filters.eq(UserAuthentication.DATA + "." + "token", token)
        )),
        type
    )
        .first();
  }
}
