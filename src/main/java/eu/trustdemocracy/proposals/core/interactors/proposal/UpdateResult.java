package eu.trustdemocracy.proposals.core.interactors.proposal;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.UpdateResultDTO;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;

public class UpdateResult implements Interactor<UpdateResultDTO, Boolean> {

  private ProposalRepository proposalRepository;

  public UpdateResult(ProposalRepository proposalRepository) {
    this.proposalRepository = proposalRepository;
  }

  @Override
  public Boolean execute(UpdateResultDTO updateResultDTO) {
    proposalRepository.updateResults(updateResultDTO.getId(), updateResultDTO.getResults());
    return true;
  }
}
