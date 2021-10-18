package de.invees.portal.core.utils.controller;

import de.invees.portal.common.datasource.DataSource;
import de.invees.portal.common.model.Model;
import de.invees.portal.core.utils.input.InputUtils;
import org.bson.conversions.Bson;
import spark.Request;

public class ControllerUtils {

  public static <Y extends Model> Object list(DataSource dataSource, Request req, Class<Y> type) {
    if (hasLimitOrSkip(req)) {
      int skip = InputUtils.integerByString(req.queryParams("skip"), 0);
      int limit = InputUtils.integerByString(req.queryParams("limit"), 10);
      return dataSource.listPaged(skip, limit, type);
    }
    return dataSource.list(type);
  }

  public static <Y extends Model> Object list(DataSource dataSource, Request req, Class<Y> type, Bson filter) {
    if (hasLimitOrSkip(req)) {
      int skip = InputUtils.integerByString(req.queryParams("skip"), 0);
      int limit = InputUtils.integerByString(req.queryParams("limit"), 10);
      return dataSource.listPaged(skip, limit, type, filter);
    }
    return dataSource.list(type, filter);
  }

  private static boolean hasLimitOrSkip(Request req) {
    return !InputUtils.isEmpty(req.queryParams("skip")) || !InputUtils.isEmpty(req.queryParams("limit"));
  }
}
