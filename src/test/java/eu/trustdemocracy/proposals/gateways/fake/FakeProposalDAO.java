package eu.trustdemocracy.proposals.gateways.fake;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.val;

public class FakeProposalDAO implements ProposalDAO {

  private Map<UUID, Proposal> proposals = new HashMap<>();

  @Override
  public Proposal create(Proposal proposal) {
    UUID id;
    do {
      id = UUID.randomUUID();
    } while (findById(id) != null);

    proposal.setId(id);
    proposal.setStatus(ProposalStatus.UNPUBLISHED);
    proposals.put(proposal.getId(), proposal);
    return proposal;
  }

  @Override
  public Proposal findById(UUID id) {
    return proposals.get(id);
  }

  @Override
  public Proposal delete(UUID id) {
    return proposals.remove(id);
  }

  @Override
  public Proposal setStatus(UUID id, ProposalStatus status) {
    val proposal = findById(id);
    return proposal.setStatus(status);
  }

  @Override
  public List<Proposal> findByAuthorId(UUID authorId) {
    List<Proposal> result = new ArrayList<>();

    for (val proposal : proposals.values()) {
      if (proposal.getAuthor().getId().equals(authorId)) {
        result.add(proposal);
      }
    }

    return result;
  }

  @Override
  public List<Proposal> findByAuthorId(UUID authorId, ProposalStatus status) {
    List<Proposal> result = new ArrayList<>();

    for (val proposal : proposals.values()) {
      if (proposal.getAuthor().getId().equals(authorId) && proposal.getStatus().equals(status)) {
        result.add(proposal);
      }
    }

    return result;
  }

  @Override
  public List<Proposal> findAllPublished() {
    List<Proposal> result = new ArrayList<>();

    for (val proposal : proposals.values()) {
      if (proposal.getStatus().equals(ProposalStatus.PUBLISHED)) {
        result.add(proposal);
      }
    }

    return result;
  }

}
