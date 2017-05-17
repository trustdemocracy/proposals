package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.Assert.assertFalse;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.fake.FakeCommentDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DeleteCommentTest {

  private List<CommentResponseDTO> responseComments;
  private FakeCommentDAO commentDAO;

  private UUID authorId;
  private String authorUsername;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentDAO();
    responseComments = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();
    authorId = UUID.randomUUID();
    authorUsername = lorem.getEmail();

    val interactor = new CreateComment(commentDAO);
    for (int i = 0; i < 10; i++) {
      val inputComment = new CommentRequestDTO()
          .setAuthorToken(TokenUtils.createToken(authorId, authorUsername))
          .setProposalId(UUID.randomUUID())
          .setContent(lorem.getParagraphs(1, 2));

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
