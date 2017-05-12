package eu.trustdemocracy.proposals.core.models.response;

import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import java.util.HashMap;
import java.util.Map;
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
  private Map<CommentVoteOption, Integer> votes = new HashMap<>();
}
