package eu.trustdemocracy.proposals.core.entities;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Comment {

  private UUID id;
  private UUID rootCommentId;
  private UUID proposalId;
  private User author;
  private String content;
  private long timestamp;
  private int upVotesCount;
  private int downVotesCount;
}
