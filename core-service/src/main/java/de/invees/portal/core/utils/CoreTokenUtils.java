package de.invees.portal.core.utils;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.UserAuthenticationDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.UserDataSourceV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.TokenUtils;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import spark.Request;

import java.security.SecureRandom;

public class CoreTokenUtils {

  public static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
  public static final int SECURE_TOKEN_LENGTH = 512;
  private static final SecureRandom RANDOM = new SecureRandom();
  private static final char[] SYMBOLS = CHARACTERS.toCharArray();
  private static final char[] BUF = new char[SECURE_TOKEN_LENGTH];

  public static String nextToken() {
    for (int idx = 0; idx < BUF.length; ++idx) {
      BUF[idx] = SYMBOLS[RANDOM.nextInt(SYMBOLS.length)];
    }
    return new String(BUF);
  }

  public static UserV1 parseToken(Request request) {
    String token = request.headers("Authorization");
    if (token == null || token.equalsIgnoreCase("") || token.equalsIgnoreCase("null")) {
      return null;
    }
    String parsedToken = token.substring(7);
    if (parsedToken == null || parsedToken.equalsIgnoreCase("") || token.equalsIgnoreCase("null")) {
      return null;
    }
    return TokenUtils.parseToken(parsedToken, request.ip());
  }

  private static UserDataSourceV1 userDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(UserDataSourceV1.class);
  }

  private static UserAuthenticationDataSourceV1 userAuthenticationDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(UserAuthenticationDataSourceV1.class);
  }
}
