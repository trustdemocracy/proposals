package eu.trustdemocracy.proposals.core.entities;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Data;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.val;

@Data
@Accessors(chain = true)
public class Comment {

  private UUID id;
  private UUID rootCommentId;
  private Proposal proposal = new Proposal();
  private User author;
  private String content;
  private long timestamp;
  @Setter(AccessLevel.PRIVATE)
  private Map<CommentVoteOption, Integer> votes = initVotes();


  private Map<CommentVoteOption, Integer> initVotes() {
    Map<CommentVoteOption, Integer> votes = new HashMap<>();
    for (val option : CommentVoteOption.values()) {
      votes.put(option, 0);
    }
    return votes;
  }
}
