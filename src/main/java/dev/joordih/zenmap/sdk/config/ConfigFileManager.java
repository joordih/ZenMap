package dev.joordih.zenmap.sdk.config;

import lombok.Getter;
import lombok.Setter;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Getter
@Setter
public class ConfigFileManager {

  private final String configurationPath;

  public ConfigFileManager(String configurationPath) {
    this.configurationPath = configurationPath;
  }

  public boolean configFileExists() {
    return Files.exists(Paths.get(configurationPath));
  }

  public void createConfigFile() throws Exception {
    Path directory = Paths.get("config/settings.yml").getParent();

    if (directory != null && !Files.exists(directory)) {
      Files.createDirectories(directory);
    }

    String defaultConfig = """
        zenmap:
          server:
            host: 0.0.0.0
            port: 8080
        
        database:
          neo4j:
            uri: neo4j://localhost:7687
            username: neo4j
            password: password
        """;

    Files.writeString(Paths.get(configurationPath), defaultConfig);
    System.out.println("Configuration file created at: " + configurationPath);
  }
}
