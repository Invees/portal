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
    get("/section/:section/", this::getSection);
    get("/section/:section/product/", this::listForSection);
  }

  public Object list(Request req, Response resp) {
    return GsonUtils.GSON.toJson(sectionDataSource().list(Section.class));
  }

  public Object getSection(Request req, Response resp) {
    return GsonUtils.GSON.toJson(section(req.params("section")));
  }

  private Object listForSection(Request req, Response resp) {
    Section section = section(req.params("section"));
    if (section == null) {
      throw new InputException("INVALID_SECTION");
    }
    return GsonUtils.GSON.toJson(
        productDataSource().listForSection(section(req.params("section")).getId(), Product.class)
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
