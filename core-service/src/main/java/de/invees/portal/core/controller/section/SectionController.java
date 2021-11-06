package de.invees.portal.core.controller.section;

import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.model.product.Product;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.ProductDataSource;
import de.invees.portal.common.datasource.mongodb.SectionDataSource;
import de.invees.portal.common.model.section.Section;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class SectionController {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public SectionController() {
    get("/section/", this::list);
    get("/section/:sectionId/", this::byId);
    get("/section/:sectionId/product/", this::listForSection);
  }

  public Object list(Request req, Response resp) {
    return GsonUtils.GSON.toJson(sectionDataSource().list(Section.class));
  }

  public Object byId(Request req, Response resp) {
    return GsonUtils.GSON.toJson(section(req.params("sectionId")));
  }

  private Object listForSection(Request req, Response resp) {
    Section section = section(req.params("sectionId"));
    if (section == null) {
      throw new InputException("INVALID_SECTION");
    }
    return GsonUtils.GSON.toJson(
        productDataSource().listForSection(section(req.params("sectionId")).getId(), Product.class)
    );
  }

  private Section section(String section) {
    return sectionDataSource().byId(section, Section.class);
  }

  private SectionDataSource sectionDataSource() {
    return connection.get().access(SectionDataSource.class);
  }

  private ProductDataSource productDataSource() {
    return connection.get().access(ProductDataSource.class);
  }
}
