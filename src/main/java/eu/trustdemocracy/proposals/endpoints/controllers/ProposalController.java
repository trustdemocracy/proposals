package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.GetProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.UnpublishProposal;
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
    getRouter().get("/proposals/:id/publish").handler(this::publishProposal);
    getRouter().get("/proposals/:id/unpublish").handler(this::unpublishProposal);
  }

  private void createProposal(RoutingContext routingContext) {
    val requestProposal = Json.decodeValue(routingContext.getBodyAsString(), ProposalRequestDTO.class);
    val interactor = getInteractorFactory().createProposalInteractor(CreateProposal.class);
    val proposal = interactor.execute(requestProposal);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(201)
        .end(Json.encodePrettily(proposal));
  }

  private void getProposal(RoutingContext routingContext) {
    val id = UUID.fromString(routingContext.pathParam("id"));
    val requestProposal = new ProposalRequestDTO().setId(id);
    val interactor = getInteractorFactory().createProposalInteractor(GetProposal.class);
    val proposal = interactor.execute(requestProposal);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(Json.encodePrettily(proposal));
  }

  private void publishProposal(RoutingContext routingContext) {
    val id = UUID.fromString(routingContext.pathParam("id"));
    val requestProposal = new ProposalRequestDTO().setId(id);
    val interactor = getInteractorFactory().createProposalInteractor(PublishProposal.class);
    val proposal = interactor.execute(requestProposal);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(Json.encodePrettily(proposal));
  }

  private void unpublishProposal(RoutingContext routingContext) {
    val id = UUID.fromString(routingContext.pathParam("id"));
    val requestProposal = new ProposalRequestDTO().setId(id);
    val interactor = getInteractorFactory().createProposalInteractor(UnpublishProposal.class);
    val proposal = interactor.execute(requestProposal);

    routingContext.response()
        .putHeader("content-type", "application/json")
        .setStatusCode(200)
        .end(Json.encodePrettily(proposal));
  }


}
