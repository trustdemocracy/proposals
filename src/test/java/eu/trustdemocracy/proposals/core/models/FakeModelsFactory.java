package eu.trustdemocracy.proposals.core.models;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.gateways.mysql.MySqlProposalDAO;
import java.util.UUID;
import lombok.val;

public class FakeModelsFactory {

  private static Lorem lorem = LoremIpsum.getInstance();

  public static ProposalRequestDTO getRandomProposal() {
    val currentUsername = truncate(lorem.getEmail(), MySqlProposalDAO.AUTHOR_SIZE);
    val authorToken = TokenUtils.createToken(UUID.randomUUID(), currentUsername);

    return getRandomProposal(authorToken);
  }

  public static ProposalRequestDTO getRandomProposal(String authorToken) {
    val title = truncate(lorem.getTitle(5, 30), MySqlProposalDAO.TITLE_SIZE);
    val brief = truncate(lorem.getParagraphs(1, 1), MySqlProposalDAO.BRIEF_SIZE);
    val source = truncate(lorem.getUrl(), MySqlProposalDAO.SOURCE_SIZE);
    val motivation = truncate(lorem.getParagraphs(1, 5), MySqlProposalDAO.MOTIVATION_SIZE);
    val measures = truncate(lorem.getParagraphs(1, 5), MySqlProposalDAO.MEASURES_SIZE);

    return new ProposalRequestDTO()
        .setAuthorToken(authorToken)
        .setTitle(title)
        .setBrief(brief)
        .setSource(source)
        .setMotivation(motivation)
        .setMeasures(measures);
  }

  public static CommentRequestDTO getRandomComment() {
    return getRandomComment(UUID.randomUUID());
  }

  public static CommentRequestDTO getRandomComment(UUID proposalId) {
    return getRandomComment(UUID.randomUUID(), lorem.getEmail(), proposalId);
  }

  public static CommentRequestDTO getRandomComment(UUID userId, String username, UUID proposalId) {
    return getRandomComment(userId, username, proposalId, new UUID(0L, 0L));
  }

  public static CommentRequestDTO getRandomComment(UUID userId, String username, UUID proposalId,
      UUID rootId) {
    return getRandomComment(TokenUtils.createToken(userId, username), proposalId, rootId);
  }

  public static CommentRequestDTO getRandomComment(String authorToken, UUID proposalId, UUID rootId) {
    return new CommentRequestDTO()
        .setAuthorToken(authorToken)
        .setProposalId(proposalId)
        .setRootCommentId(rootId)
        .setContent(lorem.getParagraphs(1, 2));
  }


  private static String truncate(String string, int limit) {
    return string.length() > limit ? string.substring(0, limit) : string;
  }
}
