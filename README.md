# ZenMap

ZenMap is a proof-of-concept route planning engine designed for the island of Mallorca. The core idea is to calculate the **fastest route between two addresses** by factoring in real-time **traffic density** and the **type of mobility** — because a car route and a walking route are entirely different things, both in distance tolerance and in which roads are even accessible.

The system models the entire road network of Mallorca as a graph, where streets, intersections, and lanes are nodes connected by typed relationships. This allows the use of Neo4j's native graph traversal algorithms (shortest path, alternative routes) to find optimal paths efficiently.

---

## Features

- **Traffic-aware routing** — tracks carry a `trafficLevel` property that can be factored into path cost when querying routes.
- **Direction-aware traversal** — streets are modeled as directed (`FORWARD`, `BACKWARD`, `BIDIRECTIONAL`), preventing illegal routing through one-way streets.
- **Geometry-based direction inference** — street direction is automatically derived from GeoJSON coordinate sequences using the Haversine formula.
- **Shortest path & alternative routes** — exposes both `shortestPath` and ranked `ALTERNATIVE_ROUTE` Cypher queries against the graph.
- **HTTP data ingestion** — fetches road network data (lanes, tracks, intersections, cities) from external REST APIs and persists them into Neo4j automatically on first run.
- **Priority-based provider loading** — infrastructure providers (e.g., database) are discovered and registered via reflection, sorted by declared priority.
- **YAML-driven configuration** — all connection settings and app parameters live in a single `config/settings.yml` file.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 23 |
| Build | Apache Maven |
| Graph Database | Neo4j (Bolt protocol) + Neo4j OGM 4.x |
| HTTP Client | OkHttp 4 |
| JSON | Jackson Databind + Gson |
| Configuration | SnakeYAML |
| Utilities | Google Guava |
| Classpath Scanning | Reflections |
| Boilerplate Reduction | Lombok |
| Logging | Logback (SLF4J) |

---

## Architecture & Design Patterns

### Strategy Pattern
The data ingestion pipeline is fully strategy-driven. Each node type (`City`, `Lane`, `Track`, `Intersection`) has its own `*Strategy` class that encapsulates how data is fetched from the external API and persisted into Neo4j. This makes adding new node types a matter of implementing the interface, not modifying existing code.

### Factory Pattern
`StrategyFactory` provides a type-safe, generic registry that maps `(StrategyClass, ParameterClass)` pairs to instances using a custom `TypeToken` implementation. `ConfigurationFactory` centralises access to all configuration sections.

### Repository Pattern
`NeoObjectRepository<T>` wraps Neo4j OGM session operations behind a clean generic interface, keeping persistence logic out of business classes.

### Provider Pattern with Reflection
`ProviderManager` uses the Reflections library to auto-discover all `Provider` implementations on the classpath, reads their `@ProviderParams` annotation for metadata and priority, and registers them in the correct order at startup. This avoids manual wiring and makes the system extensible without modifying `ProviderManager`.

### Object Graph Mapping
Entities (`Track`, `Lane`, `City`, `Intersection`) are annotated with Neo4j OGM annotations (`@NodeEntity`, `@Id`, `@Property`, `@Relationship`) and Jackson annotations (`@JsonProperty`, `@JsonCreator`) simultaneously, enabling both graph persistence and JSON deserialization from the same POJO.

---

## Project Structure

```
ZenMap/
├── config/
│   └── settings.yml
├── src/main/java/dev/joordih/zenmap/
│   ├── Zenmap.java
│   ├── managers/
│   │   ├── config/
│   │   ├── nodes/
│   │   │   ├── city/
│   │   │   ├── intersection/
│   │   │   ├── lane/
│   │   │   └── track/
│   │   ├── providers/
│   │   ├── repository/
│   │   ├── service/
│   │   └── strategy/
│   └── sdk/
│       ├── config/
│       └── json/
└── pom.xml
```

---

## Getting Started

### Prerequisites

- Java 23+
- Apache Maven 3.8+
- Neo4j 5.x (local or remote instance)

### Setup

1. **Clone the repository**
   ```bash
   git clone https://github.com/joordih/zenmap.git
   cd zenmap
   ```

2. **Configure the database**  
   Edit `config/settings.yml` and set your Neo4j connection URI, username, and password.

3. **Build**
   ```bash
   mvn clean package
   ```

4. **Run**
   ```bash
   mvn exec:java -Dexec.mainClass="dev.joordih.zenmap.Zenmap"
   ```

   On first run the application will fetch road network data from the configured external APIs and populate the Neo4j graph automatically.

---

## Route Queries

Once the graph is populated you can query routes directly via the Neo4j Browser or through `RouteService`:

```cypher
-- Shortest path between two streets
MATCH (startLane:Lane {name: "ANTONI MAURA"})
MATCH (endLane:Lane {name: "ALEXANDRE ROSSELLÓ"})
MATCH (s:Track)-[:CONNECTS]-(startLane)
MATCH (e:Track)-[:CONNECTS]-(endLane)
MATCH path = shortestPath((s)-[:CONNECTS*]-(e))
WHERE ALL(n IN nodes(path) WHERE n.direction IN ['BIDIRECTIONAL', 'FORWARD'])
RETURN path
```

---

## License

This project is licensed under the MIT License — see the [LICENSE](LICENSE) file for details.
