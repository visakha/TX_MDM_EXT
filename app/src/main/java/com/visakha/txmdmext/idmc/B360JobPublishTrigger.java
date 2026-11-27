package com.visakha.txmdmext.idmc;

import com.visakha.txmdmext.config.AppProperties;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.stream.Stream;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

/**
 * Mirrors the tx-mdm backend: v3 login, then POST {mdmUrl}/job/public/api/v1/jobInstance/{kind}
 * with header IDS-SESSION-ID. NOT run against Vamsi's org yet; 429 is a hard stop, no retry.
 */
public class B360JobPublishTrigger implements IdmcPublishTrigger {

  static final String LOGIN_PATH = "/saas/public/core/v3/login";
  static final String JOB_RUN_PATH = "/job/public/api/v1/jobInstance/";
  static final String SESSION_HEADER = "IDS-SESSION-ID";

  private final AppProperties.Idmc settings;
  private final HttpClient http;
  private final ObjectMapper mapper = JsonMapper.builder().build();

  public B360JobPublishTrigger(AppProperties.Idmc settings) {
    this.settings = settings;
    this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(30)).build();
  }

  @Override
  public String triggerPublish() {
    boolean incomplete =
        Stream.of(
                settings.loginUrl(),
                settings.mdmUrl(),
                settings.username(),
                settings.password(),
                settings.publishJobKind(),
                settings.publishJobDefinitionId())
            .anyMatch(v -> v == null || v.isBlank());
    if (incomplete) {
      return "IDMC publish trigger skipped: txmdmext.idmc.* settings are incomplete";
    }
    try {
      JsonNode login = post(settings.loginUrl() + LOGIN_PATH, loginBody(), null);
      String sessionId = login.path("userInfo").path("sessionId").asString();
      ObjectNode job = mapper.createObjectNode();
      job.put("jobDefinitionId", settings.publishJobDefinitionId());
      String url = settings.mdmUrl() + JOB_RUN_PATH + settings.publishJobKind();
      JsonNode run = post(url, job.toString(), sessionId);
      return "IDMC publish job started: " + run.toString();
    } catch (IOException e) {
      return "IDMC publish trigger failed: " + e.getMessage();
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      return "IDMC publish trigger interrupted";
    }
  }

  private String loginBody() {
    ObjectNode body = mapper.createObjectNode();
    body.put("username", settings.username());
    body.put("password", settings.password());
    return body.toString();
  }

  private JsonNode post(String url, String body, String sessionId)
      throws IOException, InterruptedException {
    HttpRequest.Builder req =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(60))
            .header("Content-Type", "application/json")
            .header("Accept", "application/json")
            .POST(HttpRequest.BodyPublishers.ofString(body));
    if (sessionId != null) {
      req.header(SESSION_HEADER, sessionId);
    }
    HttpResponse<String> res = http.send(req.build(), HttpResponse.BodyHandlers.ofString());
    // Never echo the request body: the login body carries the password.
    return switch (res.statusCode()) {
      case 200, 201, 202 -> mapper.readTree(res.body());
      case 429 -> throw new IOException("HTTP 429 rate limited by Informatica - not retrying");
      default ->
          throw new IOException("HTTP " + res.statusCode() + " from " + URI.create(url).getPath());
    };
  }
}
