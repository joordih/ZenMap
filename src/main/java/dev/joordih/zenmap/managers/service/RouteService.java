package dev.joordih.zenmap.managers.service;

import dev.joordih.zenmap.managers.nodes.track.Track;
import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import org.neo4j.ogm.session.Session;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class RouteService {
    private final Session session;

    public RouteService() {
        this.session = Neo4jProvider.getSessionFactory().openSession();
    }

    public void updateTrackDirectionsFromGeometry() {
        String query = """
            MATCH (t:Track)
            WHERE t.geometry IS NOT NULL
            RETURN t
            """;

        Iterable<Track> tracks = session.query(Track.class, query, Map.of());
        for (Track track : tracks) {
            Track.Direction direction = determineDirectionFromGeometry(track);
            updateTrackDirection(track.getId(), direction);
        }
    }

    @SuppressWarnings("unused")
    private Track.Direction determineDirectionFromGeometry(Track track) {
        if (track.getGeometryPoints().isEmpty()) {
            return Track.Direction.BIDIRECTIONAL;
        }

        double[] firstPoint = track.getGeometryPoints().get(0);
        double[] lastPoint = track.getGeometryPoints().get(track.getGeometryPoints().size() - 1);

        String intersectionQuery = """
            MATCH (t:Track {id: $trackId})-[:CONNECTS]-(i:Intersection)
            RETURN i
            ORDER BY i.x, i.y
            """;

        Iterable<Map<String, Object>> intersections = session.query(intersectionQuery, Map.of("trackId", track.getId()));
        List<Map<String, Object>> intersectionList = StreamSupport.stream(intersections.spliterator(), false)
            .collect(Collectors.toList());

        if (intersectionList.size() >= 2) {
            Map<String, Object> startIntersection = intersectionList.get(0);
            Map<String, Object> endIntersection = intersectionList.get(intersectionList.size() - 1);

            double startX = (double) startIntersection.get("x");
            double startY = (double) startIntersection.get("y");
            double endX = (double) endIntersection.get("x");
            double endY = (double) endIntersection.get("y");

            double distStartFirst = calculateDistance(startX, startY, firstPoint[0], firstPoint[1]);
            double distStartLast = calculateDistance(startX, startY, lastPoint[0], lastPoint[1]);

            if (distStartFirst < distStartLast) {
                return Track.Direction.FORWARD;
            } else {
                return Track.Direction.BACKWARD;
            }
        }

        return Track.Direction.BIDIRECTIONAL;
    }

    private double calculateDistance(double x1, double y1, double x2, double y2) {
        double R = 6371000;
        double dLat = Math.toRadians(y2 - y1);
        double dLon = Math.toRadians(x2 - x1);
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
            Math.cos(Math.toRadians(y1)) * Math.cos(Math.toRadians(y2)) *
                Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return R * c;
    }

    public List<Track> findShortestPath(String startLaneName, String endLaneName) {
        String query = """
            MATCH (startLane:Lane {name: $startName})
            MATCH (endLane:Lane {name: $endName})
            MATCH (startTrack:Track)-[:CONNECTS]-(startLane)
            MATCH (endTrack:Track)-[:CONNECTS]-(endLane)
            MATCH path = shortestPath((startTrack)-[:CONNECTS*]-(endTrack))
            WHERE ALL(i in range(0, size(nodes(path))-1) 
                WHERE nodes(path)[i].direction IN ['BIDIRECTIONAL', 'FORWARD'])
            RETURN path
            """;

        Map<String, Object> params = Map.of(
            "startName", startLaneName,
            "endName", endLaneName
        );

        Iterable<Track> result = session.query(Track.class, query, params);
        return StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());
    }

    public List<Track> findAlternativeRoutes(String startLaneName, String endLaneName) {
        String query = """
            MATCH (startLane:Lane {name: $startName})
            MATCH (endLane:Lane {name: $endName})
            MATCH (startTrack:Track)-[:CONNECTS]-(startLane)
            MATCH (endTrack:Track)-[:CONNECTS]-(endLane)
            MATCH path = (startTrack)-[:CONNECTS|ALTERNATIVE_ROUTE*]-(endTrack)
            WHERE ALL(i in range(0, size(nodes(path))-1) 
                WHERE nodes(path)[i].direction IN ['BIDIRECTIONAL', 'FORWARD'])
                AND length(path) <= 10
            RETURN path
            ORDER BY reduce(dist = 0, t IN [t in nodes(path) WHERE t:Track] | dist + t.distance)
            LIMIT 5
            """;

        Map<String, Object> params = Map.of(
            "startName", startLaneName,
            "endName", endLaneName
        );

        Iterable<Track> result = session.query(Track.class, query, params);
        return StreamSupport.stream(result.spliterator(), false).collect(Collectors.toList());
    }

    public void updateTrackDirection(String trackId, Track.Direction direction) {
        String query = """
            MATCH (t:Track {id: $trackId})
            SET t.direction = $direction
            """;

        Map<String, Object> params = Map.of(
            "trackId", trackId,
            "direction", direction.name()
        );

        session.query(query, params);
    }

    public void addAlternativeRoute(String trackId1, String trackId2) {
        String query = """
            MATCH (t1:Track {id: $trackId1})
            MATCH (t2:Track {id: $trackId2})
            WHERE t1.direction = 'BIDIRECTIONAL' OR t2.direction = 'BIDIRECTIONAL'
                OR (t1.direction = 'FORWARD' AND t2.direction = 'FORWARD')
            CREATE (t1)-[:ALTERNATIVE_ROUTE]-(t2)
            """;

        Map<String, Object> params = Map.of(
            "trackId1", trackId1,
            "trackId2", trackId2
        );

        session.query(query, params);
    }
} 