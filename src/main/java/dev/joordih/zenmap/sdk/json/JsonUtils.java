package dev.joordih.zenmap.sdk.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.Getter;

import java.io.IOException;

@SuppressWarnings("all")
public class JsonUtils {

  @Getter
  private static final ObjectMapper MAPPER = new ObjectMapper()
      .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
      .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
      .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true)
      .registerModules(new JavaTimeModule(), new Jdk8Module());

  public static <T> T fromJson(String json, Class<T> clazz) throws IOException {
    return MAPPER.readValue(json, clazz);
  }

  public static <T> T fromJson(String json, TypeReference<T> typeReference) throws IOException {
    return MAPPER.readValue(json, typeReference);
  }

  public static String toJson(Object object) throws IOException {
    return MAPPER.writeValueAsString(object);
  }

  public static String toPrettyJson(String object) throws IOException {
    Object jsonObject = MAPPER.readValue(object, Object.class);
    String prettyJson = MAPPER.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
    return prettyJson;
  }
}
