package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.core.interactors.comment.CreateComment;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.endpoints.App;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.val;

public class CommentController extends Controller {

  public CommentController(App app) {
    super(app);
  }

  @Override
  public void buildRoutes() {
    getRouter().post("/proposals/:proposalId/comments").handler(this::createComment);
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
}
