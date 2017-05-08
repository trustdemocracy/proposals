package eu.trustdemocracy.proposals.gateways.fake;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.val;

public class FakeCommentDAO implements CommentDAO {

  public Map<UUID, Comment> comments = new HashMap<>();
  public Map<String, CommentVoteOption> votes = new HashMap<>();

  @Override
  public Comment create(Comment comment) {
    UUID id;
    do {
      id = UUID.randomUUID();
    } while (comments.containsKey(id));

    comment.setId(id);
    comment.setTimestamp(System.currentTimeMillis());

    comments.put(id, comment);
    return comment;
  }

  @Override
  public Comment deleteById(UUID id) {
    val comment = comments.get(id);
    if (comment != null) {
      comments.remove(id);
    }
    return comment;
  }

  @Override
  public Comment vote(UUID commentId, UUID voterId, CommentVoteOption option) {
    val storedComment = comments.get(commentId);
    val compositeKey = commentId + "|" + voterId;

    val previouslyVoted = votes.put(compositeKey, option);

    for (val key : storedComment.getVotes().keySet()) {
      Integer currentVotes = storedComment.getVotes().get(key);
      if (previouslyVoted == key) {
        currentVotes--;
      } else if (option == key) {
        currentVotes++;
      }
      storedComment.getVotes().replace(key, currentVotes);
    }

    return storedComment;
  }

}
