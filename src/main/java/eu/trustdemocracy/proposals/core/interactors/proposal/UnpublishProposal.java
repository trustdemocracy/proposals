package eu.trustdemocracy.proposals.core.interactors.proposal;

import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.out.VotesGateway;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import lombok.val;

public class UnpublishProposal implements Interactor<ProposalRequestDTO, ProposalResponseDTO> {

  private ProposalRepository proposalRepository;
  private VotesGateway votesGateway;

  public UnpublishProposal(ProposalRepository proposalRepository, VotesGateway votesGateway) {
    this.proposalRepository = proposalRepository;
    this.votesGateway = votesGateway;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO inputProposal) {
    val user = UserMapper.createEntity(inputProposal.getAuthorToken());

    val foundProposal = proposalRepository.findById(inputProposal.getId());

    if (foundProposal == null) {
      throw new ResourceNotFoundException(
          "Trying to unpublish non-existing proposal [" + inputProposal.getId() + "]");
    }

    if (!foundProposal.getAuthor().getId().equals(user.getId())) {
      throw new NotAllowedActionException(
          "Failed to unpublish proposal [" + foundProposal.getId()
              + "]. User [" + user.getId() + "] is not the owner");
    }

    val proposal = proposalRepository.setStatus(inputProposal.getId(), ProposalStatus.UNPUBLISHED);

    votesGateway.unregisterProposal(proposal);

    return ProposalMapper.createResponse(proposal);
  }
}
