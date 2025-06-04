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
import dev.joordih.zenmap.managers.service.RouteService;

import org.neo4j.ogm.session.Session;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class NodeManager {

  private static final Logger LOGGER = LoggerFactory.getLogger(NodeManager.class);

  private final NeoObjectRepository<Lane> laneRepository;
  private final NeoObjectRepository<City> cityRepository;
  private final NeoObjectRepository<Track> trackRepository;
  private final NeoObjectRepository<Intersection> intersectionRepository;
  private final RouteService routeService;

  private void initializeStrategyIfEmpty(Collection<?> collection, Runnable strategyInitializer, String strategyName) {
    if (collection.isEmpty()) {
      strategyInitializer.run();
      LOGGER.info("{} initialized.", strategyName);
    }
  }

  public NodeManager() {
    Neo4jProvider provider = new Neo4jProvider();
    Session session = Neo4jProvider.getSessionFactory().openSession();
    this.laneRepository = new NeoObjectRepository<>(session, Lane.class);
    this.cityRepository = new NeoObjectRepository<>(session, City.class);
    this.trackRepository = new NeoObjectRepository<>(session, Track.class);
    this.intersectionRepository = new NeoObjectRepository<>(session, Intersection.class);
    this.routeService = new RouteService();

    Collection<City> cities = Optional.ofNullable(this.cityRepository.findAll()).orElse(Lists.newArrayList());
    Collection<Lane> lanes = Optional.ofNullable(this.laneRepository.findAll()).orElse(Lists.newArrayList());
    Collection<Track> tracks = Optional.ofNullable(this.trackRepository.findAll()).orElse(Lists.newArrayList());
    Collection<Intersection> intersections = Optional.ofNullable(this.intersectionRepository.findAll()).orElse(Lists.newArrayList());

    LOGGER.info("Cities: {}", cities.size());
    LOGGER.info("Lanes: {}", lanes.size());
    LOGGER.info("Tracks: {}", tracks.size());
    LOGGER.info("Intersections: {}", intersections.size());

    initializeStrategyIfEmpty(cities, () -> new CityStrategy(provider, session), "Cities");
    initializeStrategyIfEmpty(lanes, () -> new LaneStrategy(provider, session, cities), "Lanes");
    initializeStrategyIfEmpty(tracks, () -> new TrackStrategy(provider, session, lanes), "Tracks");
    initializeStrategyIfEmpty(intersections, () -> new IntersectionStrategy(provider, session, tracks), "Intersections");
  
    runAllTests();
  }

  public void testUpdateDirections() {
    LOGGER.info("Actualizando direcciones de las calles basado en geometría...");
    routeService.updateTrackDirectionsFromGeometry();
    LOGGER.info("Direcciones actualizadas.");

    Collection<Track> tracks = trackRepository.findAll();
    LOGGER.info("\nEstadísticas de direcciones:");
    long bidirectional = tracks.stream().filter(t -> t.getDirection() == Track.Direction.BIDIRECTIONAL).count();
    long forward = tracks.stream().filter(t -> t.getDirection() == Track.Direction.FORWARD).count();
    long backward = tracks.stream().filter(t -> t.getDirection() == Track.Direction.BACKWARD).count();

    LOGGER.info("- Calles bidireccionales: {}", bidirectional);
    LOGGER.info("- Calles dirección forward: {}", forward);
    LOGGER.info("- Calles dirección backward: {}", backward);
  }

  public void testFindRoute(String startStreet, String endStreet) {
    LOGGER.info("\nBuscando ruta entre '{}' y '{}'...", startStreet, endStreet);
    
    List<Track> shortestPath = routeService.findShortestPath(startStreet, endStreet);
    if (shortestPath.isEmpty()) {
      LOGGER.info("No se encontró ruta directa.");
    } else {
      LOGGER.info("\nRuta más corta encontrada:");
      printRoute(shortestPath);
    }

    List<Track> alternativeRoutes = routeService.findAlternativeRoutes(startStreet, endStreet);
    if (!alternativeRoutes.isEmpty()) {
      LOGGER.info("\nRutas alternativas encontradas: {}", alternativeRoutes.size());
      for (int i = 0; i < alternativeRoutes.size(); i++) {
        LOGGER.info("\nRuta alternativa {}:", i + 1);
        printRoute(List.of(alternativeRoutes.get(i)));
      }
    } else {
      LOGGER.info("No se encontraron rutas alternativas.");
    }
  }

  private void printRoute(List<Track> route) {
    double totalDistance = 0;
    for (Track track : route) {
      LOGGER.info("- {} (Dir: {}, Dist: {}m)",
          track.getName(),
          track.getDirection(),
          String.format("%.2f", track.getDistance()));
      totalDistance += track.getDistance();
    }
    LOGGER.info("Distancia total: {}m", String.format("%.2f", totalDistance));
  }

  public void testAddAlternativeRoute(String trackId1, String trackId2) {
    LOGGER.info("\nAñadiendo ruta alternativa entre tracks {} y {}", trackId1, trackId2);
    routeService.addAlternativeRoute(trackId1, trackId2);
    LOGGER.info("Ruta alternativa añadida.");
  }

  public void runAllTests() {
    LOGGER.info("Iniciando pruebas de rutas...\n");
    
    testUpdateDirections();
    
    testFindRoute("ANTONI MAURA", "ALEXANDRE ROSSELLÓ");
    
    testAddAlternativeRoute("TRACK-1", "TRACK-2");
    
    LOGGER.info("\nPruebas completadas.");
  }
}
