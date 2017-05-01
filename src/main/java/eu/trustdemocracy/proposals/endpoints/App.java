package eu.trustdemocracy.proposals.endpoints;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.asyncsql.AsyncSQLClient;
import io.vertx.ext.asyncsql.MySQLClient;
import io.vertx.ext.sql.ResultSet;
import io.vertx.ext.sql.SQLConnection;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;

import java.util.List;

public class App extends AbstractVerticle {

    AsyncSQLClient mySQLClient;

    @Override
    public void start() {
        Router router = Router.router(vertx);
        router.route().handler(BodyHandler.create());
        router.get("/").handler(this::handleProposals);
        mySQLClient = MySQLClient.createShared(vertx,
                new JsonObject().put("host", "mysql")
                        .put("username", "root")
                        .put("password", "root")
                        .put("database", "proposals"));

        vertx.createHttpServer().requestHandler(router::accept).listen(8080);
    }

    private void handleProposals(RoutingContext routingContext) {
        System.out.println("Getting request");
        mySQLClient.getConnection(res -> {
            if (res.succeeded()) {
                System.out.println("Getting connection");


                SQLConnection connection = res.result();

                connection.query("SELECT title, id from proposals", res1 -> {
                    if (res1.succeeded()) {
                        // Get the result set
                        ResultSet resultSet = res1.result();
                        System.out.println("Getting query");

                        List<String> columnNames = resultSet.getColumnNames();

                        List<JsonArray> results = resultSet.getResults();

                        String result = "";
                        for (JsonArray row : results) {

                            result += row.encodePrettily();
                        }


                        routingContext.response().putHeader("content-type", "application/json").end(result);
                    } else {
                        // Failed!
                    }
                });

            } else {
                res.cause().printStackTrace();
            }
        });


    }

}
