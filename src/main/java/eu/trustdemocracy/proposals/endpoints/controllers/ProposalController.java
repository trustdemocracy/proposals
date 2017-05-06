package eu.trustdemocracy.proposals.endpoints.controllers;

import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.endpoints.App;
import io.vertx.core.json.Json;
import io.vertx.ext.web.RoutingContext;
import lombok.val;

public class ProposalController extends Controller {

  public ProposalController(App app) {
    super(app);
  }

  @Override
  public void buildRoutes() {
    getRouter().post("/proposal").handler(this::createProposal);
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
}
