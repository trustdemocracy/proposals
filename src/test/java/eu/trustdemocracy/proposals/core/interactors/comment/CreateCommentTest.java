package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import eu.trustdemocracy.proposals.gateways.fake.FakeCommentDAO;
import eu.trustdemocracy.proposals.gateways.fake.FakeProposalDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateCommentTest {

  private List<CommentRequestDTO> inputComments;
  private CommentDAO commentDAO;
  private ProposalDAO proposalDAO;

  private String authorUsername;
  private String proposalAuthorToken;
  private ProposalResponseDTO createdProposal;


  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentDAO();
    proposalDAO = new FakeProposalDAO();
    inputComments = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();

    authorUsername = lorem.getEmail();

    proposalAuthorToken = TokenUtils.createToken(UUID.randomUUID(), authorUsername);
    createdProposal = new CreateProposal(proposalDAO)
        .execute(new ProposalRequestDTO()
            .setAuthorToken(proposalAuthorToken)
            .setTitle(lorem.getTitle(5, 30))
            .setBrief(lorem.getParagraphs(1, 1))
            .setSource(lorem.getUrl())
            .setMotivation(lorem.getParagraphs(1, 5))
            .setMeasures(lorem.getParagraphs(1, 5)));

    for (int i = 0; i < 10; i++) {
      inputComments.add(new CommentRequestDTO()
          .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), authorUsername))
          .setProposalId(createdProposal.getId())
          .setContent(lorem.getParagraphs(1, 2)));
    }
  }

  @Test
  public void createCommentNonTokenUser() {
    val inputComment = inputComments.get(0);
    inputComment.setAuthorToken("");

    assertThrows(InvalidTokenException.class,
        () -> new CreateComment(commentDAO, proposalDAO).execute(inputComment));
  }

  @Test
  public void createCommentInNonExistingProposal() {
    val inputComment = inputComments.get(0)
        .setProposalId(UUID.randomUUID());

    assertThrows(ResourceNotFoundException.class,
        () -> new CreateComment(commentDAO, proposalDAO).execute(inputComment));
  }

  @Test
  public void createCommentInNonPublishedProposal() {
    val inputComment = inputComments.get(0);

    assertThrows(ResourceNotFoundException.class,
        () -> new CreateComment(commentDAO, proposalDAO).execute(inputComment));
  }

  @Test
  public void createComment() {
    new PublishProposal(proposalDAO).execute(new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(proposalAuthorToken));

    val inputComment = inputComments.get(0);
    val timestamp = System.currentTimeMillis();
    CommentResponseDTO responseComment = new CreateComment(commentDAO, proposalDAO)
        .execute(inputComment);

    assertEquals(authorUsername, responseComment.getAuthorUsername());
    assertEquals(inputComment.getProposalId(), responseComment.getProposalId());
    assertEquals(inputComment.getContent(), responseComment.getContent());
    assertTrue(timestamp <= responseComment.getTimestamp());
  }
}
