package de.invees.portal.core.utils.controller;

import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.*;
import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.gateway.paypal.PayPalGatewayProvider;
import de.invees.portal.common.model.Model;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.InputUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.utils.provider.ProviderRegistry;
import de.invees.portal.core.service.ServiceProvider;
import de.invees.portal.core.utils.CoreTokenUtils;
import org.bson.conversions.Bson;
import spark.Request;

import java.util.UUID;

public class Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public <Y extends Model> Object list(DataSource dataSource, Request req, Class<Y> type) {
    return list(dataSource, req, null);
  }

  public <Y extends Model> Object list(DataSource dataSource, Request req, Class<Y> type, Bson filter) {
    return list(dataSource, req, type, filter, null);
  }

  public <Y extends Model> Object list(DataSource dataSource, Request req, Class<Y> type, Bson filter, Bson sort) {
    if (hasLimitOrSkip(req)) {
      long skip = InputUtils.longByString(req.queryParams("skip"), 0);
      long limit = InputUtils.longByString(req.queryParams("limit"), 10);
      return dataSource.listPaged(skip, limit, type, filter, sort);
    }
    return dataSource.list(type, filter);
  }

  public boolean hasLimitOrSkip(Request req) {
    return !InputUtils.isEmpty(req.queryParams("skip")) || !InputUtils.isEmpty(req.queryParams("limit"));
  }

  public boolean isSameUser(Request req, UUID userId) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      return false;
    }
    return userId.equals(user.getId());
  }

  protected <T extends Model> T resource(DataSource<?> dataSource, String id, Class<T> type, boolean numericId) {
    T model;
    if (numericId) {
      model = dataSource.byId(InputUtils.longByString(id, -1), type);
    } else {
      model = dataSource.byId(id, type);
    }
    if (model == null) {
      throw new InputException("RESOURCE_NOT_FOUND");
    }
    return model;
  }

  // DataSource
  protected ContractDataSourceV1 contractDataSourceV1() {
    return this.connection.get().access(ContractDataSourceV1.class);
  }

  protected ContractCancellationDataSourceV1 contractCancellationDataSourceV1() {
    return this.connection.get().access(ContractCancellationDataSourceV1.class);
  }

  protected InvoiceDataSourceV1 invoiceDataSourceV1() {
    return this.connection.get().access(InvoiceDataSourceV1.class);
  }

  protected InvoiceFileDataSourceV1 invoiceFileDataSourceV1() {
    return this.connection.get().access(InvoiceFileDataSourceV1.class);
  }

  protected GatewayDataDataSourceV1 gatewayDataSourceV1() {
    return this.connection.get().access(GatewayDataDataSourceV1.class);
  }

  protected PayPalGatewayProvider payPalGateway() {
    return ProviderRegistry.access(PayPalGatewayProvider.class);
  }

  protected ProductDataSourceV1 productDataSourceV1() {
    return this.connection.get().access(ProductDataSourceV1.class);
  }

  protected ServiceDataSourceV1 serviceDataSourceV1() {
    return this.connection.get().access(ServiceDataSourceV1.class);
  }

  protected ServiceProvider serviceProvider() {
    return ProviderRegistry.access(ServiceProvider.class);
  }

  protected NetworkAddressDataSourceV1 networkAddressDataSourceV1() {
    return this.connection.get().access(NetworkAddressDataSourceV1.class);
  }

  protected SectionDataSourceV1 sectionDataSourceV1() {
    return connection.get().access(SectionDataSourceV1.class);
  }

  protected UserDataSourceV1 userDataSourceV1() {
    return connection.get().access(UserDataSourceV1.class);
  }

  protected UserAuthenticationDataSourceV1 userAuthenticationDataSourceV1() {
    return connection.get().access(UserAuthenticationDataSourceV1.class);
  }
}
