package de.invees.portal.processing.worker.service.provider.proxmox;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.processing.worker.Application;
import de.invees.portal.processing.worker.configuration.ProxmoxConfiguration;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import static java.net.http.HttpClient.Version;

public class PveClient {

  public static class URL {
    public static final String LOGIN = "{%url%}/api2/json/access/ticket";
    public static final String QEMU = "{%url%}/api2/json/nodes/%0$s/qemu";
    public static final String NEXT_ID = "{%url%}/api2/json/cluster/nextid";
  }

  private final ProxmoxConfiguration configuration;
  private final HttpClient client;
  private String cookie = "";
  private String csrfToken = "";


  public PveClient(ProxmoxConfiguration configuration) {
    this.configuration = configuration;
    client = HttpClient.newBuilder()
        .sslContext(ignoreCert())
        .version(Version.HTTP_2)
        .build();
    JsonObject data = login(
        configuration.getUsername(),
        configuration.getPassword(),
        configuration.getRealm()
    );
    this.cookie = data.get("ticket").getAsString();
    this.csrfToken = data.get("CSRFPreventionToken").getAsString();

    new Thread(() -> {
      while (true) {
        try {
          JsonObject resolvedData = login(
              configuration.getUsername(),
              configuration.getPassword(),
              configuration.getRealm()
          );
          this.cookie = resolvedData.get("ticket").getAsString();
          this.csrfToken = resolvedData.get("CSRFPreventionToken").getAsString();

          if (this.cookie == null) {
            Application.LOGGER.error("Error while resolving token!");
          }
          Thread.sleep(60000);
        } catch (Exception e) {
          Application.LOGGER.error("", e);
        }
      }
    }).start();
  }

  public void createVirtualMachine(VirtualMachine machine) {
    post(URI.create(parse(URL.QEMU, "avalon")), GsonUtils.toJson(machine));
  }

  public int getNextId() {
    return get(URI.create(parse(URL.NEXT_ID)))
        .get("data")
        .getAsInt();
  }

  public JsonObject login(String username, String password, String realm) {
    return post(URI.create(parse(URL.LOGIN)), body(
        "username", username + "@" + realm,
        "password", password
    ))
        .getAsJsonObject("data");
  }

  private JsonObject get(URI uri) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(uri)
          .GET()
          .header("Cookie", "PVEAuthCookie=" + cookie)
          .header("CSRFPreventionToken", "61811B6F:eFG+ukeThein6Df7QHno6n1NNhzwLzDwrOQYyFhgUcA")
          .header("Content-Type", "application/json")
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return JsonParser.parseString(response.body()).getAsJsonObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  // Internal
  private JsonObject post(URI uri, String body) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(uri)
          .POST(HttpRequest.BodyPublishers.ofString(body))
          .header("Cookie", "PVEAuthCookie=" + cookie)
          .header("CSRFPreventionToken", csrfToken)
          .header("Content-Type", "application/json")
          .build();
      HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
      return JsonParser.parseString(response.body()).getAsJsonObject();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  private String body(String... parameter) {
    JsonObject body = new JsonObject();
    for (int index = 0; index < parameter.length; index++) {
      body.addProperty(parameter[index], parameter[++index]);
    }
    return body.toString();
  }

  private String parse(String url, String... replaces) {
    String fullUrl = url.replace("{%url%}", configuration.getHost());
    int x = 0;
    for (String replace : replaces) {
      fullUrl = fullUrl.replace("%" + x + "$s", replace);
      x++;
    }
    return fullUrl;
  }

  private SSLContext ignoreCert() {
    try {
      TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
              return null;
            }

            public void checkClientTrusted(X509Certificate[] certs, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] certs, String authType) {
            }
          }
      };
      SSLContext context = SSLContext.getInstance("SSL");
      context.init(null, trustAllCerts, new SecureRandom());
      return context;
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
