package de.invees.portal.core.controller.v1.product;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class ProductController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public ProductController() {
    get("/v1/product/", this::list);
    get("/v1/product/:product/", this::getProduct);
  }

  private Object getProduct(Request req, Response res) {
    return GsonUtils.GSON.toJson(resource(
        productDataSourceV1(),
        req.params("product"),
        ProductV1.class,
        false
    ));
  }

  public Object list(Request req, Response resp) {
    return GsonUtils.GSON.toJson(productDataSourceV1().list(ProductV1.class));
  }

}
