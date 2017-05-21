package eu.trustdemocracy.proposals.gateways.events;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.Proposal;
import io.vertx.core.json.JsonObject;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.client.WebClient;
import lombok.val;

public class EventsGatewayImpl implements EventsGateway {

  private Vertx vertx = Vertx.vertx();
  private static String host;
  private static Integer port;

  @Override
  public void createPublicationEvent(Proposal proposal) {
    val serializedContent = new JsonObject()
        .put("id", proposal.getId())
        .put("title", proposal.getTitle())
        .put("brief", proposal.getBrief());

    val event = new JsonObject()
        .put("userId", proposal.getAuthor().getId())
        .put("username", proposal.getAuthor().getUsername())
        .put("type", "PUBLICATION")
        .put("timestamp", System.currentTimeMillis())
        .put("serializedContent", serializedContent);
    sendEvent(event);
  }

  @Override
  public void createCommentEvent(Comment comment) {
    val serializedContent = new JsonObject()
        .put("id", comment.getId())
        .put("proposalId", comment.getProposal().getId())
        .put("title", comment.getProposal().getTitle())
        .put("content", comment.getContent())
        .put("id", comment.getId());

    val event = new JsonObject()
        .put("userId", comment.getAuthor().getId())
        .put("username", comment.getAuthor().getUsername())
        .put("type", "COMMENT")
        .put("timestamp", comment.getTimestamp())
        .put("serializedContent", serializedContent);
    sendEvent(event);
  }

  private void sendEvent(JsonObject event) {
    WebClient.create(vertx)
        .post(getSocialPort(), getSocialHost(), "/events")
        .rxSendJson(event)
        .subscribe();
  }

  private static String getSocialHost() {
    if (host == null) {
      host = System.getenv("social_host");
    }
    return host;
  }

  private static int getSocialPort() {
    if (port == null) {
      port = Integer.valueOf(System.getenv("social_port"));
    }
    return port;
  }
}
