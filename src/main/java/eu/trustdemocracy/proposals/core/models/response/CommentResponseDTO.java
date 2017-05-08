package eu.trustdemocracy.proposals.core.models.response;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CommentResponseDTO {

  private UUID id;
  private UUID rootCommentId;
  private UUID proposalId;
  private String authorUsername;
  private String content;
  private long timestamp;
  private int upVotesCount;
  private int downVotesCount;
}
