package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.CommentVoteRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.fake.FakeCommentDAO;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VoteCommentTest {

  private CommentResponseDTO responseComment;
  private FakeCommentDAO commentDAO;
  private LoremIpsum lorem;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentDAO();

    lorem = LoremIpsum.getInstance();

    val inputComment = new CommentRequestDTO()
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail()))
        .setProposalId(UUID.randomUUID())
        .setContent(lorem.getParagraphs(1, 2));

    responseComment = new CreateComment(commentDAO).execute(inputComment);
  }

  @Test
  public void voteUpComment() {
    val id = UUID.randomUUID();
    val inputVote = new CommentVoteRequestDTO()
        .setCommentId(responseComment.getId())
        .setVoterToken(TokenUtils.createToken(id, lorem.getEmail()))
        .setOption(CommentVoteOption.UP);

    val votedComment = new VoteComment(commentDAO).execute(inputVote);

    assertEquals(new Integer(1), votedComment.getVotes().get(CommentVoteOption.UP));
    assertNotNull(votedComment.getVotes().get(CommentVoteOption.DOWN));
    assertEquals(new Integer(0), votedComment.getVotes().get(CommentVoteOption.DOWN));
  }

  @Test
  public void voteDownComment() {
    val id = UUID.randomUUID();
    val inputVote = new CommentVoteRequestDTO()
        .setCommentId(responseComment.getId())
        .setVoterToken(TokenUtils.createToken(id, lorem.getEmail()))
        .setOption(CommentVoteOption.DOWN);

    val votedComment = new VoteComment(commentDAO).execute(inputVote);

    assertEquals(new Integer(1), votedComment.getVotes().get(CommentVoteOption.DOWN));
    assertNotNull(votedComment.getVotes().get(CommentVoteOption.UP));
    assertEquals(new Integer(0), votedComment.getVotes().get(CommentVoteOption.UP));
  }
}
