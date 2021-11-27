package de.invees.portal.core.controller.v1.user;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.UserAuthenticationDataSource;
import de.invees.portal.common.datasource.mongodb.UserDataSource;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.exception.UserCreationException;
import de.invees.portal.common.model.v1.user.User;
import de.invees.portal.common.model.v1.user.UserAuthentication;
import de.invees.portal.common.model.v1.user.UserAuthenticationType;
import de.invees.portal.common.model.v1.user.UserDetails;
import de.invees.portal.common.utils.InputUtils;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.utils.security.SecurityUtils;
import de.invees.portal.core.utils.TokenUtils;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class UserController {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public UserController() {
    get("/v1/user/", this::localUser);
    post("/v1/user/", this::register);
    post("/v1/user/authenticate/", this::authenticate);
  }

  public Object localUser(Request req, Response resp) {
    return GsonUtils.GSON.toJson(
        TokenUtils.parseToken(req)
    );
  }

  public Object register(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    if (body.get("user").getAsJsonObject().get("_id") != null
        && !body.get("user").getAsJsonObject().get("_id").isJsonNull()) {
      throw new UserCreationException("ID_MUST_BE_NULL");
    }

    body.get("user").getAsJsonObject().addProperty("_id", UUID.randomUUID().toString());

    String password = GsonUtils.GSON.fromJson(body.get("password"), String.class);
    if (InputUtils.isEmpty(password)) {
      throw new UserCreationException("PASSWORD_REQUIRED");
    }

    UserDetails user = GsonUtils.GSON.fromJson(body.get("user"), UserDetails.class);
    if (InputUtils.isEmpty(user.getName())) {
      throw new UserCreationException("MISSING_USER_NAME");
    }
    if (InputUtils.isInvalidEmailAddress((user.getEmail()))) {
      throw new UserCreationException("INVALID_EMAIL");
    }
    if (InputUtils.isEmpty((user.getFirstName()))) {
      throw new UserCreationException("MISSING_FIRSTNAME");
    }
    if (InputUtils.isEmpty((user.getLastName()))) {
      throw new UserCreationException("MISSING_LASTNAME");
    }
    if (InputUtils.isEmpty(user.getPostCode())) {
      throw new UserCreationException("MISSING_POSTCODE");
    }
    if (InputUtils.isEmpty((user.getCity()))) {
      throw new UserCreationException("MISSING_CITY");
    }
    if (InputUtils.isEmpty((user.getAddress()))) {
      throw new UserCreationException("MISSING_ADDRESS");
    }
    if (InputUtils.isEmpty((user.getCountry()))) {
      throw new UserCreationException("MISSING_COUNTRY");
    }
    String salt = SecurityUtils.generateSalt(40);

    userDataSource().createDisplayUser(user);
    userAuthenticationDataSource().create(new UserAuthentication(
        UUID.randomUUID(),
        user.getId(),
        UserAuthenticationType.PASSWORD,
        Map.of(
            "password", SecurityUtils.hash(password, salt, user.getId()),
            "salt", salt
        )
    ));

    return GsonUtils.GSON.toJson(user);
  }

  public Object authenticate(Request req, Response resp) {
    JsonObject body = JsonParser.parseString(req.body()).getAsJsonObject();
    if (body.get("email") == null || body.get("email").isJsonNull()) {
      throw new UnauthorizedException("INVALID_USER_PASSWORD");
    }
    if (body.get("password") == null || body.get("password").isJsonNull()) {
      throw new UnauthorizedException("INVALID_USER_PASSWORD");
    }

    String email = body.get("email").getAsString();
    String password = body.get("password").getAsString();
    User user = userDataSource().byEmail(email, User.class);
    if (user == null) {
      throw new UnauthorizedException("INVALID_USER_PASSWORD");
    }

    List<UserAuthentication> authentications = userAuthenticationDataSource().byUser(
        user.getId(),
        UserAuthenticationType.PASSWORD,
        UserAuthentication.class
    );
    for (UserAuthentication authentication : authentications) {
      String hashedPassword = SecurityUtils.hash(password, (String) authentication.getData().get("salt"), user.getId());
      if (authentication.getData().get("password").equals(hashedPassword)) {
        String token = TokenUtils.nextToken();
        long expiryTime = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
        UserAuthentication tokenAuthentication = new UserAuthentication(
            UUID.randomUUID(),
            user.getId(),
            UserAuthenticationType.TOKEN,
            Map.of(
                "token", token,
                "address", req.ip(),
                "expiryTime", expiryTime
            )
        );
        userAuthenticationDataSource().create(tokenAuthentication);
        return token;
      }
    }
    throw new UnauthorizedException("INVALID_USER_PASSWORD");
  }

  private UserDataSource userDataSource() {
    return connection.get().access(UserDataSource.class);
  }

  private UserAuthenticationDataSource userAuthenticationDataSource() {
    return connection.get().access(UserAuthenticationDataSource.class);
  }
}
