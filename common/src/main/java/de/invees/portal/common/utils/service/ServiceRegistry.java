package de.invees.portal.common.utils.service;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {

  private static final Map<Class<? extends Service>, Service> SERVICES = new ConcurrentHashMap<>();

  private ServiceRegistry() {
  }

  public static <T extends Service> void register(final Class<T> clazz, final T service) {
    SERVICES.put(clazz, service);
  }

  public static <T extends Service> T access(final Class<T> clazz) {
    return (T) SERVICES.get(clazz);
  }

  public static Map<Class<? extends Service>, Service> getAllServices() {
    return Collections.unmodifiableMap(SERVICES);
  }

}
