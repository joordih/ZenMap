package dev.joordih.zenmap.managers.repository;

import dev.joordih.zenmap.managers.nodes.track.Track;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.neo4j.ogm.session.Session;

import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NeoObjectRepositoryTest {

    @Mock
    Session session;

    NeoObjectRepository<Track> repository;

    @BeforeEach
    void setUp() {
        repository = new NeoObjectRepository<>(session, Track.class);
    }

    @Test
    void findDelegatesToSessionLoad() {
        Track track = new Track("id");
        when(session.load(Track.class, "id")).thenReturn(track);

        Track result = repository.find("id");

        assertSame(track, result);
        verify(session).load(Track.class, "id");
    }

    @Test
    void findByPostalCodeReturnsResults() {
        Track track = new Track("id");
        Iterable<Track> iterable = List.of(track);
        when(session.query(eq(Track.class), anyString(), anyMap())).thenReturn(iterable);

        List<Track> result = repository.findByPostalCode("07001");

        assertEquals(1, result.size());
        assertSame(track, result.get(0));
        verify(session).query(eq(Track.class), contains("MATCH (n:Track"), anyMap());
    }

    @Test
    void findAllDelegatesToLoadAll() {
        Collection<Track> coll = List.of(new Track("id"));
        when(session.loadAll(Track.class)).thenReturn(coll);

        Collection<Track> result = repository.findAll();

        assertEquals(coll, result);
        verify(session).loadAll(Track.class);
    }

    @Test
    void saveDelegatesToSession() {
        Track track = new Track("id");
        repository.save(track);
        verify(session).save(track);
    }

    @Test
    void deleteLoadsAndDeletes() {
        Track track = new Track("id");
        when(session.load(Track.class, "id")).thenReturn(track);

        repository.delete(track);

        verify(session).load(Track.class, "id");
        verify(session).delete(track);
    }
}
