package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.UnpublishProposal;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeCommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.fake.FakeProposalRepository;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetCommentsTest {

  private List<CommentResponseDTO> responseComments;
  private FakeCommentRepository commentDAO;
  private FakeProposalRepository proposalDAO;

  private String proposalAuthorToken;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentRepository();
    proposalDAO = new FakeProposalRepository();
    responseComments = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();

    proposalAuthorToken = TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail());
    val createdProposal = new CreateProposal(proposalDAO)
        .execute(FakeModelsFactory.getRandomProposal(proposalAuthorToken));

    new PublishProposal(proposalDAO).execute(new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(proposalAuthorToken));
    val interactor = new CreateComment(commentDAO, proposalDAO);
    for (int i = 0; i < 10; i++) {
      val inputComment = FakeModelsFactory.getRandomComment(createdProposal.getId());

      responseComments.add(interactor.execute(inputComment));
    }
  }

  @Test
  public void getCommentsNonTokenUser() {
    val responseComment = responseComments.get(0);

    val inputProposal = new ProposalRequestDTO()
        .setId(responseComment.getProposalId())
        .setAuthorToken("");

    assertThrows(InvalidTokenException.class,
        () -> new GetComments(commentDAO, proposalDAO).execute(inputProposal));
  }

  @Test
  public void getComments() {
    val responseComment = responseComments.get(0);

    val inputProposal = new ProposalRequestDTO()
        .setId(responseComment.getProposalId())
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(),
            responseComment.getAuthorUsername()));

    val commentList = new GetComments(commentDAO, proposalDAO).execute(inputProposal);

    assertEquals(responseComments.size(), commentList.size());
    for (int i = 0; i < commentList.size(); i++) {
      assertEquals(responseComments.get(i), commentList.get(i));
    }
  }

  @Test
  public void getCommentsFromUnpublished() {
    val responseComment = responseComments.get(0);

    val inputProposal = new ProposalRequestDTO()
        .setId(responseComment.getProposalId())
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(),
            responseComment.getAuthorUsername()));

    new UnpublishProposal(proposalDAO).execute(new ProposalRequestDTO()
        .setId(responseComment.getProposalId())
        .setAuthorToken(proposalAuthorToken));

    assertThrows(ResourceNotFoundException.class,
        () -> new GetComments(commentDAO, proposalDAO).execute(inputProposal));
  }
}
