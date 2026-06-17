package dev.joordih.zenmap.managers.config.impl;

import dev.joordih.zenmap.sdk.config.ConfigSection;
import dev.joordih.zenmap.sdk.config.Configuration;

public class DatabaseConfiguration extends ConfigSection {

  public DatabaseConfiguration(Configuration config) {
    super(config, "database");
  }

  public Neo4jConfiguration getNeo4j() {
    return new Neo4jConfiguration(config);
  }

  public static class Neo4jConfiguration extends ConfigSection {

    public Neo4jConfiguration(Configuration config) {
      super(config, "database.neo4j");
    }

    public String getUri() {
      return config.getString(getKey("uri"));
    }

    public String getUsername() {
      return config.getString(getKey("username"));
    }

    public String getPassword() {
      return config.getString(getKey("password"));
    }
  }
}
