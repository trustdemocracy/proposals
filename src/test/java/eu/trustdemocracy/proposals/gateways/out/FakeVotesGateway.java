package eu.trustdemocracy.proposals.gateways.out;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FakeVotesGateway implements VotesGateway {

  public Map<UUID, Proposal> registeredProposals = new HashMap<>();

  @Override
  public void registerProposal(Proposal proposal) {
    this.registeredProposals.put(proposal.getId(), proposal);
  }

  @Override
  public void unregisterProposal(Proposal proposal) {
    this.registeredProposals.remove(proposal.getId());
  }
}
