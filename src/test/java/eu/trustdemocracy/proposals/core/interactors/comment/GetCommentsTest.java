package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.exceptions.InvalidTokenException;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.fake.FakeCommentDAO;
import eu.trustdemocracy.proposals.gateways.fake.FakeProposalDAO;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class GetCommentsTest {

  private List<CommentResponseDTO> responseComments;
  private FakeCommentDAO commentDAO;
  private FakeProposalDAO proposalDAO;

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentDAO();
    proposalDAO = new FakeProposalDAO();
    responseComments = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();

    val createdProposal = new CreateProposal(proposalDAO)
        .execute(new ProposalRequestDTO()
            .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail()))
            .setTitle(lorem.getTitle(5, 30))
            .setBrief(lorem.getParagraphs(1, 1))
            .setSource(lorem.getUrl())
            .setMotivation(lorem.getParagraphs(1, 5))
            .setMeasures(lorem.getParagraphs(1, 5)));
    val interactor = new CreateComment(commentDAO, proposalDAO);
    for (int i = 0; i < 10; i++) {
      val inputComment = new CommentRequestDTO()
          .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail()))
          .setProposalId(createdProposal.getId())
          .setContent(lorem.getParagraphs(1, 2));

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
        () -> new GetComments(commentDAO).execute(inputProposal));
  }

  @Test
  public void getComments() {
    val responseComment = responseComments.get(0);

    val inputProposal = new ProposalRequestDTO()
        .setId(responseComment.getProposalId())
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(),
            responseComment.getAuthorUsername()));

    val commentList = new GetComments(commentDAO).execute(inputProposal);

    assertEquals(responseComments.size(), commentList.size());
    for (int i = 0; i < commentList.size(); i++) {
      assertEquals(responseComments.get(i), commentList.get(i));
    }
  }

}
