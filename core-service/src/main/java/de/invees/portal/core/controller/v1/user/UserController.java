package de.invees.portal.core.controller.v1.user;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.exception.UserCreationException;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.model.v1.user.UserAuthenticationV1;
import de.invees.portal.common.model.v1.user.UserAuthenticationTypeV1;
import de.invees.portal.common.model.v1.user.UserDetailsV1;
import de.invees.portal.common.utils.InputUtils;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.security.SecurityUtils;
import de.invees.portal.core.utils.CoreTokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static spark.Spark.get;
import static spark.Spark.post;

public class UserController extends Controller {

  public UserController() {
    get("/v1/user/", this::localUser);
    post("/v1/user/", this::register);
    post("/v1/user/authenticate/", this::authenticate);
  }

  public Object localUser(Request req, Response resp) {
    return GsonUtils.GSON.toJson(
        CoreTokenUtils.parseToken(req)
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

    UserDetailsV1 user = GsonUtils.GSON.fromJson(body.get("user"), UserDetailsV1.class);
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

    userDataSourceV1().createDisplayUser(user);
    userAuthenticationDataSourceV1().create(new UserAuthenticationV1(
        UUID.randomUUID(),
        user.getId(),
        UserAuthenticationTypeV1.PASSWORD,
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
    UserV1 user = userDataSourceV1().byEmail(email, UserV1.class);
    if (user == null) {
      throw new UnauthorizedException("INVALID_USER_PASSWORD");
    }

    List<UserAuthenticationV1> authentications = userAuthenticationDataSourceV1().byUser(
        user.getId(),
        UserAuthenticationTypeV1.PASSWORD,
        UserAuthenticationV1.class
    );
    for (UserAuthenticationV1 authentication : authentications) {
      String hashedPassword = SecurityUtils.hash(password, (String) authentication.getData().get("salt"), user.getId());
      if (authentication.getData().get("password").equals(hashedPassword)) {
        String token = CoreTokenUtils.nextToken();
        long expiryTime = System.currentTimeMillis() + (1000 * 60 * 60 * 24);
        UserAuthenticationV1 tokenAuthentication = new UserAuthenticationV1(
            UUID.randomUUID(),
            user.getId(),
            UserAuthenticationTypeV1.TOKEN,
            Map.of(
                "token", token,
                "address", req.ip(),
                "expiryTime", expiryTime
            )
        );
        userAuthenticationDataSourceV1().create(tokenAuthentication);
        return token;
      }
    }
    throw new UnauthorizedException("INVALID_USER_PASSWORD");
  }
}
