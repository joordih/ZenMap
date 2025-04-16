package dev.joordih.zenmap.managers.nodes.lane;

import com.google.common.collect.Lists;
import dev.joordih.zenmap.managers.nodes.city.City;
import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import dev.joordih.zenmap.managers.strategy.StrategyFactory;
import dev.joordih.zenmap.managers.strategy.fetch.DataFetchStrategy;
import dev.joordih.zenmap.managers.strategy.fetch.HttpDataFetchStrategy;
import lombok.Getter;
import org.neo4j.ogm.session.Session;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

@Getter
public class LaneStrategy {

  private final Neo4jProvider provider;
  private final Session session;

  @SuppressWarnings("unchecked")
  public LaneStrategy(Neo4jProvider provider, Session session, Collection<City> cities) {
    this.provider = provider;
    this.session = session;

    System.out.println("------------------------------");
    System.out.println("Loading lanes...");
    System.out.println("------------------------------");

    HttpDataFetchStrategy<Lane> laneFetchStrategy = new HttpDataFetchStrategy<>(session, Lane.class);

    StrategyFactory.registerStrategy(
        DataFetchStrategy.class,
        Lane.class,
        laneFetchStrategy
    );

    try {
      List<Lane> lanes = Lists.newArrayList();

      for (int i = 0; i < cities.size(); i++) {
        City city = cities.stream().toList().get(i);

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
            String.format("https://ideib.caib.es/adreces/rest/vies/%s", city.getId())
        );

        StrategyFactory.execute(
            DataFetchStrategy.class,
            Lane.class,
            (strategy, params) -> {
              try {
                return (List<Lane>) strategy.parseJsonList((String) params[0]).stream()
                    .filter(lane -> !((Lane) lane).getIdIne().equalsIgnoreCase("-997"))
                    .collect(Collectors.toList());
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            },
            laneData
        ).stream().forEach(lane -> lanes.add(lane));
      }

      for (int x = 0; x < lanes.size(); x++) {
        Lane lane = lanes.get(x);
        session.save(lane);
        System.out.println("Saved lane (Postal Code: " + lane.getPostalCode() + "): " + x);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }
}
