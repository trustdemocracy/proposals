package eu.trustdemocracy.proposals.gateways;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import java.util.UUID;

public interface ProposalDAO {

  Proposal create(Proposal proposal);

  Proposal findById(UUID id);

  Proposal delete(UUID id);

  Proposal setStatus(UUID id, ProposalStatus status);
}
