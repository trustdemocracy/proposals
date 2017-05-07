package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.endpoints.App;
import eu.trustdemocracy.proposals.infrastructure.InteractorFactory;
import io.vertx.ext.web.Router;

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
}
