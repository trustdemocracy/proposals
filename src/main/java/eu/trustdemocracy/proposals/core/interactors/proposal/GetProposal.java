package eu.trustdemocracy.proposals.core.interactors.proposal;

import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import lombok.val;

public class GetProposal implements Interactor<ProposalRequestDTO, ProposalResponseDTO> {

  private ProposalDAO proposalDAO;

  public GetProposal(ProposalDAO proposalDAO) {
    this.proposalDAO = proposalDAO;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO inputProposal) {
    val user = UserMapper.createEntity(inputProposal.getAuthorToken());
    val proposal = proposalDAO.findById(inputProposal.getId());

    if (proposal == null) {
      throw new ResourceNotFoundException(
          "Trying to retrieve non-existing proposal [" + inputProposal.getId() + "]");
    }

    if (!user.hasAccess(proposal)) {
      throw new NotAllowedActionException(
          "Failed to retrieve unpublished proposal [" + proposal.getId()
              + "]. User [" + user.getId() + "] is not the owner");
    }

    return ProposalMapper.createResponse(proposal);
  }

}
