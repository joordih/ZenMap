package dev.joordih.zenmap.managers.nodes;

import dev.joordih.zenmap.managers.nodes.lane.Lane;
import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import dev.joordih.zenmap.managers.strategy.StrategyFactory;
import dev.joordih.zenmap.managers.strategy.fetch.DataFetchStrategy;
import dev.joordih.zenmap.managers.strategy.fetch.HttpDataFetchStrategy;
import org.neo4j.ogm.session.Session;

import java.io.IOException;
import java.util.List;

public class NodeManager {

  public NodeManager() {
    Neo4jProvider provider = new Neo4jProvider();
    Session session = Neo4jProvider.getSessionFactory().openSession();

    HttpDataFetchStrategy<Lane> laneFetchStrategy = new HttpDataFetchStrategy<>(session, Lane.class);

    StrategyFactory.registerStrategy(
        DataFetchStrategy.class,
        Lane.class,
        laneFetchStrategy
    );

    try {
      String laneData = StrategyFactory.execute(
          DataFetchStrategy.class,
          Lane.class,
          (strategy, params) -> {
            try {
              return strategy.fetchData((String) params[0]);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          },
          "https://ideib.caib.es/adreces/rest/vies/07014"
      );

      List<Lane> lanes = StrategyFactory.execute(
          DataFetchStrategy.class,
          Lane.class,
          (strategy, params) -> {
            try {
              return strategy.parseJsonList((String) params[0]);
            } catch (IOException e) {
              throw new RuntimeException(e);
            }
          },
          laneData
      );

      for (int i = 0; i < lanes.size(); i++) {
        Lane lane = lanes.get(i);
        session.save(lane);
        System.out.println("Saved lane: " + lane);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
