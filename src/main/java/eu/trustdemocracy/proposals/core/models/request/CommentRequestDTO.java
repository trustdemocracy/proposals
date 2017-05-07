package eu.trustdemocracy.proposals.core.models.request;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CommentRequestDTO {

  private UUID id;
  private UUID proposalId;
  private UUID rootCommentId;
  private String authorToken;
  private String content;
}
