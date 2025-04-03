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
  @JsonProperty("nom")
  private String name;
  @JsonProperty("idMunicipi")
  private String postalCode;

  @SuppressWarnings("unused")
  public Lane() {
  }

  @SuppressWarnings("unused")
  public Lane(String id) {
    this.id = id;
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public Lane(
      @JsonProperty("id") String id,
      @JsonProperty("nom") String name,
      @JsonProperty("idMunicipi") String postalCode
  ) {
    this.id = id;
    this.name = name;
    this.postalCode = postalCode;
  }
}