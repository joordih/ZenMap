package dev.joordih.zenmap.managers.nodes.track;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.joordih.zenmap.managers.nodes.Node;
import lombok.Getter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Getter
@NodeEntity
public class Track implements Node {

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

  @SuppressWarnings("unused")
  public Track() {
  }

  @SuppressWarnings("unused")
  public Track(String id) {
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
      @JsonProperty("y") double y
  ) {
    this.id = id;
    this.idIne = idIne;
    this.name = name;
    this.postalCode = postalCode;
    this.trackType = trackType;
    this.longitude = longitude;
    this.x = x;
    this.y = y;
  }
}
