package eu.trustdemocracy.proposals.core.interactors.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.events.FakeEventsGateway;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeProposalRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateProposalTest {

  private List<ProposalRequestDTO> inputProposals;
  private ProposalRepository proposalRepository;
  private FakeEventsGateway eventsGateway;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    proposalRepository = new FakeProposalRepository();
    eventsGateway = new FakeEventsGateway();
    inputProposals = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();

    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    for (int i = 0; i < 10; i++) {
      inputProposals.add(
          FakeModelsFactory.getRandomProposal(TokenUtils.createToken(authorId, authorUsername)));
    }
  }

  @Test
  public void createProposalNonTokenUser() {
    val inputProposal = inputProposals.get(0);
    inputProposal.setAuthorToken("");
    assertThrows(InvalidTokenException.class,
        () -> new CreateProposal(proposalRepository, eventsGateway).execute(inputProposal));
  }

  @Test
  public void createProposal() {
    val inputProposal = inputProposals.get(0);
    ProposalResponseDTO responseProposal = new CreateProposal(proposalRepository, eventsGateway).execute(inputProposal);

    assertEquals(authorUsername, responseProposal.getAuthorUsername());
    assertEquals(inputProposal.getTitle(), responseProposal.getTitle());
    assertEquals(inputProposal.getBrief(), responseProposal.getBrief());
    assertEquals(inputProposal.getSource(), responseProposal.getSource());
    assertEquals(inputProposal.getMotivation(), responseProposal.getMotivation());
    assertEquals(inputProposal.getMeasures(), responseProposal.getMeasures());
    assertEquals(ProposalStatus.UNPUBLISHED, responseProposal.getStatus());
  }
}
