package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.CommentVoteRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.out.FakeEventsGateway;
import eu.trustdemocracy.proposals.gateways.out.FakeVotesGateway;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeCommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeProposalRepository;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class VoteCommentTest {

  private CommentResponseDTO responseComment;
  private FakeCommentRepository commentDAO;
  private FakeProposalRepository proposalDAO;
  private FakeEventsGateway eventsGateway;
  private LoremIpsum lorem;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentRepository();
    proposalDAO = new FakeProposalRepository();
    eventsGateway = new FakeEventsGateway();

    lorem = LoremIpsum.getInstance();

    val proposalAuthorToken = TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail());
    val createdProposal = new CreateProposal(proposalDAO)
        .execute(FakeModelsFactory.getRandomProposal(proposalAuthorToken));

    new PublishProposal(proposalDAO, eventsGateway, new FakeVotesGateway())
        .execute(new ProposalRequestDTO()
            .setId(createdProposal.getId())
            .setAuthorToken(proposalAuthorToken));

    val inputComment = FakeModelsFactory.getRandomComment(createdProposal.getId());

    responseComment = new CreateComment(commentDAO, proposalDAO, eventsGateway)
        .execute(inputComment);
  }

  @Test
  public void deleteCommentNonTokenUser() {
    val inputVote = new CommentVoteRequestDTO()
        .setCommentId(responseComment.getId())
        .setVoterToken("")
        .setOption(CommentVoteOption.UP);

    assertThrows(InvalidTokenException.class,
        () -> new VoteComment(commentDAO).execute(inputVote));
  }

  @Test
  public void voteNonExistingComment() {
    val inputComment = new CommentVoteRequestDTO()
        .setCommentId(UUID.randomUUID())
        .setVoterToken(TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail()))
        .setOption(CommentVoteOption.UP);

    assertThrows(ResourceNotFoundException.class,
        () -> new VoteComment(commentDAO).execute(inputComment));
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
  public void voteUpCommentTwice() {
    val id = UUID.randomUUID();
    val inputVote = new CommentVoteRequestDTO()
        .setCommentId(responseComment.getId())
        .setVoterToken(TokenUtils.createToken(id, lorem.getEmail()))
        .setOption(CommentVoteOption.UP);

    val votedComment = new VoteComment(commentDAO).execute(inputVote);

    assertEquals(new Integer(1), votedComment.getVotes().get(CommentVoteOption.UP));
    assertNotNull(votedComment.getVotes().get(CommentVoteOption.DOWN));
    assertEquals(new Integer(0), votedComment.getVotes().get(CommentVoteOption.DOWN));

    val sameComment = new VoteComment(commentDAO).execute(inputVote);

    assertEquals(new Integer(1), sameComment.getVotes().get(CommentVoteOption.UP));
    assertNotNull(sameComment.getVotes().get(CommentVoteOption.DOWN));
    assertEquals(new Integer(0), sameComment.getVotes().get(CommentVoteOption.DOWN));
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

  @Test
  public void revokeVoteInComment() {
    val id = UUID.randomUUID();
    val inputVote = new CommentVoteRequestDTO()
        .setCommentId(responseComment.getId())
        .setVoterToken(TokenUtils.createToken(id, lorem.getEmail()));

    val interactor = new VoteComment(commentDAO);

    inputVote.setOption(CommentVoteOption.UP);
    val upvotedComment = interactor.execute(inputVote);
    assertEquals(new Integer(1), upvotedComment.getVotes().get(CommentVoteOption.UP));
    assertEquals(new Integer(0), upvotedComment.getVotes().get(CommentVoteOption.DOWN));

    inputVote.setOption(null);
    val revokeUpvoteComment = interactor.execute(inputVote);
    assertEquals(new Integer(0), revokeUpvoteComment.getVotes().get(CommentVoteOption.UP));
    assertEquals(new Integer(0), revokeUpvoteComment.getVotes().get(CommentVoteOption.DOWN));

    inputVote.setOption(CommentVoteOption.DOWN);
    val downvotedComment = interactor.execute(inputVote);
    assertEquals(new Integer(1), downvotedComment.getVotes().get(CommentVoteOption.DOWN));
    assertEquals(new Integer(0), downvotedComment.getVotes().get(CommentVoteOption.UP));

    inputVote.setOption(null);
    val revokeDownvoteComment = interactor.execute(inputVote);
    assertEquals(new Integer(0), revokeDownvoteComment.getVotes().get(CommentVoteOption.UP));
    assertEquals(new Integer(0), revokeDownvoteComment.getVotes().get(CommentVoteOption.DOWN));
  }
}
