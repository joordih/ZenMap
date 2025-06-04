package dev.joordih.zenmap.managers.service;

import dev.joordih.zenmap.managers.nodes.track.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.ogm.session.Session;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RouteServiceTest {

    @Mock
    Session session;

    RouteService service;

    @BeforeEach
    void setUp() {
        service = new RouteService(session);
    }

    @Test
    void findShortestPathReturnsQueryResults() {
        Track track = new Track("id");
        Iterable<Track> iterable = List.of(track);
        when(session.query(eq(Track.class), anyString(), anyMap())).thenReturn(iterable);

        List<Track> result = service.findShortestPath("A", "B");

        assertEquals(1, result.size());
        assertSame(track, result.get(0));
        verify(session).query(eq(Track.class), contains("shortestPath"), anyMap());
    }

    @Test
    void addAlternativeRouteExecutesQuery() {
        service.addAlternativeRoute("1", "2");
        verify(session).query(anyString(), eq(Map.of("trackId1", "1", "trackId2", "2")));
    }

    @Test
    void updateTrackDirectionExecutesQuery() {
        service.updateTrackDirection("1", Track.Direction.FORWARD);
        verify(session).query(anyString(), eq(Map.of("trackId", "1", "direction", "FORWARD")));
    }
}
