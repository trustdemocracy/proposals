package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
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
  public void createCommentNonTokenUser() {
    val inputComment = inputComments.get(0);
    inputComment.setAuthorToken("");

    assertThrows(InvalidTokenException.class,
        () -> new CreateComment(commentDAO).execute(inputComment));
  }

  @Test
  public void createComment() {
    val inputComment = inputComments.get(0);
    val timestamp = System.currentTimeMillis();
    CommentResponseDTO responseComment = new CreateComment(commentDAO).execute(inputComment);

    assertEquals(authorUsername, responseComment.getAuthorUsername());
    assertEquals(inputComment.getProposalId(), responseComment.getProposalId());
    assertEquals(inputComment.getContent(), responseComment.getContent());
    assertTrue(timestamp <= responseComment.getTimestamp());
  }
}
