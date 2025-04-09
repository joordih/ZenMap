package dev.joordih.zenmap.managers.nodes.repository;

import dev.joordih.zenmap.managers.nodes.Node;

import java.util.Collection;

public interface NodeRepository<T extends Node> {
  T find(String id);

  T findByPostalCode(String postalCode);

  Collection<T> findAll();

  void save(T node);

  void delete(T node);
}
