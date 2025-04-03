package dev.joordih.zenmap.managers.nodes.postal;

import com.fasterxml.jackson.annotation.JsonProperty;
import dev.joordih.zenmap.managers.nodes.Node;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.NodeEntity;

@Getter
@Setter
@NodeEntity
public class Postal implements Node {

  @JsonProperty("id")
  private String id;


}
