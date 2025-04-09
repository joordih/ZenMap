package dev.joordih.zenmap.managers.strategy.fetch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import dev.joordih.zenmap.managers.nodes.Node;
import dev.joordih.zenmap.managers.nodes.repository.NeoObjectRepository;
import dev.joordih.zenmap.sdk.json.JsonUtils;
import lombok.Getter;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.neo4j.ogm.session.Session;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.io.IOException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
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
    this.session = session;
    this.clazz = clazz;
    this.repository = new NeoObjectRepository<>(session, clazz);

    OkHttpClient.Builder builder = new OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS);

    try {
      final TrustManager[] trustAllCerts = new TrustManager[]{
          new X509TrustManager() {
            @Override
            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
            }
          }
      };

      final SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, trustAllCerts, new SecureRandom());

      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();

      builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
          .hostnameVerifier((hostname, sslSession) -> true);

    } catch (Exception e) {
      System.err.println("Error configurando SSL para el DataProvider: " + e.getMessage());
      e.printStackTrace();
    }

    this.client = builder.build();
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
