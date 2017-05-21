package eu.trustdemocracy.proposals.gateways;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import java.util.List;
import java.util.UUID;

public interface CommentRepository {

  Comment create(Comment comment);

  Comment deleteById(UUID id);

  Comment vote(UUID commentId, UUID voterId, CommentVoteOption option);

  List<Comment> findByProposalId(UUID proposalId);

  Comment findById(UUID id);
}
