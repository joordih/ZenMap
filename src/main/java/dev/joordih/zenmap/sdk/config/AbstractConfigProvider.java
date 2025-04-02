package dev.joordih.zenmap.sdk.config;


import com.google.common.collect.Maps;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;

public abstract class AbstractConfigProvider implements ConfigProvider {

  protected Map<String, Object> configurationData = Collections.emptyMap();

  protected Optional<Object> getNestedValue(String key, Map<String, Object> dataMap) {
    if (key == null || key.isEmpty() || dataMap == null) {
      return Optional.empty();
    }

    String[] keys = key.split("\\.");
    Map<String, Object> currentMap = dataMap;

    for (int i = 0; i < keys.length - 1; i++) {
      Object value = currentMap.get(keys[i]);
      if (!(value instanceof Map)) {
        return Optional.empty();
      }

      currentMap = (Map<String, Object>) value;
    }

    return Optional.ofNullable(currentMap.get(keys[keys.length - 1]));
  }

  @Override
  public Optional<Object> getValue(String key) {
    return getNestedValue(key, configurationData);
  }
}
