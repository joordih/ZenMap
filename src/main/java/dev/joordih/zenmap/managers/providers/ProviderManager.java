package dev.joordih.zenmap.managers.providers;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.Getter;
import org.reflections.Reflections;
import org.reflections.scanners.Scanners;

import java.lang.reflect.Constructor;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class ProviderManager {

  private final Set<Provider> providers = Sets.newHashSet();
  private final Map<Provider, ProviderParams> providerParams = Maps.newHashMap();
  private final Reflections reflections;

  public ProviderManager() {
    this.reflections = new Reflections("dev.joordih.zenmap.managers.providers.impl", Scanners.SubTypes);
  }

  public void loadProviders() {
    Set<Class<? extends Provider>> providerClasses = this.reflections.getSubTypesOf(Provider.class);
    Map<Class<? extends Provider>, ProviderParams> tempProviders = Maps.newHashMap();

    for (Class<? extends Provider> clazz : providerClasses) {
      if (!clazz.isAnnotationPresent(ProviderParams.class)) {
        System.out.println("Provider " + clazz.getName() + " is missing @ProviderParams annotation");
        continue;
      }

      ProviderParams params = clazz.getAnnotation(ProviderParams.class);
      tempProviders.put(clazz, params);
    }

    List<Class<? extends Provider>> sortedProviderClasses = tempProviders.entrySet().stream()
        .sorted(Comparator.comparingInt(entry -> entry.getValue().priority().ordinal()))
        .map(Map.Entry::getKey)
        .collect(Collectors.toList());

    for (Class<? extends Provider> clazz : sortedProviderClasses) {
      try {
        Constructor<? extends Provider> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Provider provider = constructor.newInstance();

        ProviderParams providerParams = clazz.getAnnotation(ProviderParams.class);
        this.providerParams.put(provider, providerParams);
        this.providers.add(provider);

        provider.register();

        System.out.println("Registered provider: " + provider.getClass().getName() + " (" + providerParams.name() + ")" +
            " with priority " + providerParams.priority());
      } catch (Exception exception) {
        exception.printStackTrace();
        System.out.println("Failed to register provider: " + clazz.getName());
      }
    }

  /*this.reflections.getSubTypesOf(Provider.class).forEach(clazz -> {
      try {
        Constructor<? extends Provider> constructor = clazz.getDeclaredConstructor();
        constructor.setAccessible(true);
        Provider provider = constructor.newInstance();

        if (!clazz.isAnnotationPresent(ProviderParams.class)) {
          throw new RuntimeException("Provider " + clazz.getName() + " is missing @ProviderParams annotation");
        }

        ProviderParams providerParams = clazz.getAnnotation(ProviderParams.class);
        this.providerParams.put(provider, providerParams);
        this.providers.add(provider);

        System.out.println("Registered provider: " + provider.getClass().getName());
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to register provider: " + clazz.getName());
      }
    });*/
  }

  public void unloadProviders() {
    List<Provider> sortedProviders = this.providers.stream()
        .sorted(Comparator.<Provider>comparingInt(p ->
            this.providerParams.get(p).priority().ordinal()).reversed())
        .collect(Collectors.toList());

    for (Provider provider : sortedProviders) {
      try {
        provider.unregister();
        System.out.println("Unregistered provider: " + provider.getClass().getName());
      } catch (Exception e) {
        e.printStackTrace();
        System.out.println("Failed to unregister provider: " + provider.getClass().getName());
      }
    }

    this.providers.clear();
    this.providerParams.clear();
  }
}
