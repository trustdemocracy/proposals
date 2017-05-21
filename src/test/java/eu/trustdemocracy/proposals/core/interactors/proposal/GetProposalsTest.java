package eu.trustdemocracy.proposals.core.interactors.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.GetProposalsRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.GetProposalsResponseDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import eu.trustdemocracy.proposals.gateways.fake.FakeProposalDAO;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetProposalsTest {

  private Map<UUID, ProposalResponseDTO> reponseProposals;
  private ProposalDAO proposalDAO;

  private UUID authorId;
  private String authorUsername;
  private UUID strangerId;
  private String strangerUsername;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    proposalDAO = new FakeProposalDAO();
    reponseProposals = new HashMap<>();

    val lorem = LoremIpsum.getInstance();

    strangerId = UUID.randomUUID();
    strangerUsername = lorem.getEmail();
    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    val create = new CreateProposal(proposalDAO);
    val publish = new PublishProposal(proposalDAO);

    for (int i = 0; i < 10; i++) {
      val inputProposal = FakeModelsFactory
          .getRandomProposal(TokenUtils.createToken(authorId, authorUsername));

      val responseProposal = create.execute(inputProposal);
      if (i % 2 == 0) {
        publish.execute(inputProposal.setId(responseProposal.getId()));
      }
      reponseProposals.put(responseProposal.getId(), responseProposal);
    }

    for (int i = 0; i < 30; i++) {
      val inputProposal = FakeModelsFactory
          .getRandomProposal(TokenUtils.createToken(strangerId, strangerUsername));

      val responseProposal = create.execute(inputProposal);
      if (i % 2 == 0) {
        publish.execute(inputProposal.setId(responseProposal.getId()));
      }
      reponseProposals.put(responseProposal.getId(), responseProposal);
    }
  }

  @Test
  public void getOwnProposals() {
    val inputRequest = new GetProposalsRequestDTO()
        .setAccessToken(TokenUtils.createToken(authorId, authorUsername))
        .setAuthorId(authorId);

    GetProposalsResponseDTO responseDTO = new GetProposals(proposalDAO).execute(inputRequest);

    assertEquals(10, responseDTO.getProposals().size());
  }

  @Test
  public void getPublishedProposalsByAuthor() {
    val inputRequest = new GetProposalsRequestDTO()
        .setAccessToken(TokenUtils.createToken(authorId, authorUsername))
        .setAuthorId(strangerId);

    GetProposalsResponseDTO responseDTO = new GetProposals(proposalDAO).execute(inputRequest);

    assertEquals(15, responseDTO.getProposals().size());
    for (val proposal : responseDTO.getProposals()) {
      assertEquals(strangerUsername, proposal.getAuthorUsername());
    }
  }

  @Test
  public void getAllPublishedProposals() {
    val inputRequest = new GetProposalsRequestDTO()
        .setAccessToken(TokenUtils.createToken(authorId, authorUsername));

    GetProposalsResponseDTO responseDTO = new GetProposals(proposalDAO).execute(inputRequest);

    assertEquals(10, responseDTO.getProposals().size());

    int countOfPublishedByStranger = 5;
    for (val proposal : responseDTO.getProposals()) {
      if (proposal.getAuthorUsername().equals(strangerUsername)) {
        countOfPublishedByStranger--;
      }
    }

    assertEquals(0, countOfPublishedByStranger);
  }

}
