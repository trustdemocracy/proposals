package eu.trustdemocracy.proposals.core.interactors.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.events.FakeEventsGateway;
import eu.trustdemocracy.proposals.gateways.out.FakeVotesGateway;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeProposalRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class PublishProposalTest {

  private Map<UUID, ProposalResponseDTO> reponseProposals;
  private ProposalRepository proposalRepository;
  private FakeEventsGateway eventsGateway;
  private FakeVotesGateway votesGateway;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    proposalRepository = new FakeProposalRepository();
    eventsGateway = new FakeEventsGateway();
    reponseProposals = new HashMap<>();
    TokenUtils.generateKeys();

    val lorem = LoremIpsum.getInstance();

    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    val interactor = new CreateProposal(proposalRepository);

    for (int i = 0; i < 10; i++) {
      val inputProposal = FakeModelsFactory
          .getRandomProposal(TokenUtils.createToken(authorId, authorUsername));

      val responseProposal = interactor.execute(inputProposal);
      reponseProposals.put(responseProposal.getId(), responseProposal);
    }
  }

  @Test
  public void publishProposalNonTokenUser() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken("");

    assertThrows(InvalidTokenException.class,
        () -> new PublishProposal(proposalRepository, eventsGateway, votesGateway)
            .execute(inputProposal));
  }

  @Test
  public void publishProposalNonAuthor() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), authorUsername));

    assertThrows(NotAllowedActionException.class,
        () -> new PublishProposal(proposalRepository, eventsGateway, votesGateway)
            .execute(inputProposal));
  }

  @Test
  public void publishNonExistingProposal() {
    val inputProposal = new ProposalRequestDTO()
        .setId(UUID.randomUUID())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));

    assertThrows(ResourceNotFoundException.class,
        () -> new PublishProposal(proposalRepository, eventsGateway, votesGateway)
            .execute(inputProposal));
  }

  @Test
  public void publishProposal() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));

    ProposalResponseDTO responseProposal =
        new PublishProposal(proposalRepository, eventsGateway, votesGateway).execute(inputProposal);

    assertEquals(authorUsername, responseProposal.getAuthorUsername());
    assertEquals(createdProposal.getTitle(), responseProposal.getTitle());
    assertEquals(createdProposal.getBrief(), responseProposal.getBrief());
    assertEquals(createdProposal.getSource(), responseProposal.getSource());
    assertEquals(createdProposal.getMotivation(), responseProposal.getMotivation());
    assertEquals(createdProposal.getMeasures(), responseProposal.getMeasures());
    assertNotEquals(createdProposal.getStatus(), responseProposal.getStatus());
    assertEquals(ProposalStatus.PUBLISHED, responseProposal.getStatus());

    assertTrue(votesGateway.registeredProposals.containsKey(responseProposal.getId()));
  }

}
