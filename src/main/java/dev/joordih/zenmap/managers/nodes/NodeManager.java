package dev.joordih.zenmap.managers.nodes;

import com.google.common.collect.Lists;
import dev.joordih.zenmap.managers.nodes.city.City;
import dev.joordih.zenmap.managers.nodes.city.CityStrategy;
import dev.joordih.zenmap.managers.nodes.lane.Lane;
import dev.joordih.zenmap.managers.nodes.lane.LaneStrategy;
import dev.joordih.zenmap.managers.nodes.repository.NeoObjectRepository;
import dev.joordih.zenmap.managers.nodes.track.Track;
import dev.joordih.zenmap.managers.nodes.track.TrackStrategy;
import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import org.neo4j.ogm.session.Session;

import java.util.Collection;
import java.util.Optional;

public class NodeManager {

  private final NeoObjectRepository<Lane> laneRepository;
  private final NeoObjectRepository<City> cityRepository;
  private final NeoObjectRepository<Track> trackRepository;

  public NodeManager() {
    Neo4jProvider provider = new Neo4jProvider();
    Session session = Neo4jProvider.getSessionFactory().openSession();
    this.laneRepository = new NeoObjectRepository<>(session, Lane.class);
    this.cityRepository = new NeoObjectRepository<>(session, City.class);
    this.trackRepository = new NeoObjectRepository<>(session, Track.class);

    Collection<City> cities = Optional.ofNullable(this.cityRepository.findAll()).orElse(Lists.newArrayList());
    Collection<Lane> lanes = Optional.ofNullable(this.laneRepository.findAll()).orElse(Lists.newArrayList());
    Collection<Track> tracks = Optional.ofNullable(this.trackRepository.findAll()).orElse(Lists.newArrayList());

    if (lanes.isEmpty() || cities.isEmpty()) {
      new LaneStrategy(provider, session, cities);
      new CityStrategy(provider, session);

      System.out.println("------------------------------");
      System.out.println("Lanes loaded.");
      System.out.println("------------------------------");
      return;
    }
    new TrackStrategy(provider, session, lanes);
  }
}
