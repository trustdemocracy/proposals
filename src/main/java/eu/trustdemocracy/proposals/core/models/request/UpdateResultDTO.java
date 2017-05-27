package eu.trustdemocracy.proposals.core.models.request;

import eu.trustdemocracy.proposals.core.entities.VoteOption;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class UpdateResultDTO {

  private UUID id;
  private Map<VoteOption, Double> results = new HashMap<>();
  private boolean expired;
}
