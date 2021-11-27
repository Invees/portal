package de.invees.portal.core.controller.v1.section;

import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.datasource.mongodb.v1.ProductDataSourceV1;
import de.invees.portal.common.datasource.mongodb.v1.SectionDataSourceV1;
import de.invees.portal.common.model.v1.section.SectionV1;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class SectionController {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);

  public SectionController() {
    get("/v1/section/", this::list);
    get("/v1/section/:section/", this::getSection);
    get("/v1/section/:section/product/", this::listForSection);
  }

  public Object list(Request req, Response resp) {
    return GsonUtils.GSON.toJson(sectionDataSource().list(SectionV1.class));
  }

  public Object getSection(Request req, Response resp) {
    return GsonUtils.GSON.toJson(section(req.params("section")));
  }

  private Object listForSection(Request req, Response resp) {
    SectionV1 section = section(req.params("section"));
    if (section == null) {
      throw new InputException("INVALID_SECTION");
    }
    return GsonUtils.GSON.toJson(
        productDataSource().listForSection(section(req.params("section")).getId(), ProductV1.class)
    );
  }

  private SectionV1 section(String section) {
    return sectionDataSource().byId(section, SectionV1.class);
  }

  private SectionDataSourceV1 sectionDataSource() {
    return connection.get().access(SectionDataSourceV1.class);
  }

  private ProductDataSourceV1 productDataSource() {
    return connection.get().access(ProductDataSourceV1.class);
  }
}
