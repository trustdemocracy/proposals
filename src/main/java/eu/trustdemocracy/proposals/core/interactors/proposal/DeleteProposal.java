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

public class DeleteProposal implements Interactor<ProposalRequestDTO, ProposalResponseDTO> {

  private ProposalDAO proposalDAO;

  public DeleteProposal(ProposalDAO proposalDAO) {
    this.proposalDAO = proposalDAO;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO inputProposal) {
    val user = UserMapper.createEntity(inputProposal.getAuthorToken());

    val foundProposal = proposalDAO.findById(inputProposal.getId());

    if (foundProposal == null) {
      throw new ResourceNotFoundException(
          "Trying to delete non-existing proposal [" + inputProposal.getId() + "]");
    }

    if (!foundProposal.getAuthor().getId().equals(user.getId())) {
      throw new NotAllowedActionException(
          "User [" + user.getId() + "] is not the owner of proposal ["
              + foundProposal.getId() + "]");
    }

    val proposal = proposalDAO.delete(inputProposal.getId());
    return ProposalMapper.createResponse(proposal);
  }
}
