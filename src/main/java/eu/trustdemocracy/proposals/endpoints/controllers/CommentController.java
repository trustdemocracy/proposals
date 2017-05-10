package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.core.interactors.comment.CreateComment;
import eu.trustdemocracy.proposals.core.interactors.comment.DeleteComment;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
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
  }

  private void createComment(RoutingContext routingContext) {
    val requestProposal = Json
        .decodeValue(routingContext.getBodyAsString(), CommentRequestDTO.class);
    val interactor = getInteractorFactory().createCommentInteractor(CreateComment.class);
    val comment = interactor.execute(requestProposal);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(201)
        .end(Json.encodePrettily(comment));
  }

  private void getComments(RoutingContext routingContext) {
    val proposalId = UUID.fromString(routingContext.pathParam("proposalId"));
    val interactor = getInteractorFactory().createGetCommentsInteractor();
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

    val interactor = getInteractorFactory().createCommentInteractor(DeleteComment.class);
    val comment = interactor.execute(commentRequest);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(Json.encodePrettily(comment));
  }
}
