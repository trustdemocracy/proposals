package eu.trustdemocracy.proposals.gateways.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.vorburger.exec.ManagedProcessException;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MySqlCommentDAOTest {

  private SqlUtils sqlUtils;

  private Lorem lorem = LoremIpsum.getInstance();

  private CommentDAO commentDAO;

  @BeforeEach
  public void init() throws Exception {
    TokenUtils.generateKeys();

    sqlUtils = new SqlUtils();
    sqlUtils.startDB();

    sqlUtils.createCommentsAndVotesTables();

    commentDAO = new MySqlCommentDAO(sqlUtils.getConnection());
  }

  @AfterEach
  public void tearDown() throws SQLException, ManagedProcessException {
    sqlUtils.stopDB();
  }

  @Test
  public void createComment() throws SQLException {
    val comment = createRandomComment();

    val resultComment = commentDAO.create(comment);

    val connection = sqlUtils.getConnection();
    val sql = "SELECT * FROM `comments` WHERE id = ?";
    val statement = connection.prepareStatement(sql);
    statement.setString(1, resultComment.getId().toString());
    val resultSet = statement.executeQuery();

    assertTrue(resultSet.next());
    assertEquals(resultComment.getId(), UUID.fromString(resultSet.getString("id")));
    assertEquals(resultComment.getProposalId(),
        UUID.fromString(resultSet.getString("proposal_id")));
    assertEquals(resultComment.getRootCommentId(),
        UUID.fromString(resultSet.getString("root_comment_id")));
    assertEquals(comment.getAuthor().getId(), UUID.fromString(resultSet.getString("author_id")));
    assertEquals(comment.getAuthor().getUsername(), resultSet.getString("author_username"));
    assertEquals(comment.getContent(), resultSet.getString("content"));
    assertEquals(resultComment.getTimestamp(), resultSet.getTimestamp("created_at").getTime());

    for (val option : resultComment.getVotes().keySet()) {
      assertEquals(new Integer(0), resultComment.getVotes().get(option));
    }
  }

  @Test
  public void deleteComment() throws SQLException {
    val comment = createRandomComment();

    val resultComment = commentDAO.create(comment);

    val connection = sqlUtils.getConnection();
    val sql = "SELECT * FROM `comments` WHERE id = ?";
    val statement = connection.prepareStatement(sql);
    statement.setString(1, resultComment.getId().toString());

    assertTrue(statement.executeQuery().next());

    val deletedComment = commentDAO.deleteById(comment.getId());

    assertNotNull(resultComment);
    assertNotNull(deletedComment);
    assertEquals(resultComment.getId(), deletedComment.getId());
    assertEquals(resultComment.getProposalId(), deletedComment.getProposalId());
    assertEquals(resultComment.getRootCommentId(), deletedComment.getRootCommentId());
    assertEquals(comment.getAuthor().getId(), deletedComment.getAuthor().getId());
    assertEquals(comment.getAuthor().getUsername(), deletedComment.getAuthor().getUsername());
    assertEquals(comment.getContent(), deletedComment.getContent());

    assertFalse(statement.executeQuery().next());
  }

  @Test
  public void voteComment() {
    for (val optionToTest : CommentVoteOption.values()) {
      val comment = createRandomComment();

      val resultComment = commentDAO.create(comment);

      for (val option : resultComment.getVotes().keySet()) {
        assertEquals(new Integer(0), resultComment.getVotes().get(option));
      }

      val votedComment = commentDAO
          .vote(comment.getId(), comment.getAuthor().getId(), optionToTest);

      assertNotNull(votedComment);
      for (val option : votedComment.getVotes().keySet()) {
        if (option == optionToTest) {
          assertEquals(new Integer(1), votedComment.getVotes().get(option));
        } else {
          assertEquals(new Integer(0), votedComment.getVotes().get(option));
        }
      }
    }
  }


  @Test
  public void getComments() throws SQLException, InterruptedException {
    val createdComments = new ArrayList<Comment>();
    val proposalId = UUID.randomUUID();

    for (int i = 0; i < 10; i++) {
      val comment = createRandomComment();
      comment.setProposalId(proposalId);
      createdComments.add(commentDAO.create(comment));
      Thread.sleep(1000);
    }

    List<Comment> retrievedComments = commentDAO.findByProposalId(proposalId);

    assertEquals(createdComments.size(), retrievedComments.size());

    for (int i = 0; i < retrievedComments.size(); i++) {
      assertEquals(proposalId, retrievedComments.get(i).getProposalId());

      assertEquals(createdComments.get(i), retrievedComments.get(i));

      for (val option : retrievedComments.get(i).getVotes().keySet()) {
        assertEquals(createdComments.get(i).getVotes().get(option),
            retrievedComments.get(i).getVotes().get(option));
      }
    }
  }


  private Comment createRandomComment() {
    val username = MySqlProposalDAO.truncate(lorem.getEmail(), MySqlProposalDAO.AUTHOR_SIZE);
    val user = UserMapper.createEntity(TokenUtils.createToken(UUID.randomUUID(), username));

    return new Comment()
        .setAuthor(user)
        .setProposalId(UUID.randomUUID())
        .setRootCommentId(new UUID(0L, 0L))
        .setContent(lorem.getParagraphs(1, 2));
  }
}
