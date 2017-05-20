package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
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
    getRouter().post("/proposals/:proposalId/comments/:commentId/vote").handler(this::voteComment);
  }

  private void createComment(RoutingContext routingContext) {
    CommentRequestDTO requestComment;
    try {
      if (routingContext.getBodyAsJson().isEmpty()) {
        throw new Exception();
      }
      requestComment = Json.decodeValue(routingContext.getBodyAsString(), CommentRequestDTO.class);
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }

    val authorToken = getAuthorizationToken(routingContext.request());
    requestComment.setAuthorToken(authorToken);

    val interactor = getInteractorFactory().getCreateComment();

    try {
      val comment = interactor.execute(requestComment);
      serveJsonResponse(routingContext, 201, Json.encodePrettily(comment));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException e) {
      serveNotFound(routingContext);
    }
  }

  private void getComments(RoutingContext routingContext) {
    UUID proposalId;
    try {
      proposalId = UUID.fromString(routingContext.pathParam("proposalId"));
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }

    val authorToken = getAuthorizationToken(routingContext.request());
    val requestProposal = new ProposalRequestDTO()
        .setId(proposalId)
        .setAuthorToken(authorToken);

    val interactor = getInteractorFactory().getGetComments();
    try {
      val commentList = interactor.execute(requestProposal);
      serveJsonResponse(routingContext, 200, Json.encodePrettily(commentList));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException | NotAllowedActionException e) {
      serveNotFound(routingContext);
    }
  }

  private void deleteComment(RoutingContext routingContext) {
    UUID proposalId;
    UUID commentId;
    try {
      proposalId = UUID.fromString(routingContext.pathParam("proposalId"));
      commentId = UUID.fromString(routingContext.pathParam("commentId"));
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }

    val authorToken = getAuthorizationToken(routingContext.request());
    val requestComment = new CommentRequestDTO()
        .setId(commentId)
        .setProposalId(proposalId)
        .setAuthorToken(authorToken);

    val interactor = getInteractorFactory().getDeleteComment();

    try {
      val comment = interactor.execute(requestComment);
      serveJsonResponse(routingContext, 200, Json.encodePrettily(comment));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException | NotAllowedActionException e) {
      serveNotFound(routingContext);
    }
  }

  private void voteComment(RoutingContext routingContext) {
    CommentVoteRequestDTO requestVote;
    try {
      if (routingContext.getBodyAsJson().isEmpty()) {
        throw new Exception();
      }
      requestVote = Json.decodeValue(routingContext.getBodyAsString(), CommentVoteRequestDTO.class);
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }

    val authorToken = getAuthorizationToken(routingContext.request());
    requestVote.setVoterToken(authorToken);

    val interactor = getInteractorFactory().getVoteComment();
    try {
      val comment = interactor.execute(requestVote);
      serveJsonResponse(routingContext, 200, Json.encodePrettily(comment));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException | NotAllowedActionException e) {
      serveNotFound(routingContext);
    }
  }
}
