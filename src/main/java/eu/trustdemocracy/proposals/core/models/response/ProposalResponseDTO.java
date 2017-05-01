package eu.trustdemocracy.proposals.core.models.response;


import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class ProposalResponseDTO {

  private UUID id;
  private String author;
  private String title;
  private String brief;
  private String source;
  private String motivation;
  private String measures;
}
