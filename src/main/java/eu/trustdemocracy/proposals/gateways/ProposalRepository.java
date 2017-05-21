package eu.trustdemocracy.proposals.gateways;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import java.util.List;
import java.util.UUID;

public interface ProposalRepository {

  Proposal create(Proposal proposal);

  Proposal findById(UUID id);

  Proposal delete(UUID id);

  Proposal setStatus(UUID id, ProposalStatus status);

  List<Proposal> findByAuthorId(UUID authorId);

  List<Proposal> findByAuthorId(UUID authorId, ProposalStatus status);

  List<Proposal> findAllPublished();

}
