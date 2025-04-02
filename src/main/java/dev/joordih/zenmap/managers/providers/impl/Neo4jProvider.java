package dev.joordih.zenmap.managers.providers.impl;

import dev.joordih.zenmap.Zenmap;
import dev.joordih.zenmap.managers.providers.Provider;
import dev.joordih.zenmap.managers.providers.ProviderParams;
import dev.joordih.zenmap.managers.providers.ProviderPriority;
import lombok.Getter;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.GraphDatabase;

import static dev.joordih.zenmap.managers.config.impl.DatabaseConfiguration.Neo4jConfiguration;

@Getter
@ProviderParams(
    name = "Neo4J Database Provider",
    priority = ProviderPriority.NORMAL
)
public class Neo4jProvider implements Provider {

  private final Zenmap instance;
  private final Neo4jConfiguration neo4jConfiguration;

  public Neo4jProvider() {
    this.instance = Zenmap.getInstance();
    this.neo4jConfiguration = instance.getConfigFactory().getDatabaseConfiguration().getNeo4j();
  }

  @Override
  public void register() {
//    final String uri = this.neo4jConfiguration.getUri();
//    final String username = this.neo4jConfiguration.getUsername();
//    final String password = this.neo4jConfiguration.getPassword();
//
//    try (var driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password))) {
//      driver.verifyConnectivity();
//      System.out.println("Connected to Neo4j database.");
//    } catch (Exception e) {
//      e.printStackTrace();
//    }
  }
}
