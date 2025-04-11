package dev.joordih.zenmap.managers.repository;

import com.google.common.collect.Lists;
import dev.joordih.zenmap.managers.nodes.Node;
import org.neo4j.ogm.session.Session;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

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
  public List<T> findByPostalCode(String postalCode) {
    List<T> results = Lists.newArrayList();
    Iterable<T> resultIterations = session.query(clazz, "MATCH (n:Track {postalCode: $postalCode}) RETURN n",
        Map.of("postalCode", postalCode));
    return resultIterations.spliterator().tryAdvance(results::add) ? results : Collections.emptyList();
  }

  @Override
  public Collection<T> findAll() {
    return session.loadAll(clazz);
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
