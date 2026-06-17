package dev.joordih.zenmap.managers.nodes.lane;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import dev.joordih.zenmap.managers.nodes.Node;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Getter
@Setter
@NodeEntity
public class Lane implements Node {

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
  @JsonProperty("nucli")
  private String nucleus;

  public Lane() {}

  public Lane(String id) { this.id = id; }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public Lane(
      @JsonProperty("id") String id,
      @JsonProperty("idIne") String idIne,
      @JsonProperty("nom") String name,
      @JsonProperty("idMunicipi") String postalCode,
      @JsonProperty("nucli") String nucleus
  ) {
    this.id = id;
    this.idIne = idIne;
    this.name = name;
    this.postalCode = postalCode;
    this.nucleus = nucleus;
  }
}