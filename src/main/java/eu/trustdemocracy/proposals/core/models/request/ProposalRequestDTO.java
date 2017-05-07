package eu.trustdemocracy.proposals.core.models.request;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProposalRequestDTO {

  private UUID id;
  private String authorToken;
  private String title;
  private String brief;
  private String source;
  private String motivation;
  private String measures;
}
