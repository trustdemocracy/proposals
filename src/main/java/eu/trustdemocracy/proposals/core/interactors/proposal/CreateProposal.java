package eu.trustdemocracy.proposals.core.interactors.proposal;

import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import lombok.val;

public class CreateProposal implements Interactor<ProposalRequestDTO, ProposalResponseDTO> {

  private ProposalRepository proposalRepository;

  public CreateProposal(ProposalRepository proposalRepository) {
    this.proposalRepository = proposalRepository;
  }

  public ProposalResponseDTO execute(ProposalRequestDTO proposalRequestDTO) {
    val proposal = ProposalMapper.createEntity(proposalRequestDTO);
    proposal.setStatus(ProposalStatus.UNPUBLISHED);
    return ProposalMapper.createResponse(proposalRepository.create(proposal));
  }
}
