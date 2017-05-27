package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.core.entities.VoteOption;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.models.request.GetProposalsRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.UpdateResultDTO;
import eu.trustdemocracy.proposals.endpoints.App;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import java.util.HashMap;
import java.util.UUID;
import lombok.val;

public class ProposalController extends Controller {

  public ProposalController(App app) {
    super(app);
  }

  @Override
  public void buildRoutes() {
    getRouter().get("/proposals").handler(this::getProposals);
    getRouter().post("/proposals").handler(this::createProposal);
    getRouter().get("/proposals/:id").handler(this::getProposal);
    getRouter().delete("/proposals/:id").handler(this::deleteProposal);
    getRouter().get("/proposals/:id/publish").handler(this::publishProposal);
    getRouter().get("/proposals/:id/unpublish").handler(this::unpublishProposal);
    getRouter().post("/proposals/results").handler(this::updateResults);
  }

  private void getProposals(RoutingContext routingContext) {
    UUID authorId = null;
    String authorIdParam = routingContext.request().getParam("authorId");
    if (authorIdParam != null && !authorIdParam.isEmpty()) {
      authorId = UUID.fromString(authorIdParam);
    }

    val authorToken = getAuthorizationToken(routingContext.request());
    val requestProposal = new GetProposalsRequestDTO()
        .setAccessToken(authorToken)
        .setAuthorId(authorId);

    val interactor = getInteractorFactory().getGetProposals();

    try {
      val proposals = interactor.execute(requestProposal);
      serveJsonResponse(routingContext, 200, Json.encodePrettily(proposals));
    } catch (InvalidTokenException e) {
      serveBadCredentials(routingContext);
    } catch (ResourceNotFoundException | NotAllowedActionException e) {
      serveNotFound(routingContext);
    }
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
    } catch (ResourceNotFoundException | NotAllowedActionException e) {
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
    } catch (ResourceNotFoundException | NotAllowedActionException e) {
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
    } catch (ResourceNotFoundException | NotAllowedActionException e) {
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
    } catch (ResourceNotFoundException | NotAllowedActionException e) {
      serveNotFound(routingContext);
    }
  }

  private void updateResults(RoutingContext context) {
    val interactor = getInteractorFactory().getUpdateResult();

    try {
      val proposals = context.getBodyAsJson();
      for (val proposal : proposals) {
        val dto = new UpdateResultDTO()
            .setId(UUID.fromString(proposal.getKey()));

        val json = JsonObject.mapFrom(proposal.getValue());

        if (json.containsKey("expired")) {
          dto.setExpired(json.getBoolean("expired"));
        } else if (json.containsKey("results")) {
          val resultsJson = json.getJsonObject("results");
          val favour = resultsJson.getDouble("FAVOUR");
          val against = resultsJson.getDouble("AGAINST");

          val results = new HashMap<VoteOption, Double>();
          results.put(VoteOption.FAVOUR, favour == null ? 0.0 : favour);
          results.put(VoteOption.AGAINST, against == null ? 0.0 : against);

          dto.setResults(results);
        }

        interactor.execute(dto);
      }
      serveJsonResponse(context, 200, new JsonObject().put("status", "ok").encodePrettily());
    } catch (Exception e) {
      serveBadRequest(context);
    }
  }
}
