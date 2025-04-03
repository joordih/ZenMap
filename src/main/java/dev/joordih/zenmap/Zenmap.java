package dev.joordih.zenmap;

import dev.joordih.zenmap.managers.config.ConfigurationFactory;
import dev.joordih.zenmap.managers.nodes.NodeManager;
import dev.joordih.zenmap.managers.providers.ProviderManager;
import dev.joordih.zenmap.sdk.config.Configuration;
import lombok.Getter;

@Getter
public class Zenmap {

  @Getter
  private static Zenmap instance;

  private final Configuration config;
  private final ConfigurationFactory configFactory;

  private final ProviderManager providerManager;


  public Zenmap() {
    instance = this;

    this.config = Configuration.getInstance();
    this.configFactory = new ConfigurationFactory(config);

    System.out.println("Mapping zenmap configuration...");
    this.configFactory.getZenmapConfiguration();

    System.out.println("Mapping database configuration...");
    this.configFactory.getDatabaseConfiguration();

    System.out.println("Instancing and loading providers...");
    this.providerManager = new ProviderManager();
    this.providerManager.loadProviders();

    System.out.println("Instancing and loading nodes...");
    new NodeManager();
  }

  public static void main(String[] args) {
    new Zenmap();

    Thread keepAliveThread = new Thread(() -> {
      while (true) {
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          System.out.println("Cya.");
          break;
        }
      }
    });

    keepAliveThread.setDaemon(false);
    keepAliveThread.start();
  }
}
