package de.invees.portal.core.controller.v1.product;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ProductDataSourceV1;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class ProductController {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public ProductController() {
    get("/v1/product/", this::list);
    get("/v1/product/:product/", this::getProduct);
  }

  private Object getProduct(Request req, Response res) {
    return GsonUtils.GSON.toJson(product(req.params("product")));
  }

  public Object list(Request req, Response resp) {
    return GsonUtils.GSON.toJson(productDataSource().list(ProductV1.class));
  }

  private ProductV1 product(String product) {
    try {
      return productDataSource().byId(product, ProductV1.class);
    } catch (Exception e) {
      return productDataSource().byId(product, ProductV1.class);
    }
  }

  private ProductDataSourceV1 productDataSource() {
    return connection.get().access(ProductDataSourceV1.class);
  }
}
