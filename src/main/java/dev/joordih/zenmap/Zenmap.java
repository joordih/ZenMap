package dev.joordih.zenmap;

import dev.joordih.zenmap.managers.config.ConfigurationFactory;
import dev.joordih.zenmap.managers.nodes.NodeManager;
import dev.joordih.zenmap.managers.providers.ProviderManager;
import dev.joordih.zenmap.sdk.config.Configuration;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public class Zenmap {

  private static final Logger LOGGER = LoggerFactory.getLogger(Zenmap.class);

  @Getter
  private static Zenmap instance;

  private final Configuration config;
  private final ConfigurationFactory configFactory;

  private final ProviderManager providerManager;


  public Zenmap() {
    instance = this;

    this.config = Configuration.getInstance();
    this.configFactory = new ConfigurationFactory(config);

    LOGGER.info("Mapping zenmap configuration...");
    this.configFactory.getZenmapConfiguration();

    LOGGER.info("Mapping database configuration...");
    this.configFactory.getDatabaseConfiguration();

    LOGGER.info("Instancing and loading providers...");
    this.providerManager = new ProviderManager();
    this.providerManager.loadProviders();

    LOGGER.info("Instancing and loading nodes...");
    new NodeManager();
  }

  public static void main(String[] args) {
    new Zenmap();

    Thread keepAliveThread = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          LOGGER.info("Cya.");
          break;
        }
      }
    });

    keepAliveThread.setDaemon(false);
    keepAliveThread.start();
  }
}
