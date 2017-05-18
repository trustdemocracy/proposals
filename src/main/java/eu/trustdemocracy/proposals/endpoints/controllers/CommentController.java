package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.CommentVoteRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.endpoints.App;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;
import lombok.val;

public class CommentController extends Controller {

  public CommentController(App app) {
    super(app);
  }

  @Override
  public void buildRoutes() {
    getRouter().post("/proposals/:proposalId/comments").handler(this::createComment);
    getRouter().get("/proposals/:proposalId/comments").handler(this::getComments);
    getRouter().delete("/proposals/:proposalId/comments/:commentId").handler(this::deleteComment);
    getRouter().post("/proposals/:proposalId/comments/:commentId").handler(this::voteComment);
  }

  private void createComment(RoutingContext routingContext) {
    val requestProposal = Json
        .decodeValue(routingContext.getBodyAsString(), CommentRequestDTO.class);
    val interactor = getInteractorFactory().getCreateComment();
    val comment = interactor.execute(requestProposal);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(201)
        .end(Json.encodePrettily(comment));
  }

  private void getComments(RoutingContext routingContext) {
    val proposalId = UUID.fromString(routingContext.pathParam("proposalId"));
    val interactor = getInteractorFactory().getGetComments();
    val commentList = interactor.execute(new ProposalRequestDTO().setId(proposalId));

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(Json.encodePrettily(commentList));
  }

  private void deleteComment(RoutingContext routingContext) {
    val proposalId = UUID.fromString(routingContext.pathParam("proposalId"));
    val commentId = UUID.fromString(routingContext.pathParam("commentId"));

    val commentRequest = new CommentRequestDTO()
        .setId(commentId)
        .setProposalId(proposalId);

    val interactor = getInteractorFactory().getDeleteComment();
    val comment = interactor.execute(commentRequest);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(Json.encodePrettily(comment));
  }

  private void voteComment(RoutingContext routingContext) {
    val requestVote = Json
        .decodeValue(routingContext.getBodyAsString(), CommentVoteRequestDTO.class);
    val interactor = getInteractorFactory().getVoteComment();
    val comment = interactor.execute(requestVote);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(Json.encodePrettily(comment));
  }
}
