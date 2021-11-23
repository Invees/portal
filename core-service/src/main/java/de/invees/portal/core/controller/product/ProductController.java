package de.invees.portal.core.controller.product;

import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class ProductController {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public ProductController() {
    get("/product/", this::list);
    get("/product/:product/", this::getProduct);
  }

  private Object getProduct(Request req, Response res) {
    return GsonUtils.GSON.toJson(product(req.params("product")));
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
