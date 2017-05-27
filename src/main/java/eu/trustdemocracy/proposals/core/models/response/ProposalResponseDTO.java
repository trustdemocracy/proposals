package eu.trustdemocracy.proposals.core.models.response;


import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.VoteOption;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProposalResponseDTO {

  private UUID id;
  private String authorUsername;
  private String title;
  private String brief;
  private String source;
  private String motivation;
  private String measures;
  private ProposalStatus status;
  private long dueDate;
  private Map<VoteOption,Double> votes;
}
