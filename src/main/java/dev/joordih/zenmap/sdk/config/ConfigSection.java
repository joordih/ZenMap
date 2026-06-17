package dev.joordih.zenmap.sdk.config;

public abstract class ConfigSection {
  protected final Configuration config;
  protected final String prefix;

  protected ConfigSection(Configuration config, String prefix) {
    this.config = config;
    this.prefix = prefix;
  }

  protected String getKey(String key) {
    return prefix + "." + key;
  }
}
