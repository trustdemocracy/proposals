package eu.trustdemocracy.proposals.core.interactors.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.GetProposal;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
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

public class GetProposalTest {

  private Map<UUID, ProposalResponseDTO> reponseProposals;
  private ProposalDAO proposalDAO;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    proposalDAO = new FakeProposalDAO();
    reponseProposals = new HashMap<>();

    val lorem = LoremIpsum.getInstance();

    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    val interactor = new CreateProposal(proposalDAO);

    for (int i = 0; i < 10; i++) {
      val inputProposal = new ProposalRequestDTO()
          .setAuthorToken(TokenUtils.createToken(authorId, authorUsername))
          .setTitle(lorem.getTitle(5, 30))
          .setBrief(lorem.getParagraphs(1, 1))
          .setSource(lorem.getUrl())
          .setMotivation(lorem.getParagraphs(1, 5))
          .setMeasures(lorem.getParagraphs(1, 5));

      val responseProposal = interactor.execute(inputProposal);
      reponseProposals.put(responseProposal.getId(), responseProposal);
    }
  }

  @Test
  public void getProposal() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO().setId(createdProposal.getId());

    ProposalResponseDTO responseProposal = new GetProposal(proposalDAO).execute(inputProposal);

    assertEquals(authorUsername, responseProposal.getAuthorUsername());
    assertEquals(createdProposal.getTitle(), responseProposal.getTitle());
    assertEquals(createdProposal.getBrief(), responseProposal.getBrief());
    assertEquals(createdProposal.getSource(), responseProposal.getSource());
    assertEquals(createdProposal.getMotivation(), responseProposal.getMotivation());
    assertEquals(createdProposal.getMeasures(), responseProposal.getMeasures());
    assertEquals(createdProposal.getStatus(), responseProposal.getStatus());
  }

}
