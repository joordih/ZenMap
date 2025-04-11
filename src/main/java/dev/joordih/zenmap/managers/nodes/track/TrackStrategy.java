package dev.joordih.zenmap.managers.nodes.track;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import dev.joordih.zenmap.managers.nodes.lane.Lane;
import dev.joordih.zenmap.managers.providers.impl.Neo4jProvider;
import dev.joordih.zenmap.managers.repository.NeoObjectRepository;
import dev.joordih.zenmap.managers.strategy.StrategyFactory;
import dev.joordih.zenmap.managers.strategy.fetch.DataFetchStrategy;
import dev.joordih.zenmap.managers.strategy.fetch.HttpDataFetchStrategy;
import dev.joordih.zenmap.sdk.json.JsonUtils;
import lombok.Getter;
import org.neo4j.ogm.session.Session;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@Getter
public class TrackStrategy {
  private static final Logger LOGGER = Logger.getLogger(TrackStrategy.class.getName());
  private static final String TRACKS_JSON_FILE = "tracks_data.json";

  private final Neo4jProvider provider;
  private final Session session;
  private final NeoObjectRepository<Track> trackRepository;

  public TrackStrategy(Neo4jProvider provider, Session session, Collection<Lane> lanes) {
    this.provider = provider;
    this.session = session;
    this.trackRepository = new NeoObjectRepository<>(session, Track.class);

    LOGGER.info("------------------------------");
    LOGGER.info("Loading tracks...");
    LOGGER.info("------------------------------");

    HttpDataFetchStrategy<Track> trackFetchStrategy = new HttpDataFetchStrategy<>(session, Track.class);
    StrategyFactory.registerStrategy(
        DataFetchStrategy.class,
        Track.class,
        trackFetchStrategy
    );

//    processTracksFromLanes(new ArrayList<>(lanes));

    importTracksFromJson();
  }

  private void importTracksFromJson() {
    try {
      File jsonFile = new File(TRACKS_JSON_FILE);
      ObjectMapper objectMapper = JsonUtils.getMAPPER();

      JsonNode rootNode = objectMapper.readTree(jsonFile);
      List<Track> tracks = new ArrayList<>();

      for (JsonNode trackNode : rootNode) {
        ObjectNode trackObjectNode = trackNode.deepCopy();

        if (trackObjectNode.has("geometria") && trackObjectNode.get("geometria").isArray()) {
          String geometryString = trackObjectNode.get("geometria").toString();
          trackObjectNode.put("geometria", geometryString);
        }

        Track track = objectMapper.treeToValue(trackObjectNode, Track.class);
        tracks.add(track);
      }

      for (Track track : tracks) {
        session.save(track);
      }

      LOGGER.info("Se importaron " + tracks.size() + " tracks exitosamente.");
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Error al importar tracks desde JSON: " + e.getMessage(), e);
    }
  }

