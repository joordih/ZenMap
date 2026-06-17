package dev.joordih.zenmap.managers.config.impl;

import dev.joordih.zenmap.sdk.config.ConfigSection;
import dev.joordih.zenmap.sdk.config.Configuration;

public class ZenmapConfiguration extends ConfigSection {

  public ZenmapConfiguration(Configuration config) {
    super(config, "zenmap");
  }

  public ZenmapServerConfig getServer() {
    return new ZenmapServerConfig(config);
  }

  public static class ZenmapServerConfig extends ConfigSection {
    public ZenmapServerConfig(Configuration config) {
      super(config, "zenmap.server");
    }

    public String getHost() {
      return config.getString("host");
    }

    public Integer getPort() {
      return config.getInteger("port");
    }
  }
}
