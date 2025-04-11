package dev.joordih.zenmap.managers.nodes;

import com.google.common.collect.Lists;
import dev.joordih.zenmap.managers.nodes.city.City;
import dev.joordih.zenmap.managers.nodes.city.CityStrategy;
import dev.joordih.zenmap.managers.nodes.intersection.Intersection;
import dev.joordih.zenmap.managers.nodes.intersection.IntersectionStrategy;
import dev.joordih.zenmap.managers.nodes.lane.Lane;
import dev.joordih.zenmap.managers.nodes.lane.LaneStrategy;
import dev.joordih.zenmap.managers.nodes.track.Track;
import dev.joordih.zenmap.managers.nodes.track.TrackStrategy;
import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import dev.joordih.zenmap.managers.repository.NeoObjectRepository;

import org.neo4j.ogm.session.Session;

import java.util.Collection;
import java.util.Optional;

public class NodeManager {

  private final NeoObjectRepository<Lane> laneRepository;
  private final NeoObjectRepository<City> cityRepository;
  private final NeoObjectRepository<Track> trackRepository;
  private final NeoObjectRepository<Intersection> intersectionRepository;

  private void initializeStrategyIfEmpty(Collection<?> collection, Runnable strategyInitializer, String strategyName) {
    if (collection.isEmpty()) {
      strategyInitializer.run();
      System.out.println(strategyName + " initialized.");
    }
  }

  public NodeManager() {
    Neo4jProvider provider = new Neo4jProvider();
    Session session = Neo4jProvider.getSessionFactory().openSession();
    this.laneRepository = new NeoObjectRepository<>(session, Lane.class);
    this.cityRepository = new NeoObjectRepository<>(session, City.class);
    this.trackRepository = new NeoObjectRepository<>(session, Track.class);
    this.intersectionRepository = new NeoObjectRepository<>(session, Intersection.class);

    Collection<City> cities = Optional.ofNullable(this.cityRepository.findAll()).orElse(Lists.newArrayList());
    Collection<Lane> lanes = Optional.ofNullable(this.laneRepository.findAll()).orElse(Lists.newArrayList());
    Collection<Track> tracks = Optional.ofNullable(this.trackRepository.findAll()).orElse(Lists.newArrayList());
    Collection<Intersection> intersections = Optional.ofNullable(this.intersectionRepository.findAll()).orElse(Lists.newArrayList());

    System.out.println("Cities: " + cities.size());
    System.out.println("Lanes: " + lanes.size());
    System.out.println("Tracks: " + tracks.size());
    System.out.println("Intersections: " + intersections.size());

    initializeStrategyIfEmpty(cities, () -> new CityStrategy(provider, session), "Cities");
    // initializeStrategyIfEmpty(lanes, () -> new LaneStrategy(provider, session, cities), "Lanes");
    new LaneStrategy(provider, session, cities);
    initializeStrategyIfEmpty(tracks, () -> new TrackStrategy(provider, session, lanes), "Tracks");
    initializeStrategyIfEmpty(intersections, () -> new IntersectionStrategy(provider, session, tracks), "Intersections");
    // new IntersectionStrategy(provider, session, tracks);
  }
}
