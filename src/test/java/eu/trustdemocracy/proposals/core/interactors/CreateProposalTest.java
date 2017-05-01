package eu.trustdemocracy.proposals.core.interactors;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import eu.trustdemocracy.proposals.gateways.fake.FakeProposalDAO;
import eu.trustdemocracy.proposals.infrastructure.JWTKeyFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.jose4j.jwk.RsaJwkGenerator;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateProposalTest {

  private List<ProposalRequestDTO> inputProposals;
  private ProposalDAO proposalDAO;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    val rsaKey = RsaJwkGenerator.generateJwk(2048);
    JWTKeyFactory.setPrivateKey(rsaKey.getPrivateKey());
    JWTKeyFactory.setPublicKey(rsaKey.getPublicKey());

    proposalDAO = new FakeProposalDAO();
    inputProposals = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();

    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    for (int i = 0; i < 10; i++) {
      inputProposals.add(new ProposalRequestDTO()
          .setAuthorToken(TokenUtils.createToken(authorId, authorUsername))
          .setTitle(lorem.getTitle(5, 30))
          .setBrief(lorem.getParagraphs(1, 1))
          .setSource(lorem.getUrl())
          .setMotivation(lorem.getParagraphs(1, 5))
          .setMeasures(lorem.getParagraphs(1, 5)));
    }
  }

  @Test
  public void createProposal() {
    val inputProposal = inputProposals.get(0);
    ProposalResponseDTO responseProposal = new CreateProposal(proposalDAO).execute(inputProposal);

    assertEquals(authorUsername, responseProposal.getAuthor());
    assertEquals(inputProposal.getTitle(), responseProposal.getTitle());
    assertEquals(inputProposal.getBrief(), responseProposal.getBrief());
    assertEquals(inputProposal.getSource(), responseProposal.getSource());
    assertEquals(inputProposal.getMotivation(), responseProposal.getMotivation());
    assertEquals(inputProposal.getMeasures(), responseProposal.getMeasures());
  }
}
