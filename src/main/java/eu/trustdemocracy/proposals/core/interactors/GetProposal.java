package eu.trustdemocracy.proposals.core.interactors;

import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import lombok.val;

public class GetProposal {

  private ProposalDAO proposalDAO;

  public GetProposal(ProposalDAO proposalDAO) {
    this.proposalDAO = proposalDAO;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO inputProposal) {
    val proposal = proposalDAO.findById(inputProposal.getId());
    return proposal == null ? null : ProposalMapper.createResponse(proposal);
  }
}
