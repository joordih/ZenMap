package dev.joordih.zenmap.managers.nodes;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import org.neo4j.ogm.annotation.GeneratedValue;
import org.neo4j.ogm.annotation.Id;
import org.neo4j.ogm.annotation.NodeEntity;
import org.neo4j.ogm.annotation.Property;

@Getter
@Setter
@NodeEntity
public class Path {

  @Id @GeneratedValue @Property(name = "_id")
  @Setter(AccessLevel.NONE)
  private Long _id;

  @Property(name = "id")
  private String id;
  private String name;
  private String postalCode;

  public Path(
      String id,
      String name,
      String postalCode
  ) {
    this.id = id;
    this.name = name;
    this.postalCode = postalCode;


  }

}
