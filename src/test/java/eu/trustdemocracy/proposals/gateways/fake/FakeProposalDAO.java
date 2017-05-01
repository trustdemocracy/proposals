package eu.trustdemocracy.proposals.gateways.fake;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import java.util.HashMap;
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
  public Proposal publish(UUID id) {
    val proposal = findById(id);
    return proposal.setStatus(ProposalStatus.PUBLISHED);
  }

}
