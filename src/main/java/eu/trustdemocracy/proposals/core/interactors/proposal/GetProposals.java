package eu.trustdemocracy.proposals.core.interactors.proposal;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.GetProposalsRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.GetProposalsResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;

public class GetProposals implements Interactor<GetProposalsRequestDTO, GetProposalsResponseDTO> {

  public GetProposals(ProposalDAO proposalDAO) {
  }

  @Override
  public GetProposalsResponseDTO execute(GetProposalsRequestDTO getProposalsRequestDTO) {
    return null;
  }
}
