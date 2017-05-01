package eu.trustdemocracy.proposals.core.interactors;

import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import lombok.val;

public class CreateProposal {
  private ProposalDAO proposalDAO;


  public CreateProposal(ProposalDAO proposalDAO) {
    this.proposalDAO = proposalDAO;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO proposalRequestDTO) {
    val proposal = ProposalMapper.createEntity(proposalRequestDTO);
    return ProposalMapper.createResponse(proposalDAO.create(proposal));
  }
}
