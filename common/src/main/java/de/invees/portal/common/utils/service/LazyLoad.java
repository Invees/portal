package de.invees.portal.common.utils.service;

public class LazyLoad<T extends Service> {

  private final Class<T> serviceName;
  private T service;

  public LazyLoad(final Class<T> serviceName) {
    this.serviceName = serviceName;
  }

  public T get() {
    if (service == null) {
      service = ServiceRegistry.access(serviceName);
    }
    if (service == null) {
      throw new IllegalStateException("Service is not loaded yet!");
    }
    return service;
  }
}