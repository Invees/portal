package de.invees.portal.core.utils;

import de.invees.portal.common.utils.service.ServiceRegistry;
import de.invees.portal.common.datasource.ConnectionService;
import de.invees.portal.common.datasource.mongodb.UserAuthenticationDataSource;
import de.invees.portal.common.datasource.mongodb.UserDataSource;
import de.invees.portal.common.model.user.User;
import de.invees.portal.common.model.user.UserAuthentication;
import spark.Request;

import java.security.SecureRandom;

public class TokenUtils {

  public static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  public static final int SECURE_TOKEN_LENGTH = 512;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final char[] SYMBOLS = CHARACTERS.toCharArray();
  private static final char[] BUF = new char[SECURE_TOKEN_LENGTH];

  public static String nextToken() {
    for (int idx = 0; idx < BUF.length; ++idx)
      BUF[idx] = SYMBOLS[RANDOM.nextInt(SYMBOLS.length)];
    return new String(BUF);
  }

  public static User parseToken(Request request) {
    String token = request.headers("Authorization");
    if (token == null || token.equalsIgnoreCase("") || token.equalsIgnoreCase("null")) {
      return null;
    }
    String parsedToken = token.substring(7);
    if (parsedToken == null || parsedToken.equalsIgnoreCase("") || token.equalsIgnoreCase("null")) {
      return null;
    }
    UserAuthentication authentication = userAuthenticationDataSource().getAuthentication(parsedToken);
    if (authentication == null) {
      return null;
    }
    if (!authentication.getData().get("address").equals(request.ip())) {
      return null;
    }
    return userDataSource().getUser(authentication.getUserId());
  }

  private static UserDataSource userDataSource() {
    return ServiceRegistry.access(ConnectionService.class).access(UserDataSource.class);
  }

  private static UserAuthenticationDataSource userAuthenticationDataSource() {
    return ServiceRegistry.access(ConnectionService.class).access(UserAuthenticationDataSource.class);
  }
}
