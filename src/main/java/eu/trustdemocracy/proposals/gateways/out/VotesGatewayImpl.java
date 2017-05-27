package eu.trustdemocracy.proposals.gateways.out;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.client.WebClient;
import lombok.val;

public class VotesGatewayImpl implements VotesGateway {

  private Vertx vertx = Vertx.vertx();
  private static String host;
  private static Integer port;

  @Override
  public void registerProposal(Proposal proposal) {
    val event = new JsonObject()
        .put("id", proposal.getId().toString())
        .put("title", proposal.getTitle())
        .put("dueDate", proposal.getDueDate());

    WebClient.create(vertx)
        .post(getVotesPort(), getVotesHost(), "/proposals/register")
        .rxSendJson(event)
        .subscribe();
  }

  @Override
  public void unregisterProposal(Proposal proposal) {
    val json = new JsonObject()
        .put("id", proposal.getId().toString());

    WebClient.create(vertx)
        .post(getVotesPort(), getVotesHost(), "/proposals/unregister")
        .rxSendJson(json)
        .subscribe();
  }

  private static String getVotesHost() {
    if (host == null) {
      host = System.getenv("votes_host");
    }
    return host;
  }

  private static int getVotesPort() {
    if (port == null) {
      port = Integer.valueOf(System.getenv("votes_port"));
    }
    return port;
  }
}
