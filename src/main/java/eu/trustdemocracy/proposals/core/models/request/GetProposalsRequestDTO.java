package eu.trustdemocracy.proposals.core.models.request;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetProposalsRequestDTO {

  private String accessToken;
  private UUID authorId;
}
