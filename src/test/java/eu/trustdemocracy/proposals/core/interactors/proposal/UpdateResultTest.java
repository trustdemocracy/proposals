package eu.trustdemocracy.proposals.core.interactors.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.VoteOption;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.UpdateResultDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeProposalRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class UpdateResultTest {

  private Map<UUID, ProposalResponseDTO> responseProposals;
  private ProposalRepository proposalRepository;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    proposalRepository = new FakeProposalRepository();
    TokenUtils.generateKeys();

    val lorem = LoremIpsum.getInstance();

    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();
    responseProposals = new HashMap<>();

    val interactor = new CreateProposal(proposalRepository);

    for (int i = 0; i < 10; i++) {
      val inputProposal = FakeModelsFactory
          .getRandomProposal(TokenUtils.createToken(authorId, authorUsername));

      val responseProposal = interactor.execute(inputProposal);
      responseProposals.put(responseProposal.getId(), responseProposal);
    }
  }

  @Test
  public void updateResult() {
    val votes = new HashMap<VoteOption, Double>();
    votes.put(VoteOption.FAVOUR, 0.3);
    votes.put(VoteOption.AGAINST, 0.2);

    val proposal = responseProposals.values().iterator().next();

    UpdateResultDTO request = new UpdateResultDTO()
        .setId(proposal.getId())
        .setResults(votes);

    Boolean success = new UpdateResult(proposalRepository).execute(request);

    assertTrue(success);

    val foundProposal = proposalRepository.findById(proposal.getId());
    assertEquals(votes.get(VoteOption.FAVOUR), foundProposal.getVotes().get(VoteOption.FAVOUR));
    assertEquals(votes.get(VoteOption.AGAINST), foundProposal.getVotes().get(VoteOption.AGAINST));
  }

  @Test
  public void closeProposal() {
    val votes = new HashMap<VoteOption, Double>();
    votes.put(VoteOption.FAVOUR, 0.3);
    votes.put(VoteOption.AGAINST, 0.2);

    val proposal = responseProposals.values().iterator().next();

    UpdateResultDTO request = new UpdateResultDTO()
        .setId(proposal.getId())
        .setResults(votes);

    new UpdateResult(proposalRepository).execute(request);


    UpdateResultDTO expiredRequest = new UpdateResultDTO()
        .setId(proposal.getId())
        .setExpired(true);
    new UpdateResult(proposalRepository).execute(expiredRequest);


    val foundProposal = proposalRepository.findById(proposal.getId());
    assertEquals(votes.get(VoteOption.FAVOUR), foundProposal.getVotes().get(VoteOption.FAVOUR));
    assertEquals(votes.get(VoteOption.AGAINST), foundProposal.getVotes().get(VoteOption.AGAINST));
    assertTrue(foundProposal.isExpired());
  }


}
