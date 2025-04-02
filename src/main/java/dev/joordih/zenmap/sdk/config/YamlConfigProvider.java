package dev.joordih.zenmap.sdk.config;

import lombok.Getter;
import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collections;

@Getter
public class YamlConfigProvider extends AbstractConfigProvider {

  private final String configurationPath;
  private final ConfigFileManager configFileManager;

  public YamlConfigProvider(String configurationPath) {
    this.configurationPath = configurationPath;
    this.configFileManager = new ConfigFileManager(configurationPath);

    reload();
  }

  @Override
  public void reload() {
    try {
      if (!configFileManager.configFileExists()) {
        configFileManager.createConfigFile();
      }

      try (InputStream stream = new FileInputStream(configurationPath)) {
        Yaml yaml = new Yaml();
        configurationData = yaml.load(stream);

        if (configurationData == null) {
          configurationData = Collections.emptyMap();
        }
      }
    } catch (Exception exception) {
      exception.printStackTrace();
      System.err.println("Failed to reload YML config: " + exception.getMessage());
      configurationData = Collections.emptyMap();
    }
  }
}
