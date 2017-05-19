package eu.trustdemocracy.proposals.core.entities;

import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;
import lombok.val;

@Data
@Accessors(chain = true)
public class User {

  private UUID id;
  private String username;

  public boolean hasAccess(Proposal proposal) {
    val authorId = proposal.getAuthor().getId();

    return proposal.getStatus().equals(ProposalStatus.PUBLISHED)
        || authorId.equals(id);
  }
}
