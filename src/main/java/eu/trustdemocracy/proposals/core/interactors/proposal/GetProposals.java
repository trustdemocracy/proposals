package eu.trustdemocracy.proposals.core.interactors.proposal;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.util.ProposalMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.GetProposalsRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.GetProposalsResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalRepository;
import java.util.List;
import lombok.val;

public class GetProposals implements Interactor<GetProposalsRequestDTO, GetProposalsResponseDTO> {

  private ProposalRepository proposalRepository;

  public GetProposals(ProposalRepository proposalRepository) {
    this.proposalRepository = proposalRepository;
  }

  @Override
  public GetProposalsResponseDTO execute(GetProposalsRequestDTO requestDTO) {
    val user = UserMapper.createEntity(requestDTO.getAccessToken());

    List<Proposal> proposalList;

    if (user.getId().equals(requestDTO.getAuthorId())) {
      proposalList = proposalRepository.findByAuthorId(user.getId());
    } else if (requestDTO.getAuthorId() != null) {
      proposalList = proposalRepository.findByAuthorId(requestDTO.getAuthorId(), ProposalStatus.PUBLISHED);
    } else {
      proposalList = proposalRepository.findAllPublished();
    }

    return ProposalMapper.createResponse(proposalList);
  }
}
