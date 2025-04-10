package dev.joordih.zenmap.managers.nodes.repository;

import dev.joordih.zenmap.managers.nodes.Node;

import java.util.Collection;
import java.util.List;

public interface NodeRepository<T extends Node> {
  T find(String id);

  List<T> findByPostalCode(String postalCode);

  Collection<T> findAll();

  void save(T node);

  void delete(T node);
}
