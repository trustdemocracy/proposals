package eu.trustdemocracy.proposals.core.interactors.proposal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.events.FakeEventsGateway;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeProposalRepository;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeleteProposalTest {

  private Map<UUID, ProposalResponseDTO> reponseProposals;
  private ProposalRepository proposalRepository;
  private FakeEventsGateway eventsGateway;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    proposalRepository = new FakeProposalRepository();
    eventsGateway = new FakeEventsGateway();
    reponseProposals = new HashMap<>();

    val lorem = LoremIpsum.getInstance();

    authorUsername = lorem.getEmail();
    authorId = UUID.randomUUID();

    val interactor = new CreateProposal(proposalRepository, eventsGateway);

    for (int i = 0; i < 10; i++) {
      val inputProposal = FakeModelsFactory
          .getRandomProposal(TokenUtils.createToken(authorId, authorUsername));

      val responseProposal = interactor.execute(inputProposal);
      reponseProposals.put(responseProposal.getId(), responseProposal);
    }
  }

  @Test
  public void deleteProposalNonTokenUser() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken("");

    assertThrows(InvalidTokenException.class,
        () -> new DeleteProposal(proposalRepository).execute(inputProposal));
  }

  @Test
  public void deleteProposalNonAuthor() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), authorUsername));

    assertThrows(NotAllowedActionException.class,
        () -> new DeleteProposal(proposalRepository).execute(inputProposal));
  }

  @Test
  public void deleteNonExistingProposal() {
    val inputProposal = new ProposalRequestDTO()
        .setId(UUID.randomUUID())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));

    assertThrows(ResourceNotFoundException.class,
        () -> new DeleteProposal(proposalRepository).execute(inputProposal));
  }

  @Test
  public void deleteProposal() {
    val createdProposal = reponseProposals.values().iterator().next();

    val inputProposal = new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));

    ProposalResponseDTO responseProposal = new DeleteProposal(proposalRepository).execute(inputProposal);

    assertEquals(authorUsername, responseProposal.getAuthorUsername());
    assertEquals(createdProposal.getTitle(), responseProposal.getTitle());
    assertEquals(createdProposal.getBrief(), responseProposal.getBrief());
    assertEquals(createdProposal.getSource(), responseProposal.getSource());
    assertEquals(createdProposal.getMotivation(), responseProposal.getMotivation());
    assertEquals(createdProposal.getMeasures(), responseProposal.getMeasures());

    assertThrows(ResourceNotFoundException.class,
        () -> new GetProposal(proposalRepository).execute(inputProposal));
  }

}
