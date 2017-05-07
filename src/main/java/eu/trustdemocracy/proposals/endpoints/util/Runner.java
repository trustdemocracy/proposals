package eu.trustdemocracy.proposals.endpoints.util;

import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import java.util.function.Consumer;
import lombok.val;

public final class Runner {

  public static void runVerticle(String verticleID) {
    val options = new VertxOptions();
    Consumer<Vertx> runner = vertx -> {
      try {
        vertx.deployVerticle(verticleID);
      } catch (Throwable t) {
        t.printStackTrace();
      }
    };
    if (options.isClustered()) {
      Vertx.clusteredVertx(options, res -> {
        if (res.succeeded()) {
          Vertx vertx = res.result();
          runner.accept(vertx);
        } else {
          res.cause().printStackTrace();
        }
      });
    } else {
      Vertx vertx = Vertx.vertx(options);
      runner.accept(vertx);
    }
  }
}
