package eu.trustdemocracy.proposals.core.entities;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class Proposal {

  private UUID id;
  private User author;
  private String title;
  private String brief;
  private String source;
  private String motivation;
  private String measures;
  private ProposalStatus status;
}
