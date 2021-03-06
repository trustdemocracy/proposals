package eu.trustdemocracy.proposals.core.interactors.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
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
import eu.trustdemocracy.proposals.gateways.out.FakeEventsGateway;
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

public class UnpublishProposalTest {

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
    votesGateway = new FakeVotesGateway();
    reponseProposals = new HashMap<>();
    TokenUtils.generateKeys();

    val lorem = LoremIpsum.getInstance();

    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    val createProposal = new CreateProposal(proposalRepository);
    val publishProposal = new PublishProposal(proposalRepository, eventsGateway, votesGateway);

    for (int i = 0; i < 10; i++) {
      val inputProposal = FakeModelsFactory
          .getRandomProposal(TokenUtils.createToken(authorId, authorUsername));

      val createdProposal = createProposal.execute(inputProposal);
      inputProposal.setId(createdProposal.getId());

      val responseProposal = publishProposal.execute(inputProposal);

      reponseProposals.put(responseProposal.getId(), responseProposal);
    }
  }

  @Test
  public void unpublishProposalNonTokenUser() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken("");

    assertThrows(InvalidTokenException.class,
        () -> new UnpublishProposal(proposalRepository, votesGateway).execute(inputProposal));
  }

  @Test
  public void unpublishProposalNonAuthor() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), authorUsername));

    assertThrows(NotAllowedActionException.class,
        () -> new UnpublishProposal(proposalRepository, votesGateway).execute(inputProposal));
  }

  @Test
  public void unpublishNonExistingProposal() {
    val inputProposal = new ProposalRequestDTO()
        .setId(UUID.randomUUID())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));

    assertThrows(ResourceNotFoundException.class,
        () -> new UnpublishProposal(proposalRepository, votesGateway).execute(inputProposal));
  }

  @Test
  public void unpublishProposal() {
    val publishedProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(publishedProposal.getId())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));


    assertTrue(votesGateway.registeredProposals.containsKey(inputProposal.getId()));

    ProposalResponseDTO responseProposal = new UnpublishProposal(proposalRepository, votesGateway)
        .execute(inputProposal);

    assertEquals(authorUsername, responseProposal.getAuthorUsername());
    assertEquals(publishedProposal.getTitle(), responseProposal.getTitle());
    assertEquals(publishedProposal.getBrief(), responseProposal.getBrief());
    assertEquals(publishedProposal.getSource(), responseProposal.getSource());
    assertEquals(publishedProposal.getMotivation(), responseProposal.getMotivation());
    assertEquals(publishedProposal.getMeasures(), responseProposal.getMeasures());
    assertNotEquals(publishedProposal.getStatus(), responseProposal.getStatus());
    assertEquals(ProposalStatus.UNPUBLISHED, responseProposal.getStatus());

    assertFalse(votesGateway.registeredProposals.containsKey(inputProposal.getId()));
  }


}
