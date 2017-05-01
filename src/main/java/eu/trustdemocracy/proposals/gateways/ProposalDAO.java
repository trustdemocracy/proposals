package eu.trustdemocracy.proposals.gateways;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import java.util.UUID;

public interface ProposalDAO {

  Proposal create(Proposal proposal);

  Proposal findById(UUID id);

  Proposal delete(UUID id);
}
