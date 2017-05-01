package eu.trustdemocracy.proposals.core.interactors;

import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import lombok.val;

public class UnpublishProposal {

  private ProposalDAO proposalDAO;

  public UnpublishProposal(ProposalDAO proposalDAO) {
    this.proposalDAO = proposalDAO;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO inputProposal) {
    val proposal = proposalDAO.setStatus(inputProposal.getId(), ProposalStatus.UNPUBLISHED);
    return ProposalMapper.createResponse(proposal);
  }
}
