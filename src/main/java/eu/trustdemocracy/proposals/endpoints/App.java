package eu.trustdemocracy.proposals.endpoints;

import eu.trustdemocracy.proposals.endpoints.controllers.Controller;
import eu.trustdemocracy.proposals.endpoints.controllers.ProposalController;
import eu.trustdemocracy.proposals.endpoints.util.Runner;
import eu.trustdemocracy.proposals.infrastructure.DefaultInteractorFactory;
import eu.trustdemocracy.proposals.infrastructure.InteractorFactory;
import eu.trustdemocracy.proposals.infrastructure.JWTKeyFactory;
import io.vertx.core.AbstractVerticle;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import java.util.HashSet;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.val;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.lang.JoseException;

public class App extends AbstractVerticle {

  private static final Logger LOG = LoggerFactory.getLogger(App.class);
  private static final int DEFAULT_PORT = 8080;

  private static InteractorFactory interactorFactory = DefaultInteractorFactory.getInstance();

  private Router router;

  public static void main(String... args) {
    Runner.runVerticle(App.class.getName());
  }

  @Override
  public void start() {
    val port = config().getInteger("http.port", DEFAULT_PORT);

    vertx.executeBlocking(future -> {
      setKeys();
      router = Router.router(vertx);
      router.route().handler(BodyHandler.create());
      registerControllers();

      vertx.createHttpServer()
          .requestHandler(router::accept)
          .listen(port);

      future.complete();
    }, result -> {
      if (result.succeeded()) {
        LOG.info("App listening on port: " + port);
      } else {
        LOG.error("Failed to start verticle", result.cause());
      }
    });
  }

  private void registerControllers() {
    val controllers = Stream.of(
        ProposalController.class
    ).collect(Collectors.toCollection(HashSet<Class<? extends Controller>>::new));

    for (val controller : controllers) {
      try {
        val constructor = controller.getConstructor(App.class);
        constructor.newInstance(this);
      } catch (Exception e) {
        LOG.error("Failing to attach controller [" + controller.getName() + "]", e);
      }
    }
  }

  public Router getRouter() {
    return router;
  }

  public InteractorFactory getInteractorFactory() {
    return interactorFactory;
  }

  public static void setInteractorFactory(InteractorFactory interactorFactory) {
    if (interactorFactory == null) {
      throw new NullPointerException("InteractorFactory cannot be null");
    }
    App.interactorFactory = interactorFactory;
  }

  private void setKeys() {
    try {
      if (JWTKeyFactory.getPublicKey() == null) {
        val rsaJsonWebKey = RsaJwkGenerator.generateJwk(2048);
        JWTKeyFactory.setPrivateKey(rsaJsonWebKey.getPrivateKey());
        JWTKeyFactory.setPublicKey(rsaJsonWebKey.getPublicKey());
      }
    } catch (JoseException e) {
      throw new RuntimeException(e);
    }
  }

}
