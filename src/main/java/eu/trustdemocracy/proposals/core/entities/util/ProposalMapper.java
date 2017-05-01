package eu.trustdemocracy.proposals.core.entities.util;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;

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
        .setMeasures(proposal.getMeasures());
  }
}
