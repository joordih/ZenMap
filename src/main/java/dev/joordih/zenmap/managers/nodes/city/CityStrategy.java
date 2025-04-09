package dev.joordih.zenmap.managers.nodes.city;

import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import dev.joordih.zenmap.managers.strategy.StrategyFactory;
import dev.joordih.zenmap.managers.strategy.fetch.DataFetchStrategy;
import dev.joordih.zenmap.managers.strategy.fetch.HttpDataFetchStrategy;
import org.neo4j.ogm.session.Session;

import java.io.IOException;
import java.util.List;

public class CityStrategy {

  private final Neo4jProvider provider;
  private final Session session;

  public CityStrategy(Neo4jProvider provider, Session session) {
    this.provider = provider;
    this.session = session;

    System.out.println("------------------------------");
    System.out.println("Loading cities...");
    System.out.println("------------------------------");

    HttpDataFetchStrategy<City> cityFetchStrategy = new HttpDataFetchStrategy<>(session, City.class);

    StrategyFactory.registerStrategy(
        DataFetchStrategy.class,
        City.class,
        cityFetchStrategy
    );

    try {
      String cityData = StrategyFactory.execute(
          DataFetchStrategy.class,
          City.class,
          (strategy, params) -> {
            try {
              return strategy.fetchData((String) params[0]);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          },
          "https://ideib.caib.es/adreces/rest/municipis"
      );

      List<City> cities = StrategyFactory.execute(
          DataFetchStrategy.class,
          City.class,
          (strategy, params) -> {
            try {
              return strategy.parseJsonList((String) params[0]);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          },
          cityData
      );

      for (int i = 0; i < cities.size(); i++) {
        City city = cities.get(i);
        session.save(city);
        System.out.println("Saved city: " + i);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
