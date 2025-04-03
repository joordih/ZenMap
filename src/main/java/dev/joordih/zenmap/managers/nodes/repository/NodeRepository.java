package dev.joordih.zenmap.managers.nodes.repository;

import dev.joordih.zenmap.managers.nodes.Node;

public interface NodeRepository<T extends Node> {
  T find(String id);

  void save(T node);

  void delete(T node);
}
