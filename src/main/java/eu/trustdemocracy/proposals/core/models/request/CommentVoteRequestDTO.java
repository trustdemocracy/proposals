package eu.trustdemocracy.proposals.core.models.request;

import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CommentVoteRequestDTO {

  private UUID commentId;
  private String voterToken;
  private CommentVoteOption option;
}
