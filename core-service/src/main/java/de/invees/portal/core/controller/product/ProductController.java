package de.invees.portal.core.controller.product;

import de.invees.portal.common.datasource.ConnectionService;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.service.LazyLoad;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class ProductController {

  private final LazyLoad<ConnectionService> connection = new LazyLoad<>(ConnectionService.class);

  public ProductController() {
    get("/product/", this::list);
    get("/product/:productId/", this::byId);
  }

  private Object byId(Request req, Response res) {
    return GsonUtils.GSON.toJson(product(req.params("productId")));
  }

  public Object list(Request req, Response resp) {
    return GsonUtils.GSON.toJson(productDataSource().list(Product.class));
  }

  private Product product(String product) {
    try {
      return productDataSource().byId(product, Product.class);
    } catch (Exception e) {
      return productDataSource().byId(product, Product.class);
    }
  }

  private ProductDataSource productDataSource() {
    return connection.get().access(ProductDataSource.class);
  }
}
