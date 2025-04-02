package dev.joordih.zenmap.managers.config;

import dev.joordih.zenmap.managers.config.impl.DatabaseConfiguration;
import dev.joordih.zenmap.managers.config.impl.ZenmapConfiguration;
import dev.joordih.zenmap.sdk.config.Configuration;

public class ConfigurationFactory {
  private final Configuration configuration;

  public ConfigurationFactory(Configuration configuration) {
    this.configuration = configuration;
  }

  public ZenmapConfiguration getZenmapConfiguration() {
    return new ZenmapConfiguration(configuration);
  }

  public DatabaseConfiguration getDatabaseConfiguration() {
    return new DatabaseConfiguration(configuration);
  }
}