  @SuppressWarnings("unused")
  private void processTracksFromLanes(List<Lane> lanes) {
    List<Track> tracksToSave = new ArrayList<>();
    List<Track> tracksToUpdate = new ArrayList<>();

    try {
      Collection<Track> existingTracks = trackRepository.findAll();

      Map<String, Track> existingTracksMap = existingTracks.stream()
          .collect(Collectors.toMap(
              Track::getIdIne,
              track -> track,
              (track1, track2) -> track1
          ));

      List<Lane> lanesToProcessGeometry = new ArrayList<>();
      for (Lane lane : lanes) {
        if (lane.getIdIne() == null) {
          LOGGER.warning("Lane not found: " + lane.getId());
          continue;
        }

        Track existingTrack = existingTracksMap.get(lane.getIdIne());
        if (existingTrack != null) {
          if (existingTrack.getGeometry() == null) {
            LOGGER.info("Track exists but missing geometry: " + existingTrack.getIdIne());
            lanesToProcessGeometry.add(lane);
            existingTracksMap.remove(lane.getIdIne());
          } else {
            LOGGER.info("Track already exists with geometry: " + lane.getIdIne());
          }
        } else {
          lanesToProcessGeometry.add(lane);
        }
      }

      for (int i = 0; i < lanesToProcessGeometry.size(); i++) {
        Lane lane = lanesToProcessGeometry.get(i);
        String endpoint = String.format("https://ideib.caib.es/adreces/rest/via/%s?ine=1&geometria=1&wgs84=1", lane.getIdIne());
        LOGGER.info(String.format("Fetching track (%d of %d): %s", i + 1, lanesToProcessGeometry.size(), lane.getId()));

        try {
          String trackData = fetchTrackData(endpoint);
          Track track = parseTrackData(trackData);

          if (isValidTrack(track)) {
            if (existingTracksMap.containsKey(lane.getIdIne())) {
              Track existingTrack = existingTracksMap.get(lane.getIdIne());
              existingTrack.setGeometry(track.getGeometry());
              tracksToUpdate.add(existingTrack);
            } else {
              tracksToSave.add(track);
            }
          }
        } catch (Exception e) {
          LOGGER.log(Level.SEVERE, "Error fetching track: " + lane.getId(), e);
        }
      }

      List<Track> allModifiedTracks = new ArrayList<>();
      allModifiedTracks.addAll(tracksToSave);
      allModifiedTracks.addAll(tracksToUpdate);
      saveTracksToJson(allModifiedTracks);

      saveTracksToDatabase(tracksToSave, "Added new track");

      saveTracksToDatabase(tracksToUpdate, "Updated track geometry for");

    } catch (Exception e) {
      LOGGER.log(Level.SEVERE, "Error processing tracks", e);
    }
  }

  private String fetchTrackData(String endpoint) throws IOException {
    return StrategyFactory.execute(
        DataFetchStrategy.class,
        Track.class,
        (strategy, params) -> {
          try {
            return strategy.fetchData((String) params[0]);
          } catch (IOException e) {
            e.printStackTrace();
          }
          return null;
        },
        endpoint
    );
  }

  private Track parseTrackData(String trackData) throws IOException {
    return StrategyFactory.execute(
        DataFetchStrategy.class,
        Track.class,
        (strategy, params) -> {
          try {
            return (Track) strategy.parseJsonObject((String) params[0]);
          } catch (IOException e) {
            e.printStackTrace();
          }
          return null;
        },
        trackData
    );
  }

  private boolean isValidTrack(Track track) {
    if (track == null || track.getIdIne() == null || track.getId() == null || track.getPostalCode() == null) {
      LOGGER.warning("Invalid track: " + (track != null ? track.getIdIne() : "null"));
      return false;
    }
    return true;
  }

  private void saveTracksToJson(List<Track> tracks) {
    if (tracks.isEmpty()) {
      LOGGER.info("No tracks to save to JSON");
      return;
    }

    try {
      Gson gson = new GsonBuilder().setPrettyPrinting().create();
      Path filePath = Paths.get(TRACKS_JSON_FILE);

      List<Track> allTracks = new ArrayList<>(tracks);
      if (Files.exists(filePath)) {
        String existingContent = new String(Files.readAllBytes(filePath));
        if (!existingContent.isEmpty()) {
          Track[] existingTracks = gson.fromJson(existingContent, Track[].class);
          if (existingTracks != null) {
            for (Track track : existingTracks) {
              if (allTracks.stream().noneMatch(t -> t.getIdIne().equals(track.getIdIne()))) {
                allTracks.add(track);
              }
            }
          }
        }
      }

      try (FileWriter writer = new FileWriter(filePath.toFile())) {
        gson.toJson(allTracks, writer);
      }

      LOGGER.info("Saved " + tracks.size() + " tracks to JSON file: " + filePath.toAbsolutePath());
    } catch (IOException e) {
      LOGGER.log(Level.SEVERE, "Failed to save tracks to JSON", e);
    }
  }

  private void saveTracksToDatabase(List<Track> tracks, String logMessage) {
    for (Track track : tracks) {
      try {
        if (isValidTrack(track)) {
          session.save(track);
          LOGGER.info(logMessage + ": " + track.getIdIne() + " (" + track.getPostalCode() + ")");
        }
      } catch (Exception e) {
        LOGGER.log(Level.SEVERE, "Failed to save track to database: " + track.getIdIne(), e);
      }
    }
  }
}