package de.invees.portal.core.controller.v1.service.software;

import com.google.gson.JsonObject;
import com.itextpdf.io.util.FileUtil;
import com.mongodb.client.model.Filters;
import de.invees.portal.common.datasource.DataSourceProvider;
import de.invees.portal.common.exception.InputException;
import de.invees.portal.common.exception.UnauthorizedException;
import de.invees.portal.common.model.v1.service.ServiceTypeV1;
import de.invees.portal.common.model.v1.service.software.ServiceSoftwareTypeV1;
import de.invees.portal.common.model.v1.service.software.ServiceSoftwareV1;
import de.invees.portal.common.model.v1.user.UserV1;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.common.utils.provider.LazyLoad;
import de.invees.portal.core.configuration.Configuration;
import de.invees.portal.core.utils.CoreTokenUtils;
import de.invees.portal.core.utils.controller.Controller;
import org.bson.BsonNull;
import spark.Request;
import spark.Response;

import javax.servlet.MultipartConfigElement;
import javax.servlet.http.Part;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

import static spark.Spark.*;

public class ServiceSoftwareController extends Controller {

  private final LazyLoad<DataSourceProvider> connection = new LazyLoad<>(DataSourceProvider.class);
  private final Configuration configuration;

  public ServiceSoftwareController(Configuration configuration) {
    this.configuration = configuration;
    get("/v1/software/", this::list);
    post("/v1/software/", this::create);
    delete("/v1/software/:software/", this::deleteSoftware);
  }

  public Object deleteSoftware(Request req, Response resp) {
    ServiceSoftwareV1 software = softwareDataSourceV1().byId(req.params("software"), ServiceSoftwareV1.class);
    if (!isSameUser(req, software.getBelongsTo())) {
      throw new UnauthorizedException();
    }
    File directory = new File(configuration.getSoftwareDirectory(), "template/iso");
    softwareDataSourceV1().getCollection()
        .deleteOne(Filters.eq(ServiceSoftwareV1.ID, software.getId().toString()));
    if (software.getName().endsWith(".iso")) {
      File iso = new File(directory.getAbsolutePath() + "/" + software.getId() + ".iso");
      FileUtil.deleteFile(iso);
    }
    return new JsonObject().toString();
  }

  public Object create(Request req, Response resp) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException();
    }
    UUID softwareId = UUID.randomUUID();
    try {
      File directory = new File(configuration.getSoftwareDirectory(), "template/iso");
      MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
          directory.getAbsolutePath(),
          4 * 1024 * 1024 * 1024,
          5 * 1024 * 1024 * 1024,
          1024 * 8
      );
      req.raw().setAttribute("org.eclipse.jetty.multipartConfig", multipartConfigElement);
      Part uploadedFile = req.raw().getPart("file");
      String name = uploadedFile.getSubmittedFileName();

      // ISO = Mountable / Virtual Server
      if (name.endsWith(".iso")) {
        Path out = Paths.get(directory.getAbsolutePath() + "/" + softwareId + ".iso");
        try (InputStream in = uploadedFile.getInputStream()) {
          Files.copy(in, out);
          uploadedFile.delete();
        }
        ServiceSoftwareV1 software = new ServiceSoftwareV1(
            softwareId,
            name,
            ServiceTypeV1.VIRTUAL_SERVER,
            ServiceSoftwareTypeV1.MOUNTABLE,
            user.getId(),
            System.currentTimeMillis()
        );
        softwareDataSourceV1().create(software);
        return GsonUtils.toJson(software);
      }
    } catch (Exception e) {
      throw new RuntimeException(e);
    }

    throw new InputException("INVALID_FILE_TYPE");
  }

  public Object list(Request req, Response resp) {
    UserV1 user = CoreTokenUtils.parseToken(req);
    if (user == null) {
      throw new UnauthorizedException();
    }
    return GsonUtils.GSON.toJson(
        list(
            softwareDataSourceV1(),
            req,
            ServiceSoftwareV1.class,
            Filters.or(
                Filters.eq(ServiceSoftwareV1.BELONGS_TO, user.getId().toString()),
                Filters.eq(ServiceSoftwareV1.BELONGS_TO, BsonNull.VALUE)
            )
        )
    );
  }
}
