package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.user.UserAuthentication;
import de.invees.portal.common.model.user.UserAuthenticationType;
import lombok.Getter;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class UserAuthenticationDataSource implements DataSource {

  @Getter
  private MongoCollection<Document> collection;

  @Override
  public void init(MongoCollection<Document> collection) {
    this.collection = collection;
  }

  public void create(UserAuthentication userAuthentication) {
    this.collection.insertOne(this.map(userAuthentication));
  }

  public List<UserAuthentication> getAuthenticationForUser(UUID userId, UserAuthenticationType type) {
    return this.collection.find(
            Filters.and(
                Filters.eq(UserAuthentication.USER_ID, userId.toString()),
                Filters.eq(UserAuthentication.TYPE, type.toString())
            )
        )
        .projection(Projections.include(UserAuthentication.projection()))
        .map(document -> this.map(document, UserAuthentication.class))
        .into(new ArrayList<>());
  }

  public UserAuthentication getAuthentication(String token) {
    return this.collection.find(
            Filters.and(
                Filters.eq(UserAuthentication.TYPE, UserAuthenticationType.TOKEN.toString()),
                Filters.eq(UserAuthentication.DATA + "." + "token", token)
            ))
        .projection(Projections.include(UserAuthentication.projection()))
        .map(document -> this.map(document, UserAuthentication.class))
        .first();
  }
}
