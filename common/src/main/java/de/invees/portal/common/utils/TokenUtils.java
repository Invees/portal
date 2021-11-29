package de.invees.portal.common.utils;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.UserAuthenticationDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.UserDataSourceV1;
import de.invees.portal.common.model.v1.user.UserAuthenticationV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.provider.ProviderRegistry;

public class TokenUtils {

  public static UserV1 parseToken(String parsedToken, String address) {
    UserAuthenticationV1 authentication = userAuthenticationDataSource().getAuthentication(
        parsedToken,
        UserAuthenticationV1.class
    );
    if (authentication == null) {
      return null;
    }
    if (address != null) {
      if (!authentication.getData().get("address").equals(address)) {
        return null;
      }
    }
    return userDataSource().byId(authentication.getUser().toString(), UserV1.class);
  }

  private static UserDataSourceV1 userDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(UserDataSourceV1.class);
  }

  private static UserAuthenticationDataSourceV1 userAuthenticationDataSource() {
    return ProviderRegistry.access(DataSourceProvider.class).access(UserAuthenticationDataSourceV1.class);
  }

}
