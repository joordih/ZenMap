package dev.joordih.zenmap.managers.nodes.intersection;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.joordih.zenmap.managers.nodes.repository.NeoObjectRepository;
import dev.joordih.zenmap.managers.nodes.track.Track;
import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import lombok.Getter;
import org.neo4j.ogm.session.Session;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

@Getter
public class IntersectionStrategy {
  private static final Logger LOGGER = Logger.getLogger(IntersectionStrategy.class.getName());
  private static final double COORDINATE_PRECISION = 0.00001;
  private static final int MIN_INTERSECTION_SIZE = 2;
  private static final double MIN_COORDINATE_VALUE = 0.00001;

  private final Neo4jProvider provider;
  private final Session session;
  private final NeoObjectRepository<Intersection> intersectionRepository;

  public IntersectionStrategy(Neo4jProvider provider, Session session, Collection<Track> tracks) {
    this.provider = provider;
    this.session = session;
    this.intersectionRepository = new NeoObjectRepository<>(session, Intersection.class);

    LOGGER.info("------------------------------");
    LOGGER.info("Loading intersections...");
    LOGGER.info("------------------------------");

    processIntersectionsFromTracks(new ArrayList<>(tracks));
  }

  private void processIntersectionsFromTracks(List<Track> tracks) {
    try {
      Map<String, Set<Track>> coordinateGroups = new HashMap<>();
      int processedTracks = 0;
      int validGeometryTracks = 0;

      for (Track track : tracks) {
        try {
          boolean trackProcessed = false;

          if (track.getGeometry() != null) {
            ObjectMapper mapper = new ObjectMapper();
            JsonNode geometryNode = mapper.readTree(track.getGeometry());

            if (geometryNode.isArray()) {
              for (JsonNode point : geometryNode) {
                if (point.isArray() && point.size() >= 2) {
                  double x = point.get(0).asDouble();
                  double y = point.get(1).asDouble();

                  if (isValidCoordinate(x) && isValidCoordinate(y)) {
                    addToCoordinateGroup(coordinateGroups, x, y, track);
                    trackProcessed = true;
                  }
                }
              }
            }
            if (trackProcessed) {
              validGeometryTracks++;
            }
          }

          double x = track.getX();
          double y = track.getY();
          if (isValidCoordinate(x) && isValidCoordinate(y)) {
            addToCoordinateGroup(coordinateGroups, x, y, track);
            trackProcessed = true;
          }

          if (!trackProcessed) {
            LOGGER.warning("Track " + track.getId() + " has no valid coordinates");
          }

          processedTracks++;
          if (processedTracks % 1000 == 0) {
            LOGGER.info("Processed " + processedTracks + " tracks (" + validGeometryTracks + " with valid geometry)");
          }
        } catch (Exception e) {
          LOGGER.log(Level.WARNING, "Error processing track: " + track.getId(), e);
        }
      }

      LOGGER.info("Found " + coordinateGroups.size() + " potential intersection points");

      List<Intersection> intersections = new ArrayList<>();
      for (Map.Entry<String, Set<Track>> entry : coordinateGroups.entrySet()) {
        if (entry.getValue().size() >= MIN_INTERSECTION_SIZE) {
          String[] coords = entry.getKey().split(",");
          double x = Double.parseDouble(coords[0]);
          double y = Double.parseDouble(coords[1]);

          String intersectionId = "INT-" + UUID.randomUUID();
          Intersection intersection = new Intersection(intersectionId, x, y);
          intersection.setConnectedNodes(new HashSet<>(entry.getValue()));
          intersection.updateConnectedNodesInfo();
          intersections.add(intersection);
        }
      }

      LOGGER.info("Creating " + intersections.size() + " intersections");

      intersections.sort((a, b) -> Integer.compare(b.getNumberOfConnections(), a.getNumberOfConnections()));

      for (Intersection intersection : intersections) {
        session.save(intersection);
        String trackInfo = intersection.getConnectedTrackIds().isEmpty() ?
            "(no track IDs)" : "(" + intersection.getConnectedTrackIds() + ")";

        LOGGER.info(String.format("Created intersection: %s at (%.6f, %.6f) with %d connected tracks %s %s",
            intersection.getId(),
            intersection.getX(),
            intersection.getY(),
            intersection.getNumberOfConnections(),
            trackInfo,
            intersection.getTrackDistances() != null && !intersection.getTrackDistances().isEmpty() ?
                "[" + intersection.getTrackDistances() + "]" : ""));
      }

      LOGGER.info(String.format("Successfully created %d intersections from %d tracks (%d with valid geometry)",
          intersections.size(), processedTracks, validGeometryTracks));
    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error processing intersections", e);
      e.printStackTrace();
    }
  }

  private boolean isValidCoordinate(double coord) {
    return !Double.isNaN(coord) && !Double.isInfinite(coord) && Math.abs(coord) >= MIN_COORDINATE_VALUE;
  }

  @SuppressWarnings("unused")
  private void addToCoordinateGroup(Map<String, Set<Track>> coordinateGroups, double x, double y, Track track) {
    double roundedX = Math.round(x / COORDINATE_PRECISION) * COORDINATE_PRECISION;
    double roundedY = Math.round(y / COORDINATE_PRECISION) * COORDINATE_PRECISION;
    String coordKey = String.format("%.6f,%.6f", roundedX, roundedY);
    coordinateGroups.computeIfAbsent(coordKey, k -> new HashSet<>()).add(track);
  }
} 