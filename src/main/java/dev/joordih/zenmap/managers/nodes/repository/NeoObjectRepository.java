package dev.joordih.zenmap.managers.nodes.repository;

import dev.joordih.zenmap.managers.nodes.Node;
import org.neo4j.ogm.session.Session;

public class NeoObjectRepository<T extends Node> implements NodeRepository<T> {

  private final Session session;
  private final Class<T> clazz;

  public NeoObjectRepository(Session session, Class<T> clazz) {
    this.session = session;
    this.clazz = clazz;
  }

  @Override
  public T find(String id) {
    return session.load(clazz, id);
  }

  @Override
  public void save(T node) {
    session.save(node);
  }

  @Override
  public void delete(T node) {
    T object = find(node.getId());
    assert object != null;
    session.delete(object);
  }
}
