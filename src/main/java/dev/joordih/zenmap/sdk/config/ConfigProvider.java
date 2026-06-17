package dev.joordih.zenmap.sdk.config;

import java.util.Optional;

public interface ConfigProvider {
  Optional<Object> getValue(String key);

  void reload();
}
