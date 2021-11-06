package de.invees.portal.common.utils.provider;

public class LazyLoad<T extends Provider> {

  private final Class<T> providerName;
  private T provider;

  public LazyLoad(final Class<T> providerName) {
    this.providerName = providerName;
  }

  public T get() {
    if (provider == null) {
      provider = ProviderRegistry.access(providerName);
    }
    if (provider == null) {
      throw new IllegalStateException("Service is not loaded yet!");
    }
    return provider;
  }
}