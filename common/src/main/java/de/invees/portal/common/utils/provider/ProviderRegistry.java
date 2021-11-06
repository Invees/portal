package de.invees.portal.common.utils.provider;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ProviderRegistry {

  private static final Map<Class<? extends Provider>, Provider> PROVIDERS = new ConcurrentHashMap<>();

  private ProviderRegistry() {
  }

  public static <T extends Provider> void register(final Class<T> clazz, final T provider) {
    PROVIDERS.put(clazz, provider);
  }

  public static <T extends Provider> T access(final Class<T> clazz) {
    return (T) PROVIDERS.get(clazz);
  }

  public static Map<Class<? extends Provider>, Provider> getAllProviders() {
    return Collections.unmodifiableMap(PROVIDERS);
  }

}
