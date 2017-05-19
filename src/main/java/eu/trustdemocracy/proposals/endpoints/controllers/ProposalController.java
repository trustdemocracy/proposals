package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.endpoints.App;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import java.util.UUID;
import lombok.val;

public class ProposalController extends Controller {

  public ProposalController(App app) {
    super(app);
  }

  @Override
  public void buildRoutes() {
    getRouter().post("/proposals").handler(this::createProposal);
    getRouter().get("/proposals/:id").handler(this::getProposal);
    getRouter().delete("/proposals/:id").handler(this::deleteProposal);
    getRouter().get("/proposals/:id/publish").handler(this::publishProposal);
    getRouter().get("/proposals/:id/unpublish").handler(this::unpublishProposal);
  }

  private void createProposal(RoutingContext routingContext) {
    ProposalRequestDTO requestProposal;
    try {
      if (routingContext.getBodyAsJson().isEmpty()) {
        throw new Exception();
      }
      requestProposal = Json
          .decodeValue(routingContext.getBodyAsString(), ProposalRequestDTO.class);
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }
    val authorToken = getAuthorizationToken(routingContext.request());
    requestProposal.setAuthorToken(authorToken);

    val interactor = getInteractorFactory().getCreateProposal();

    try {
      val proposal = interactor.execute(requestProposal);
      serveJsonResponse(routingContext, 201, Json.encodePrettily(proposal));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    }
  }

  private void getProposal(RoutingContext routingContext) {
    UUID id;
    try {
      id = UUID.fromString(routingContext.pathParam("id"));
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }

    val authorToken = getAuthorizationToken(routingContext.request());
    val requestProposal = new ProposalRequestDTO()
        .setId(id)
        .setAuthorToken(authorToken);

    val interactor = getInteractorFactory().getGetProposal();

    try {
      val proposal = interactor.execute(requestProposal);
      serveJsonResponse(routingContext, 200, Json.encodePrettily(proposal));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException e) {
      serveNotFound(routingContext);
    }
  }

  private void publishProposal(RoutingContext routingContext) {
    UUID id;
    try {
      id = UUID.fromString(routingContext.pathParam("id"));
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }
    val authorToken = getAuthorizationToken(routingContext.request());
    val requestProposal = new ProposalRequestDTO()
        .setId(id)
        .setAuthorToken(authorToken);
    val interactor = getInteractorFactory().getPublishProposal();

    try {
      val proposal = interactor.execute(requestProposal);
      serveJsonResponse(routingContext, 200, Json.encodePrettily(proposal));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException e) {
      serveNotFound(routingContext);
    }
  }

  private void unpublishProposal(RoutingContext routingContext) {
    UUID id;
    try {
      id = UUID.fromString(routingContext.pathParam("id"));
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }

    val authorToken = getAuthorizationToken(routingContext.request());
    val requestProposal = new ProposalRequestDTO()
        .setId(id)
        .setAuthorToken(authorToken);

    val interactor = getInteractorFactory().getUnpublishProposal();

    try {
      val proposal = interactor.execute(requestProposal);
      serveJsonResponse(routingContext, 200, Json.encodePrettily(proposal));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException e) {
      serveNotFound(routingContext);
    }
  }

  private void deleteProposal(RoutingContext routingContext) {
    UUID id;
    try {
      id = UUID.fromString(routingContext.pathParam("id"));
    } catch (Exception e) {
      serveBadRequest(routingContext);
      return;
    }

    val authorToken = getAuthorizationToken(routingContext.request());
    val requestProposal = new ProposalRequestDTO()
        .setId(id)
        .setAuthorToken(authorToken);

    val interactor = getInteractorFactory().getDeleteProposal();

    try {
      val proposal = interactor.execute(requestProposal);
      serveJsonResponse(routingContext, 200, Json.encodePrettily(proposal));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException e) {
      serveNotFound(routingContext);
    }
  }


}
