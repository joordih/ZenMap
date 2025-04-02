package dev.joordih.zenmap.sdk.config;

import java.util.Optional;

public class Configuration {

  private static Configuration instance;
  private final ConfigProvider provider;

  private Configuration(ConfigProvider provider) {
    this.provider = provider;
  }

  public static synchronized Configuration getInstance() {
    if (instance == null) {
      ConfigProvider provider = new YamlConfigProvider("config/settings.yml");
      instance = new Configuration(provider);
    }

    return instance;
  }

  public static synchronized Configuration getInstance(ConfigProvider provider) {
    if (instance == null) {
      instance = new Configuration(provider);
    }

    return instance;
  }

  public void reload() {
    provider.reload();
  }

  public Optional<Object> get(String key) {
    return provider.getValue(key);
  }

  public String getString(String key) {
    return ConfigValueConverter.toString(get(key));
  }

  public Integer getInteger(String key) {
    return ConfigValueConverter.toInteger(get(key));
  }

  public Boolean getBoolean(String key) {
    return ConfigValueConverter.toBoolean(get(key));
  }

  public Double getDouble(String key) {
    return ConfigValueConverter.toDouble(get(key));
  }
}
