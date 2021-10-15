package de.invees.portal.core.controller.section;

import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.service.LazyLoad;
import de.invees.portal.common.datasource.ConnectionService;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.datasource.mongodb.SectionDataSource;
import de.invees.portal.common.model.section.Section;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class SectionController {

  private final LazyLoad<ConnectionService> connection = new LazyLoad<>(ConnectionService.class);

  public SectionController() {
    get("/section/", this::getSections);
    get("/section/:sectionId/", this::getSection);
    get("/section/:sectionId/product/", this::getProductsForSection);
    get("/section/:sectionId/product/:productId/", this::getProduct);
  }

  private Object getProduct(Request req, Response res) {
    Section section = section(req.params("section"));
    Product product = product(req.params("product"));
    if (!product.getSectionId().equals(section.getId())) {
      return null;
    }
    return GsonUtils.GSON.toJson(product);
  }

  public Object getSections(Request req, Response resp) {
    return GsonUtils.GSON.toJson(sectionDataSource().getSections());
  }

  public Object getSection(Request req, Response resp) {
    return GsonUtils.GSON.toJson(section(req.params("sectionId")));
  }

  private Object getProductsForSection(Request req, Response resp) {
    return GsonUtils.GSON.toJson(
        productDataSource().getProductsForSection(section(req.params("sectionId")).getId())
    );
  }

  private Section section(String section) {
    try {
      return sectionDataSource().getSection(section);
    } catch (Exception e) {
      return sectionDataSource().getSection(section);
    }
  }

  private Product product(String product) {
    try {
      return productDataSource().getProduct(product);
    } catch (Exception e) {
      return productDataSource().getProduct(product);
    }
  }

  private SectionDataSource sectionDataSource() {
    return connection.get().access(SectionDataSource.class);
  }

  private ProductDataSource productDataSource() {
    return connection.get().access(ProductDataSource.class);
  }
}
