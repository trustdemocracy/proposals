package eu.trustdemocracy.proposals.core.models.response;

import java.util.List;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class GetProposalsResponseDTO {

  private List<ProposalResponseDTO> proposals;
}
