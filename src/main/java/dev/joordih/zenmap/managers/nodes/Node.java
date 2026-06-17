package dev.joordih.zenmap.managers.nodes;

import com.fasterxml.jackson.annotation.JsonProperty;

public interface Node {
  @JsonProperty("id")
  String getId();
}
