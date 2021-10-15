package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import de.invees.portal.common.exception.MissingUserException;
import de.invees.portal.common.exception.UserCreationException;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.model.user.permission.Permission;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.user.DisplayUser;
import lombok.Getter;
import org.bson.Document;

import java.util.List;
import java.util.UUID;

public class UserDataSource implements DataSource {

  @Getter
  private MongoCollection<Document> collection;

  @Override
  public void init(MongoCollection<Document> collection) {
    this.collection = collection;
  }

  public void create(DisplayUser user) {
    if (getUserByEmail(user.getEmail()) != null) {
      throw new UserCreationException("EMAIL_TAKEN");
    }
    if (getUserByName(user.getName()) != null) {
      throw new UserCreationException("NAME_TAKEN");
    }
    this.collection.insertOne(this.map(user));
  }

  public User getUserByName(String name) {
    return this.collection.find(Filters.eq(User.NAME, name))
        .projection(Projections.include(User.projection()))
        .map(document -> this.map(document, User.class))
        .first();
  }

  public User getUserByEmail(String email) {
    return this.collection.find(Filters.eq(User.EMAIL, email))
        .projection(Projections.include(User.projection()))
        .map(document -> this.map(document, User.class))
        .first();
  }

  public User getUser(UUID uniqueId) {
    return this.collection.find(Filters.eq(User.ID, uniqueId.toString()))
        .projection(Projections.include(User.projection()))
        .map(document -> this.map(document, User.class))
        .first();
  }

  public void addPermission(UUID uniqueId, String name, String context) {
    User user = getUser(uniqueId);
    if (user == null) {
      throw new MissingUserException("MISSING_USER");
    }
    Permission permission = user.getPermission(name);
    if (permission == null) {
      permission = new Permission(name, List.of(context));
      user.getPermissions().add(permission);
    } else {
      permission.getContext().add(context);
    }
    this.collection.replaceOne(Filters.eq(User.ID, user.getId()), this.map(user));
  }

  public void deletePermission(UUID uniqueId, String name, String context) {
    User user = getUser(uniqueId);
    if (user == null) {
      throw new MissingUserException("MISSING_USER");
    }
    Permission permission = user.getPermission(name);
    if (permission == null) {
      return;
    }
    if (context == null) {
      user.getPermissions().remove(permission);
    } else {
      permission.getContext().remove(context);
    }
    this.collection.replaceOne(Filters.eq(User.ID, user.getId()), this.map(user));
  }

}
