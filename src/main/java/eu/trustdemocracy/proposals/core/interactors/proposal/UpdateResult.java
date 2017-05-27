package eu.trustdemocracy.proposals.core.interactors.proposal;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.UpdateResultDTO;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;

public class UpdateResult implements Interactor<UpdateResultDTO, Boolean> {

  public UpdateResult(ProposalRepository proposalRepository) {

  }

  @Override
  public Boolean execute(UpdateResultDTO updateResultDTO) {
    return null;
  }
}
