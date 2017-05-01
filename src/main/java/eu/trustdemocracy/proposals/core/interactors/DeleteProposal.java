package eu.trustdemocracy.proposals.core.interactors;

import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;

public class DeleteProposal {

  public DeleteProposal(ProposalDAO proposalDAO) {
  }

  public ProposalResponseDTO execute(ProposalRequestDTO inputProposal) {
    return null;
  }
}
