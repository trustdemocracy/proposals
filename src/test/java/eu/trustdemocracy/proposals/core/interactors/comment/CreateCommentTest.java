package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import eu.trustdemocracy.proposals.gateways.fake.FakeCommentDAO;
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

  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentDAO();
    inputComments = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();

    authorUsername = lorem.getEmail();

    for (int i = 0; i < 10; i++) {
      inputComments.add(new CommentRequestDTO()
          .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), authorUsername))
          .setProposalId(UUID.randomUUID())
          .setContent(lorem.getParagraphs(1, 2)));
    }
  }

  @Test
  public void createComment() {
    val inputComment = inputComments.get(0);
    val timestamp = System.currentTimeMillis();
    CommentResponseDTO responseProposal = new CreateComment(commentDAO).execute(inputComment);

    assertEquals(authorUsername, responseProposal.getAuthorUsername());
    assertEquals(inputComment.getProposalId(), responseProposal.getProposalId());
    assertEquals(inputComment.getContent(), responseProposal.getContent());
    assertTrue(timestamp >= responseProposal.getTimestamp());
  }
}
