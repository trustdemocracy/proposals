package eu.trustdemocracy.proposals.gateways.out;

import eu.trustdemocracy.proposals.core.entities.Proposal;

public interface VotesGateway {

  void registerProposal(Proposal proposal);

  void unregisterProposal(Proposal proposal);

}
