package dev.joordih.zenmap.managers.nodes.intersection;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import dev.joordih.zenmap.managers.nodes.Node;
import dev.joordih.zenmap.managers.nodes.track.Track;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;

import java.util.*;
import java.util.stream.Collectors;

@Getter
@Setter
@NodeEntity
public class Intersection implements Node {
  @Id
  @Property(name = "id")
  @JsonProperty("id")
  private String id;

  @Property(name = "x")
  @JsonProperty("x")
  private double x;

  @Property(name = "y")
  @JsonProperty("y")
  private double y;

  @Property(name = "trackIdIne")
  @JsonProperty("trackIdIne")
  private String trackIdIne;

  @Property(name = "numberOfConnections")
  @JsonProperty("numberOfConnections")
  private int numberOfConnections;

  @Property(name = "connectedTrackIds")
  @JsonProperty("connectedTrackIds")
  private String connectedTrackIds;

  @Property(name = "connectedGeometries")
  @JsonProperty("connectedGeometries")
  private String connectedGeometries;

  @Property(name = "geometries")
  @JsonProperty("geometries")
  private String geometries;

  @Property(name = "trackDistances")
  @JsonProperty("trackDistances")
  private String trackDistances;

  @Relationship(type = "CONNECTS", direction = Relationship.Direction.OUTGOING)
  private Set<Node> connectedNodes;

  @SuppressWarnings("unused")
  public Intersection() {
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public Intersection(
      @JsonProperty("id") String id,
      @JsonProperty("x") double x,
      @JsonProperty("y") double y
  ) {
    this.id = id;
    this.x = x;
    this.y = y;
  }

  public void updateConnectedNodesInfo() {
    if (connectedNodes != null) {
      this.numberOfConnections = connectedNodes.size();
      List<Map<String, Object>> connectedGeometriesList = new ArrayList<>();
      List<Map<String, Object>> geometriesList = new ArrayList<>();

      this.connectedTrackIds = connectedNodes.stream()
          .map(node -> (Track) node)
          .map(Track::getId)
          .distinct()
          .limit(5)
          .collect(Collectors.joining(", "));

      this.trackDistances = connectedNodes.stream()
          .map(node -> (Track) node)
          .map(this::calculateTrackDistance)
          .filter(Objects::nonNull)
          .limit(5)
          .collect(Collectors.joining(", "));

      int index = 0;
      for (Node node : connectedNodes) {
        Track track = (Track) node;
        if (track.getGeometry() != null) {
          Map<String, Object> connectedGeometry = new HashMap<>();
          connectedGeometry.put("geometry", track.getGeometry());
          connectedGeometry.put("connectedTrackIdIne", track.getId());
          connectedGeometriesList.add(connectedGeometry);

          Map<String, Object> geometryInfo = new HashMap<>();
          geometryInfo.put("index", index);
          geometryInfo.put("distanceToNextOne", calculateTrackDistance(track));
          geometriesList.add(geometryInfo);
          index++;
        }
      }

      try {
        ObjectMapper mapper = new ObjectMapper();
        this.connectedGeometries = mapper.writeValueAsString(connectedGeometriesList);
        this.geometries = mapper.writeValueAsString(geometriesList);
      } catch (Exception e) {
        this.connectedGeometries = "[]";
        this.geometries = "[]";
      }
    }
  }

  private String calculateTrackDistance(Track track) {
    try {
      if (track.getGeometry() == null) return null;

      ObjectMapper mapper = new ObjectMapper();
      JsonNode geometryNode = mapper.readTree(track.getGeometry());

      if (!geometryNode.isArray() || geometryNode.size() == 0) return null;

      double totalDistance = 0.0;
      JsonNode coordinates = geometryNode.get(0);

      if (!coordinates.isArray() || coordinates.size() < 2) return null;

      JsonNode previousPoint = null;
      for (JsonNode point : coordinates) {
        if (point.isArray() && point.size() >= 2) {
          if (previousPoint != null) {
            double x1 = previousPoint.get(0).asDouble();
            double y1 = previousPoint.get(1).asDouble();
            double x2 = point.get(0).asDouble();
            double y2 = point.get(1).asDouble();

            totalDistance += calculateDistance(x1, y1, x2, y2);
          }
          previousPoint = point;
        }
      }

      return String.format("%s: %.2fm", track.getId(), totalDistance);
    } catch (Exception e) {
      return null;
    }
  }

  private double calculateDistance(double x1, double y1, double x2, double y2) {
    double R = 6371000;
    double dLat = Math.toRadians(y2 - y1);
    double dLon = Math.toRadians(x2 - x1);
    double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
        Math.cos(Math.toRadians(y1)) * Math.cos(Math.toRadians(y2)) *
            Math.sin(dLon / 2) * Math.sin(dLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
    return R * c;
  }
}
