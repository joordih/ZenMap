package dev.joordih.zenmap.managers.nodes.track;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import dev.joordih.zenmap.managers.nodes.Node;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;
import org.neo4j.ogm.annotation.Relationship;
import org.neo4j.ogm.annotation.Transient;
import org.neo4j.ogm.annotation.typeconversion.Convert;
import org.neo4j.ogm.typeconversion.AttributeConverter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Getter
@Setter
@NodeEntity
public class Track implements Node {

  public enum Direction {
    BIDIRECTIONAL,
    FORWARD,
    BACKWARD
  }

  public static class DirectionConverter implements AttributeConverter<Direction, String> {
    @Override
    public String toGraphProperty(Direction value) {
      return value != null ? value.name() : null;
    }

    @Override
    public Direction toEntityAttribute(String value) {
      return value != null ? Direction.valueOf(value) : null;
    }
  }

  @Id
  @Property(name = "id")
  @JsonProperty("id")
  private String id;
  @JsonProperty("idIne")
  private String idIne;
  @JsonProperty("nom")
  private String name;
  @JsonProperty("idMunicipi")
  private String postalCode;
  @JsonProperty("tipusVia")
  private String trackType;
  @JsonProperty("longitud")
  private String longitude;
  @JsonProperty("x")
  private double x;
  @JsonProperty("y")
  private double y;
  @JsonProperty("geometry")
  @JsonDeserialize(using = GeometryDeserializer.class)
  private String geometry;
  @Property(name = "distance")
  private double distance;
  @Property(name = "trafficLevel")
  private int trafficLevel;
  @Property(name = "isAlternative")
  private boolean isAlternative;
  @Property(name = "direction")
  @Convert(DirectionConverter.class)
  private Direction direction = Direction.BIDIRECTIONAL;
  @Transient
  private List<double[]> geometryPoints;
  @Relationship(type = "ALTERNATIVE_ROUTE", direction = Relationship.Direction.UNDIRECTED)
  private Set<Track> alternativeRoutes;
  @Relationship(type = "CONNECTS", direction = Relationship.Direction.UNDIRECTED)
  private Set<Node> connectedNodes;

  public Track() {
    this.alternativeRoutes = new HashSet<>();
    this.connectedNodes = new HashSet<>();
    this.geometryPoints = new ArrayList<>();
  }

  public Track(String id) {
    this();
    this.id = id;
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public Track(
      @JsonProperty("id") String id,
      @JsonProperty("idIne") String idIne,
      @JsonProperty("nom") String name,
      @JsonProperty("idMunicipi") String postalCode,
      @JsonProperty("tipusVia") String trackType,
      @JsonProperty("longitud") String longitude,
      @JsonProperty("x") double x,
      @JsonProperty("y") double y,
      @JsonProperty("geometry") String geometry
  ) {
    this();
    this.id = id;
    this.idIne = idIne;
    this.name = name;
    this.postalCode = postalCode;
    this.trackType = trackType;
    this.longitude = longitude;
    this.x = x;
    this.y = y;
    this.geometry = geometry;
    parseGeometryPoints();
  }

  private void parseGeometryPoints() {
    if (this.geometry == null) return;
    try {
      ObjectMapper mapper = new ObjectMapper();
      JsonNode geometryNode = mapper.readTree(this.geometry);
      if (!geometryNode.isArray() || geometryNode.size() == 0) return;
      JsonNode coordinates = geometryNode.get(0);
      if (!coordinates.isArray()) return;

      for (JsonNode point : coordinates) {
        if (point.isArray() && point.size() >= 2) {
          double[] coord = new double[]{point.get(0).asDouble(), point.get(1).asDouble()};
          this.geometryPoints.add(coord);
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public boolean isValidDirection(Track nextTrack) {
    if (this.direction == Direction.BIDIRECTIONAL || nextTrack.getDirection() == Direction.BIDIRECTIONAL) {
      return true;
    }

    if (this.geometryPoints.isEmpty() || nextTrack.getGeometryPoints().isEmpty()) {
      return true;
    }

    double[] lastPoint = this.geometryPoints.get(this.geometryPoints.size() - 1);
    double[] firstPointNext = nextTrack.getGeometryPoints().get(0);

    boolean isConnected = calculateDistance(lastPoint[0], lastPoint[1], firstPointNext[0], firstPointNext[1]) < 10;

    if (this.direction == Direction.FORWARD) {
      return isConnected;
    } else if (this.direction == Direction.BACKWARD) {
      return !isConnected;
    }

    return true;
  }

  public void addAlternativeRoute(Track track) {
    if (track != null && !this.equals(track) && isValidDirection(track)) {
      this.alternativeRoutes.add(track);
      track.getAlternativeRoutes().add(this);
    }
  }

  public void removeAlternativeRoute(Track track) {
    if (track != null) {
      this.alternativeRoutes.remove(track);
      track.getAlternativeRoutes().remove(this);
    }
  }

  public double calculateDistance() {
    if (this.geometryPoints.isEmpty()) {
      parseGeometryPoints();
    }

    if (this.geometryPoints.size() < 2) return 0.0;

    double totalDistance = 0.0;
    for (int i = 0; i < this.geometryPoints.size() - 1; i++) {
      double[] point1 = this.geometryPoints.get(i);
      double[] point2 = this.geometryPoints.get(i + 1);
      totalDistance += calculateDistance(point1[0], point1[1], point2[0], point2[1]);
    }
    this.distance = totalDistance;
    return totalDistance;
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

  public static class GeometryDeserializer extends JsonDeserializer<String> {
    @Override
    public String deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
      JsonNode node = p.getCodec().readTree(p);
      if (node.isArray()) {
        return node.toString();
      }
      return node.asText();
    }
  }
}
