package dev.joordih.zenmap.managers.nodes.track;

import com.google.common.collect.Lists;
import dev.joordih.zenmap.managers.nodes.lane.Lane;
import dev.joordih.zenmap.managers.nodes.repository.NeoObjectRepository;
import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import dev.joordih.zenmap.managers.strategy.StrategyFactory;
import dev.joordih.zenmap.managers.strategy.fetch.DataFetchStrategy;
import dev.joordih.zenmap.managers.strategy.fetch.HttpDataFetchStrategy;
import lombok.Getter;
import org.neo4j.ogm.session.Session;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Getter
public class TrackStrategy {

  private final Neo4jProvider provider;
  private final Session session;
  private final NeoObjectRepository<Track> trackRepository;

  public TrackStrategy(Neo4jProvider provider, Session session, Collection<Lane> lanes) {
    this.provider = provider;
    this.session = session;

    this.trackRepository = new NeoObjectRepository<>(session, Track.class);

    System.out.println("------------------------------");
    System.out.println("Loading tracks...");
    System.out.println("------------------------------");

    HttpDataFetchStrategy<Track> trackFetchStrategy = new HttpDataFetchStrategy<>(session, Track.class);

    StrategyFactory.registerStrategy(
        DataFetchStrategy.class,
        Track.class,
        trackFetchStrategy
    );

    List<Track> tracks = Lists.newArrayList();
    try {
      lanes.stream().filter(lane -> lane instanceof Lane).forEach(lane -> {
        Optional<Track> track = Optional.ofNullable(trackRepository.findByPostalCode(lane.getPostalCode()));


        if (track.isPresent()) {
          System.out.println("Track already exists: " + lane.getPostalCode());
          lanes.remove(lane);
        }
      });

      for (int i = 0; i < lanes.size(); i++) {
        Lane lane = lanes.stream().toList().get(i);

        String endpoint = String.format("https://ideib.caib.es/adreces/rest/via/%s?ine=1&geometria=1&wgs84=1", lane.getIdIne());
        System.out.println("Endpoint: " + endpoint);

        String trackData = StrategyFactory.execute(
            DataFetchStrategy.class,
            Track.class,
            (strategy, params) -> {
              try {
                return strategy.fetchData((String) params[0]);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            },
            endpoint
        );

        Track track = StrategyFactory.execute(
            DataFetchStrategy.class,
            Track.class,
            (strategy, params) -> {
              try {
                return (Track) strategy.parseJsonObject((String) params[0]);
              } catch (IOException e) {
                throw new RuntimeException(e);
              }
            },
            trackData
        );

        tracks.add(track);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        for (Track track : tracks) {
          session.save(track);
          System.out.printf("Saved track (%s): %d %n", track.getPostalCode(), tracks.indexOf(track));
        }
      } catch (Exception e) {
        e.printStackTrace();
      } finally {
        for (Track track : tracks) {
          session.save(track);
          System.out.printf("Saved track (%s): %d %n", track.getPostalCode(), tracks.indexOf(track));
        }
      }
    }
  }
}
