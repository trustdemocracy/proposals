package eu.trustdemocracy.proposals.gateways.repositories;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.VoteOption;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public interface ProposalRepository {

  Proposal create(Proposal proposal);

  Proposal findById(UUID id);

  Proposal delete(UUID id);

  Proposal setStatus(UUID id, ProposalStatus status);

  Proposal setStatus(UUID id, ProposalStatus status, long dueDate);

  List<Proposal> findByAuthorId(UUID authorId);

  List<Proposal> findByAuthorId(UUID authorId, ProposalStatus status);

  List<Proposal> findAllPublished();

  void updateResults(UUID id, Map<VoteOption, Double> results);
}
