package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.events.FakeEventsGateway;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeCommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeProposalRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeleteCommentTest {

  private List<CommentResponseDTO> responseComments;
  private FakeCommentRepository commentDAO;
  private FakeProposalRepository proposalDAO;
  private FakeEventsGateway eventsGateway;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentRepository();
    proposalDAO = new FakeProposalRepository();
    eventsGateway = new FakeEventsGateway();
    responseComments = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();
    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    val proposalAuthorToken = TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail());
    val createdProposal = new CreateProposal(proposalDAO, eventsGateway)
        .execute(FakeModelsFactory.getRandomProposal(proposalAuthorToken));

    new PublishProposal(proposalDAO).execute(new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(proposalAuthorToken));

    val interactor = new CreateComment(commentDAO, proposalDAO, eventsGateway);
    for (int i = 0; i < 10; i++) {
      val inputComment = FakeModelsFactory
          .getRandomComment(authorId, authorUsername, createdProposal.getId());

      responseComments.add(interactor.execute(inputComment));
    }
  }

  @Test
  public void deleteCommentNonTokenUser() {
    val responseComment = responseComments.get(0);

    val inputComment = new CommentRequestDTO()
        .setId(responseComment.getId())
        .setAuthorToken("");

    assertThrows(InvalidTokenException.class,
        () -> new DeleteComment(commentDAO).execute(inputComment));
  }

  @Test
  public void deleteNonExistingComment() {
    val inputComment = new CommentRequestDTO()
        .setId(UUID.randomUUID())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));

    assertThrows(ResourceNotFoundException.class,
        () -> new DeleteComment(commentDAO).execute(inputComment));
  }

  @Test
  public void deleteCommentNotOwned() {
    val responseComment = responseComments.get(0);
    val inputComment = new CommentRequestDTO()
        .setId(responseComment.getId())
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), authorUsername));

    assertThrows(NotAllowedActionException.class,
        () -> new DeleteComment(commentDAO).execute(inputComment));
  }

  @Test
  public void deleteComment() {
    val responseComment = responseComments.get(0);

    val inputComment = new CommentRequestDTO()
        .setId(responseComment.getId())
        .setAuthorToken(TokenUtils.createToken(authorId, authorUsername));

    val deletedComment = new DeleteComment(commentDAO).execute(inputComment);

    assertEquals(responseComment, deletedComment);
    assertFalse(commentDAO.comments.containsKey(responseComment.getId()));
  }
}
