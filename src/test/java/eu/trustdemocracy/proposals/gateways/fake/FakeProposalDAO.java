package eu.trustdemocracy.proposals.gateways.fake;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakeProposalDAO implements ProposalDAO {

  private Map<UUID, Proposal> proposals = new HashMap<>();

  @Override
  public Proposal create(Proposal proposal) {
    UUID id;
    do {
      id = UUID.randomUUID();
    } while (findById(id) != null);

    proposal.setId(id);
    proposals.put(proposal.getId(), proposal);
    return proposal;
  }

  @Override
  public Proposal findById(UUID id) {
    return proposals.get(id);
  }

}
