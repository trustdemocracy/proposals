package eu.trustdemocracy.proposals.core.interactors.proposal;

import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import lombok.val;

public class PublishProposal implements Interactor<ProposalRequestDTO, ProposalResponseDTO> {

  private ProposalDAO proposalDAO;

  public PublishProposal(ProposalDAO proposalDAO) {
    this.proposalDAO = proposalDAO;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO inputProposal) {
    val proposal = proposalDAO.setStatus(inputProposal.getId(), ProposalStatus.PUBLISHED);
    return ProposalMapper.createResponse(proposal);
  }
}
