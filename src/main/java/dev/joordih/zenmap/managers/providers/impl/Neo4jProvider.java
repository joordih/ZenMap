package dev.joordih.zenmap.managers.providers.impl;

import dev.joordih.zenmap.Zenmap;
import dev.joordih.zenmap.managers.providers.Provider;
import dev.joordih.zenmap.managers.providers.ProviderParams;
import dev.joordih.zenmap.managers.providers.ProviderPriority;
import lombok.Getter;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;
import org.neo4j.ogm.drivers.bolt.driver.BoltDriver;
import org.neo4j.ogm.session.SessionFactory;

import static dev.joordih.zenmap.managers.config.impl.DatabaseConfiguration.Neo4jConfiguration;

@Getter
@ProviderParams(
    name = "Neo4J Database Provider",
    priority = ProviderPriority.HIGH
)
public class Neo4jProvider implements Provider {

  @Getter
  private static SessionFactory sessionFactory;
  private final Zenmap instance = Zenmap.getInstance();
  private final Neo4jConfiguration neo4jConfiguration = instance.getConfigFactory().getDatabaseConfiguration().getNeo4j();

  @Override
  public void register() {
    try {
      final String uri = this.neo4jConfiguration.getUri();
      final String username = this.neo4jConfiguration.getUsername();
      final String password = this.neo4jConfiguration.getPassword();

      Driver nativeDriver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
      var driver = new BoltDriver(nativeDriver);

      sessionFactory = new SessionFactory(driver, "dev.joordih.zenmap.managers.nodes");

      System.out.println("Connected to Neo4j database.");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
