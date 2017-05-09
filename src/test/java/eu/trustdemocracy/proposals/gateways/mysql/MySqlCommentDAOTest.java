package eu.trustdemocracy.proposals.gateways.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.vorburger.exec.ManagedProcessException;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import java.sql.SQLException;
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

    sqlUtils.createCommentsTable();

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
    assertEquals(resultComment.getProposalId(), UUID.fromString(resultSet.getString("proposal_id")));
    assertEquals(resultComment.getRootCommentId(), UUID.fromString(resultSet.getString("root_comment_id")));
    assertEquals(comment.getContent(), resultSet.getString("content"));
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
