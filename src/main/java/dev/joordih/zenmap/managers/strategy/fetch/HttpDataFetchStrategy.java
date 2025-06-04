package dev.joordih.zenmap.managers.strategy.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.joordih.zenmap.managers.nodes.Node;
import dev.joordih.zenmap.managers.repository.NeoObjectRepository;
import dev.joordih.zenmap.sdk.json.JsonUtils;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.neo4j.ogm.session.Session;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
@SuppressWarnings("unused")
public class HttpDataFetchStrategy<T extends Node> implements DataFetchStrategy<T> {

  private final Session session;
  private final OkHttpClient client;
  private final Class<T> clazz;
  private final NeoObjectRepository<T> repository;

  public HttpDataFetchStrategy(Session session, Class<T> clazz) {
    this(session, clazz, new OkHttpClient.Builder());
  }

  public HttpDataFetchStrategy(Session session, Class<T> clazz, OkHttpClient.Builder builder) {
    this.session = session;
    this.clazz = clazz;
    this.repository = new NeoObjectRepository<>(session, clazz);

    this.client = builder
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build();
  }

  @Override
  public String fetchData(String url) throws IOException {
    Request request = new Request.Builder()
        .url(url)
        .get()
        .build();

    try (Response response = client.newCall(request).execute()) {
      if (!response.isSuccessful()) {
        System.err.println("Error al descargar datos. Código: " + response.code());
        return "";
      }

      return JsonUtils.toPrettyJson(response.body().string());
    }
  }

  @Override
  public List<T> parseJsonList(String json) throws IOException {
    ObjectMapper mapper = JsonUtils.getMAPPER();
    TypeFactory typeFactory = mapper.getTypeFactory();
    return mapper.readValue(json, typeFactory.constructCollectionType(List.class, clazz));
  }

  @Override
  public T parseJsonObject(String json) throws IOException {
    ObjectMapper mapper = JsonUtils.getMAPPER();
    return mapper.readValue(json, clazz);
  }
}
