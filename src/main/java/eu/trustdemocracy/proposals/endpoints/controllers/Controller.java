package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.endpoints.APIMessages;
import eu.trustdemocracy.proposals.endpoints.App;
import eu.trustdemocracy.proposals.infrastructure.InteractorFactory;
import io.vertx.core.http.HttpHeaders;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import lombok.val;

public abstract class Controller {

  private App app;

  public Controller(App app) {
    this.app = app;
    buildRoutes();
  }

  protected Router getRouter() {
    return app.getRouter();
  }

  protected InteractorFactory getInteractorFactory() {
    return app.getInteractorFactory();
  }

  public abstract void buildRoutes();


  protected void serveJsonResponse(RoutingContext context, int statusCode, String response) {
    context.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(statusCode)
        .end(response);
  }

  protected void serveBadCredentials(RoutingContext context) {
    val json = new JsonObject()
        .put("message", APIMessages.BAD_CREDENTIALS);
    serveJsonResponse(context, 401, Json.encodePrettily(json));
  }

  protected void serveBadRequest(RoutingContext context) {
    val json = new JsonObject()
        .put("message", APIMessages.BAD_REQUEST);
    serveJsonResponse(context, 400, Json.encodePrettily(json));
  }

  protected String getAuthorizationToken(HttpServerRequest request) {
    val header = request.getHeader(HttpHeaders.AUTHORIZATION);

    if (header == null) {
      return "";
    }

    val parts = header.split(" ");
    return parts.length == 2 ? parts[1] : "";
  }
}
