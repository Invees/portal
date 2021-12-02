package de.invees.portal.core.controller.v1.service.software;

import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.SoftwareDataSourceV1;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.v1.service.software.ServiceSoftwareV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.core.utils.CoreTokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class ServiceSoftwareController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public ServiceSoftwareController() {
    get("/v1/service/software/", this::list);
  }

  public Object list(Request req, Response resp) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        list(
            softwareDataSource(),
            req,
            ServiceSoftwareV1.class,
            Filters.or(
                Filters.eq(ServiceSoftwareV1.BELONGS_TO, user.getId().toString()),
                Filters.eq(ServiceSoftwareV1.BELONGS_TO, null)
            )
        )
    );
  }

  private SoftwareDataSourceV1 softwareDataSource() {
    return this.connection.get().access(SoftwareDataSourceV1.class);
  }

}
