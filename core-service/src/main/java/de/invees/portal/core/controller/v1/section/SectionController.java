package de.invees.portal.core.controller.v1.section;

import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.model.v1.product.ProductV1;
import de.invees.portal.common.model.v1.section.SectionV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.core.utils.controller.Controller;
import spark.Request;
import spark.Response;

import static spark.Spark.get;

public class SectionController extends Controller {

  public SectionController() {
    get("/v1/section/", this::list);
    get("/v1/section/:section/", this::getSection);
    get("/v1/section/:section/product/", this::listForSection);
  }

  public Object list(Request req, Response resp) {
    return GsonUtils.GSON.toJson(sectionDataSourceV1().list(SectionV1.class));
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
        productDataSourceV1().listForSection(section(req.params("section")).getId(), ProductV1.class)
    );
  }

  private SectionV1 section(String section) {
    return sectionDataSourceV1().byId(section, SectionV1.class);
  }

}
