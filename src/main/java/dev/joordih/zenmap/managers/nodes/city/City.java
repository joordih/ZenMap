package dev.joordih.zenmap.managers.nodes.city;

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
public class City implements Node {

  @Id
  @Property(name = "id")
  @JsonProperty("id")
  private String id;
  @JsonProperty("nom")
  private String name;

  @SuppressWarnings("unused")
  public City() {
  }

  @SuppressWarnings("unused")
  public City(String id) {
    this.id = id;
  }

  @JsonCreator(mode = JsonCreator.Mode.PROPERTIES)
  public City(
      @JsonProperty("id") String id,
      @JsonProperty("nom") String name
  ) {
    this.id = id;
    this.name = name;
  }
}