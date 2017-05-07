package eu.trustdemocracy.proposals.core.interactors.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.UnpublishProposal;
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

public class UnpublishProposalTest {

  private Map<UUID, ProposalResponseDTO> reponseProposals;
  private ProposalDAO proposalDAO;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    proposalDAO = new FakeProposalDAO();
    reponseProposals = new HashMap<>();
    TokenUtils.generateKeys();

    val lorem = LoremIpsum.getInstance();

    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    val createProposal = new CreateProposal(proposalDAO);
    val publishProposal = new PublishProposal(proposalDAO);

    for (int i = 0; i < 10; i++) {
      val inputProposal = new ProposalRequestDTO()
          .setAuthorToken(TokenUtils.createToken(authorId, authorUsername))
          .setTitle(lorem.getTitle(5, 30))
          .setBrief(lorem.getParagraphs(1, 1))
          .setSource(lorem.getUrl())
          .setMotivation(lorem.getParagraphs(1, 5))
          .setMeasures(lorem.getParagraphs(1, 5));

      val createdProposal = createProposal.execute(inputProposal);
      inputProposal.setId(createdProposal.getId());

      val responseProposal = publishProposal.execute(inputProposal);

      reponseProposals.put(responseProposal.getId(), responseProposal);
    }
  }

  @Test
  public void unpublishProposal() {
    val publishedProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(publishedProposal.getId())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));

    ProposalResponseDTO responseProposal = new UnpublishProposal(proposalDAO).execute(inputProposal);

    assertEquals(authorUsername, responseProposal.getAuthorUsername());
    assertEquals(publishedProposal.getTitle(), responseProposal.getTitle());
    assertEquals(publishedProposal.getBrief(), responseProposal.getBrief());
    assertEquals(publishedProposal.getSource(), responseProposal.getSource());
    assertEquals(publishedProposal.getMotivation(), responseProposal.getMotivation());
    assertEquals(publishedProposal.getMeasures(), responseProposal.getMeasures());
    assertNotEquals(publishedProposal.getStatus(), responseProposal.getStatus());
    assertEquals(ProposalStatus.UNPUBLISHED, responseProposal.getStatus());
  }



}