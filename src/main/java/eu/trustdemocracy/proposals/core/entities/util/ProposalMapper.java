package eu.trustdemocracy.proposals.core.entities.util;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.GetProposalsResponseDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import java.util.List;
import lombok.val;

public class ProposalMapper {

  public static Proposal createEntity(ProposalRequestDTO proposalRequestDTO) {
    return new Proposal()
        .setId(proposalRequestDTO.getId())
        .setAuthor(UserMapper.createEntity(proposalRequestDTO.getAuthorToken()))
        .setTitle(proposalRequestDTO.getTitle())
        .setBrief(proposalRequestDTO.getBrief())
        .setSource(proposalRequestDTO.getSource())
        .setMotivation(proposalRequestDTO.getMotivation())
        .setMeasures(proposalRequestDTO.getMeasures());
  }

  public static ProposalResponseDTO createResponse(Proposal proposal) {
    return new ProposalResponseDTO()
        .setId(proposal.getId())
        .setAuthorUsername(proposal.getAuthor().getUsername())
        .setTitle(proposal.getTitle())
        .setBrief(proposal.getBrief())
        .setSource(proposal.getSource())
        .setMotivation(proposal.getMotivation())
        .setMeasures(proposal.getMeasures())
        .setStatus(proposal.getStatus())
        .setDueDate(proposal.getDueDate())
        .setVotes(proposal.getVotes());
  }

  public static GetProposalsResponseDTO createResponse(List<Proposal> proposalList) {
    val response = new GetProposalsResponseDTO();

    for (val proposal : proposalList) {
      response.getProposals().add(createResponse(proposal));
    }

    return response;
  }
}
