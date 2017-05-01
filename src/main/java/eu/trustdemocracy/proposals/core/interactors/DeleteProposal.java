package eu.trustdemocracy.proposals.core.interactors;

import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import lombok.val;

public class DeleteProposal {

  private ProposalDAO proposalDAO;

  public DeleteProposal(ProposalDAO proposalDAO) {
    this.proposalDAO = proposalDAO;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO inputProposal) {
    val proposal = proposalDAO.delete(inputProposal.getId());
    return ProposalMapper.createResponse(proposal);
  }
}
