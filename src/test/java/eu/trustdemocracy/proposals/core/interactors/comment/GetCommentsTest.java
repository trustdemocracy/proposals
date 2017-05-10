package eu.trustdemocracy.proposals.core.interactors.comment;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.fake.FakeCommentDAO;
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

  @BeforeEach
  public void init() throws JoseException {
    TokenUtils.generateKeys();

    commentDAO = new FakeCommentDAO();
    responseComments = new ArrayList<>();

    val lorem = LoremIpsum.getInstance();

    val proposalId = UUID.randomUUID();
    val interactor = new CreateComment(commentDAO);
    for (int i = 0; i < 10; i++) {
      val inputComment = new CommentRequestDTO()
          .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail()))
          .setProposalId(proposalId)
          .setContent(lorem.getParagraphs(1, 2));

      responseComments.add(interactor.execute(inputComment));
    }
  }

  @Test
  public void getComments() {
    val responseComment = responseComments.get(0);

    val inputProposal = new ProposalRequestDTO()
        .setId(responseComment.getProposalId());

    val commentList = new GetComments(commentDAO).execute(inputProposal);

    assertEquals(responseComments.size(), commentList.size());
    for (int i = 0; i < commentList.size(); i++) {
      assertEquals(responseComments.get(i), commentList.get(i));
    }
  }

}
