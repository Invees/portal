package de.invees.portal.common.datasource.mongodb;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import de.invees.portal.common.exception.MissingUserException;
import de.invees.portal.common.exception.UserCreationException;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.model.user.permission.Permission;
import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.user.DisplayUser;
import lombok.Getter;
import org.bson.Document;
import org.bson.conversions.Bson;

import java.util.List;
import java.util.UUID;

public class UserDataSource implements DataSource<User> {

  @Getter
  private MongoCollection<Document> collection;
  @Getter
  private MongoCollection<Document> sequenceCollection;

  @Override
  public void init(MongoCollection<Document> collection, MongoCollection<Document> sequenceCollection) {
    this.collection = collection;
    this.sequenceCollection = sequenceCollection;
    this.createIndex(User.EMAIL, true);
    this.createIndex(User.NAME, true);
  }

  @Override
  public Bson listFilter() {
    return null;
  }

  public void createDisplayUser(DisplayUser user) {
    if (byEmail(user.getEmail(), User.class) != null) {
      throw new UserCreationException("EMAIL_TAKEN");
    }
    if (byName(user.getName(), User.class) != null) {
      throw new UserCreationException("NAME_TAKEN");
    }
    this.collection.insertOne(this.map(user));
  }

  public <Y extends Model> Y byName(String name, Class<Y> type) {
    return wrapped(
        this.collection.find(Filters.eq(User.NAME, name)),
        type
    )
        .first();
  }

  public <Y extends Model> Y byEmail(String email, Class<Y> type) {
    return wrapped(
        this.collection.find(Filters.eq(User.EMAIL, email)),
        type
    )
        .first();
  }

  public void addPermission(UUID uniqueId, String name, String context) {
    User user = byId(uniqueId.toString(), User.class);
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
    User user = byId(uniqueId.toString(), User.class);
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