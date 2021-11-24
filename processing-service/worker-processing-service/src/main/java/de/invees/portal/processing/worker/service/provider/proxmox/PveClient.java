package de.invees.portal.processing.worker.service.provider.proxmox;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.invees.portal.common.model.service.console.ServiceConsole;
import de.invees.portal.common.model.service.console.ServiceConsoleType;
import de.invees.portal.common.model.service.status.ServiceStatus;
import de.invees.portal.common.model.service.status.ServiceStatusType;
import de.invees.portal.common.utils.gson.GsonUtils;
import de.invees.portal.processing.worker.Application;
import de.invees.portal.processing.worker.configuration.ProxmoxConfiguration;
import de.invees.portal.processing.worker.service.provider.proxmox.model.Storage;
import de.invees.portal.processing.worker.service.provider.proxmox.model.VirtualMachine;
import de.invees.portal.processing.worker.service.provider.proxmox.model.VirtualMachineCreate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.*;

import static java.net.http.HttpClient.Version;

public class PveClient {

  public static class URL {
    public static final String LOGIN = "{%url%}/api2/json/access/ticket";
    public static final String QEMU = "{%url%}/api2/json/nodes/%0$s/qemu";
    public static final String NEXT_ID = "{%url%}/api2/json/cluster/nextid";
    public static final String STORAGE = "{%url%}/api2/json/nodes/%0$s/storage";
    public static final String START = "{%url%}/api2/json/nodes/%0$s/qemu/%1$s/status/start";
    public static final String RESTART = "{%url%}/api2/json/nodes/%0$s/qemu/%1$s/status/reboot";
    public static final String STOP = "{%url%}/api2/json/nodes/%0$s/qemu/%1$s/status/shutdown";
    public static final String KILL = "{%url%}/api2/json/nodes/%0$s/qemu/%1$s/status/stop";
    public static final String STATUS = "{%url%}/api2/json/nodes/%0$s/qemu/%1$s/status/current";
    public static final String SPICE = "{%url%}/api2/json/nodes/%0$s/qemu/%1$s/spiceproxy";
    public static final String TASKS = "{%url%}/api2/json/nodes/%0$s/tasks?vmid=%1$s&source=active";
    public static final String TASK = "{%url%}/api2/json/nodes/%0$s/tasks/%1$s";
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

  public ServiceConsole createConsole(UUID service) {
    JsonObject data = post(
        URI.create(parse(URL.SPICE, configuration.getNode(), getMachine(service).getVmid() + "")),
        body("node", configuration.getNode())
    ).getAsJsonObject("data");
    Map<String, Object> configuration = new HashMap<>();
    for (String key : data.keySet()) {
      configuration.put(key, data.get(key));
    }
    return new ServiceConsole(ServiceConsoleType.SPICE, configuration);
  }

  public void killActiveTask(UUID service) {
    JsonArray data = get(
        URI.create(parse(URL.TASKS, configuration.getNode(), getMachine(service).getVmid() + ""))
    ).getAsJsonArray("data");
    if (data.size() == 0) {
      return;
    }
    JsonObject obj = data.get(0).getAsJsonObject();
    delete(URI.create(parse(URL.TASK, configuration.getNode(), obj.get("upid").getAsString())));
  }

  public ServiceStatus getStatus(UUID service) {
    JsonObject data = get(URI.create(parse(
        URL.STATUS,
        configuration.getNode(),
        getMachine(service).getVmid() + "")
    )).getAsJsonObject("data");

    Map<String, Object> configuration = new HashMap<>();
    configuration.put("cpu", data.get("cpus").getAsInt());
    configuration.put("memory", data.get("maxmem").getAsDouble() / 1024d / 1024d);
    configuration.put("storage", data.get("maxdisk").getAsDouble() / 1024d / 1024d);
    return new ServiceStatus(
        service,
        configuration,
        ServiceStatusType.valueOf(data.get("status").getAsString().toUpperCase()),
        null,
        data.get("uptime").getAsLong()
    );
  }

  public void start(UUID service) {
    killActiveTask(service);
    post(
        URI.create(parse(URL.START, configuration.getNode(), getMachine(service).getVmid() + "")),
        new JsonObject().toString()
    ).getAsJsonObject();
  }

  public void stop(UUID service) {
    killActiveTask(service);
    post(
        URI.create(parse(URL.STOP, configuration.getNode(), getMachine(service).getVmid() + "")),
        new JsonObject().toString()
    ).getAsJsonObject();
  }

  public void kill(UUID service) {
    killActiveTask(service);
    post(
        URI.create(parse(URL.KILL, configuration.getNode(), getMachine(service).getVmid() + "")),
        new JsonObject().toString()
    ).getAsJsonObject();
  }

  public void restart(UUID service) {
    killActiveTask(service);
    post(
        URI.create(parse(URL.RESTART, configuration.getNode(), getMachine(service).getVmid() + "")),
        new JsonObject().toString()
    ).getAsJsonObject();
  }

  public List<Storage> getStorages() {
    JsonArray data = get(URI.create(parse(URL.STORAGE, configuration.getNode()))).getAsJsonArray("data");
    List<Storage> storages = new ArrayList<>();
    for (JsonElement element : data) {
      storages.add(GsonUtils.GSON.fromJson(element, Storage.class));
    }
    return storages;
  }

  public VirtualMachine getMachine(UUID serviceId) {
    List<VirtualMachine> machines = getVirtualMachines();
    for (VirtualMachine machine : machines) {
      if (machine.getName().equalsIgnoreCase(serviceId.toString())) {
        return machine;
      }
    }
    return null;
  }

  public List<VirtualMachine> getVirtualMachines() {
    JsonArray data = get(URI.create(parse(URL.QEMU, configuration.getNode()))).getAsJsonArray("data");
    List<VirtualMachine> machines = new ArrayList<>();
    for (JsonElement element : data) {
      machines.add(GsonUtils.GSON.fromJson(element, VirtualMachine.class));
    }
    return machines;
  }

  public void createVirtualMachine(VirtualMachineCreate machine) {
    post(URI.create(parse(URL.QEMU, configuration.getNode())), GsonUtils.toJson(machine));
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

  // Internal
  private JsonObject delete(URI uri) {
    try {
      HttpRequest request = HttpRequest.newBuilder()
          .uri(uri)
          .DELETE()
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
