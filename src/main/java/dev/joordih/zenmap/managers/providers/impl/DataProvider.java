package dev.joordih.zenmap.managers.providers.impl;

//@ProviderParams(
//    name = "Data scrapper",
//    priority = ProviderPriority.NORMAL
//)
//@SuppressWarnings("all")
public class DataProvider {
//
//  private final OkHttpClient client;
//  private final ObjectMapper mapper = JsonUtils.getMAPPER();
//
//  public DataProvider() {
//    OkHttpClient.Builder builder = new OkHttpClient.Builder()
//        .connectTimeout(30, TimeUnit.SECONDS)
//        .readTimeout(30, TimeUnit.SECONDS)
//        .writeTimeout(30, TimeUnit.SECONDS);
//
//    try {
//      final TrustManager[] trustAllCerts = new TrustManager[]{
//          new X509TrustManager() {
//            @Override
//            public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            }
//
//            @Override
//            public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {
//            }
//
//            @Override
//            public X509Certificate[] getAcceptedIssuers() {
//              return new X509Certificate[0];
//            }
//          }
//      };
//
//      final SSLContext sslContext = SSLContext.getInstance("TLS");
//      sslContext.init(null, trustAllCerts, new SecureRandom());
//
//      final SSLSocketFactory sslSocketFactory = sslContext.getSocketFactory();
//
//      builder.sslSocketFactory(sslSocketFactory, (X509TrustManager) trustAllCerts[0])
//          .hostnameVerifier((hostname, session) -> true);
//
//    } catch (Exception e) {
//      System.err.println("Error configurando SSL para el DataProvider: " + e.getMessage());
//      e.printStackTrace();
//    }
//
//    this.client = builder.build();
//  }
//
//  @Override
//  public void register() {
//    System.out.println("Iniciando descarga de datos...");
//
//    if (!downloadData("https://ideib.caib.es/adreces/rest/vies/07014")) {
//      System.out.println("Intentando con HTTP como alternativa...");
//      downloadData("http://ideib.caib.es/adreces/rest/vies/07014");
//    }
//  }
//
//  private boolean downloadData(String url) {
//    try {
//      Request request = new Request.Builder()
//          .url(url)
//          .get()
//          .build();
//
//      System.out.println("Conectando a: " + url);
//
//      try (Response response = client.newCall(request).execute()) {
//        if (!response.isSuccessful()) {
//          System.err.println("Error al descargar datos. Código: " + response.code());
//          return false;
//        }
//
//        String jsonData = this.prettyJson(response.body().string());
//        System.out.println("Datos descargados correctamente (" + jsonData.length() + " bytes)");
//
//        Path directoryPath = Paths.get("data");
//        if (!Files.exists(directoryPath)) {
//          Files.createDirectories(directoryPath);
//          System.out.println("Creado directorio: " + directoryPath);
//        }
//
//        Path filePath = directoryPath.resolve("data.json");
//
//        List<Lane> lanes = this.mapper.readValue(jsonData, new TypeReference<List<Lane>>() {});
//
//        Neo4jProvider provider = new Neo4jProvider();
//        Session session = provider.getSessionFactory().openSession();
//
//        for (int i = 0; i < lanes.size(); i++) {
//          NeoObjectRepository<Lane> repository = new NeoObjectRepository<>(session, Lane.class);
//          Lane lane = lanes.get(i);
//          repository.save(lane);
//          System.out.println("agregado " + i);
//        }
//
//        Files.write(filePath, jsonData.getBytes());
//        System.out.println("Datos guardados en: " + filePath.toAbsolutePath());
//
//        return true;
//      }
//    } catch (IOException e) {
//      System.err.println("Error de I/O al descargar datos: " + e.getMessage());
//      e.printStackTrace();
//      return false;
//    } catch (Exception e) {
//      System.err.println("Error general al descargar datos: " + e.getMessage());
//      e.printStackTrace();
//      return false;
//    }
//  }
//
//  public String prettyJson(String json) {
//    try {
//      mapper.registerModule(new Jdk8Module());
//      Object jsonObject = mapper.readValue(json, Object.class);
//      String prettyJson = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(jsonObject);
//
//
//      return prettyJson;
//    } catch (Exception e) {
//      System.err.println("Error al formatear el JSON: " + e.getMessage());
//      e.printStackTrace();
//      return json;
//    }
//  }
//
//  public Path getFromJson(String json) {
//    try {
//      return JsonUtils.fromJson(json, Path.class);
//    } catch (Exception e) {
//      System.err.println("Error al formatear el JSON: " + e.getMessage());
//      e.printStackTrace();
//      return null;
//    }
//  }
//
//  @Override
//  public void unregister() {
//    System.out.println("DataProvider desregistrado");
//  }
}