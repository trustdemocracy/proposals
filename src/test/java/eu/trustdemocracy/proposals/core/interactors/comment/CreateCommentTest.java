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
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.repositories.CommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeCommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeProposalRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class CreateCommentTest {

  private List<CommentRequestDTO> inputComments;
  private CommentRepository commentRepository;
  private ProposalRepository proposalRepository;

  private String proposalAuthorToken;
  private ProposalResponseDTO createdProposal;


  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentRepository = new FakeCommentRepository();
    proposalRepository = new FakeProposalRepository();
    inputComments = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();

    proposalAuthorToken = TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail());
    createdProposal = new CreateProposal(proposalRepository)
        .execute(FakeModelsFactory.getRandomProposal(proposalAuthorToken));

    for (int i = 0; i < 10; i++) {
      inputComments.add(FakeModelsFactory.getRandomComment(createdProposal.getId()));
    }
  }

  @Test
  public void createCommentNonTokenUser() {
    val inputComment = inputComments.get(0);
    inputComment.setAuthorToken("");

    assertThrows(InvalidTokenException.class,
        () -> new CreateComment(commentRepository, proposalRepository).execute(inputComment));
  }

  @Test
  public void createCommentInNonExistingProposal() {
    val inputComment = inputComments.get(0)
        .setProposalId(UUID.randomUUID());

    assertThrows(ResourceNotFoundException.class,
        () -> new CreateComment(commentRepository, proposalRepository).execute(inputComment));
  }

  @Test
  public void createCommentInNonPublishedProposal() {
    val inputComment = inputComments.get(0);

    assertThrows(ResourceNotFoundException.class,
        () -> new CreateComment(commentRepository, proposalRepository).execute(inputComment));
  }

  @Test
  public void createComment() {
    new PublishProposal(proposalRepository).execute(new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(proposalAuthorToken));

    val username = "username";
    val inputComment = inputComments.get(0)
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), username));
    val timestamp = System.currentTimeMillis();
    CommentResponseDTO responseComment = new CreateComment(commentRepository, proposalRepository)
        .execute(inputComment);

    assertEquals(username, responseComment.getAuthorUsername());
    assertEquals(inputComment.getProposalId(), responseComment.getProposalId());
    assertEquals(inputComment.getContent(), responseComment.getContent());
    assertTrue(timestamp <= responseComment.getTimestamp());
  }
}
