package eu.trustdemocracy.proposals.core.entities;

import java.util.HashMap;
import java.util.Map;
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
  private long dueDate;
  private Map<VoteOption, Double> votes;
  private boolean expired;

  public Map<VoteOption, Double> getVotes() {
    if (votes == null) {
      votes = new HashMap<>();
      for (VoteOption option : VoteOption.values()) {
        votes.put(option, 0.0);
      }
    }
    return votes;
  }
}
