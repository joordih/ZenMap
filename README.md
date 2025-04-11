# ZenMap

ZenMap is a Java-based application designed to manage and analyze geographical data using Neo4j, a graph database. The project focuses on modeling and querying street networks, allowing users to find the shortest paths between different locations.

## Features

- **Graph Database Integration**: Utilizes Neo4j to store and manage geographical data.
- **Street Network Modeling**: Represents streets, tracks, and intersections as nodes and relationships in a graph.
- **Shortest Path Calculation**: Provides functionality to calculate the shortest path between two street names.
- **Data Fetching and Processing**: Integrates with external data sources to fetch and process geographical data.

## Project Structure

- **`src/main/java`**: Contains the main Java source code.
  - **`dev/joordih/zenmap/managers/nodes`**: Defines the node entities such as `Lane`, `Track`, and `Intersection`.
  - **`dev/joordih/zenmap/managers/strategy`**: Contains strategies for data fetching and processing.
  - **`dev/joordih/zenmap/managers/repository`**: Provides repository interfaces for accessing and managing nodes in the database.

- **`config/settings.yml`**: Configuration file for project settings.

## Getting Started

### Prerequisites

- Java 23 or higher
- Neo4j Database
- Maven

### Installation

1. **Clone the repository**:
   ```bash
   git clone https://github.com/joordih/zenmap.git
   cd zenmap
   ```

2. **Configure Neo4j**:
   - Ensure Neo4j is installed and running.
   - Update the connection settings in `config/settings.yml`.

3. **Build the project**:
   ```bash
   mvn clean install
   ```

4. **Run the application**:
   ```bash
   mvn exec:java -Dexec.mainClass="dev.joordih.zenmap.Zenmap"
   ```

## Usage

- **Finding Shortest Path**:
  - Use the Neo4j query interface to execute queries for finding the shortest path between two street names.
  - Example query:
    ```cypher
    MATCH (startLane:Lane {name: "Carrer de la Pau"})
    MATCH (endLane:Lane {name: "Carrer de Sant Miquel"})
    MATCH (startTrack:Track)-[:CONNECTS]-(startLane)
    MATCH (endTrack:Track)-[:CONNECTS]-(endLane)
    MATCH path = shortestPath((startTrack)-[:CONNECTS*]-(endTrack))
    RETURN path
    ```

## Contributing

Contributions are welcome! Please fork the repository and submit a pull request for any improvements or bug fixes.

## License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## Contact

For questions or support, please contact jxaviermachor@gmail.com or open an issue on GitHub.
