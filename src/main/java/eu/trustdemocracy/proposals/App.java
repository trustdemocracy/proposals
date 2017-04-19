package eu.trustdemocracy.proposals;

import io.vertx.core.AbstractVerticle;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

public class App extends AbstractVerticle {

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/").handler(this::handleProposals);

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void handleProposals(RoutingContext routingContext) {
        routingContext.response().putHeader("content-type", "application/json").end("{'status': 'ok'}");
    }

}
