package eu.trustdemocracy.proposals.gateways.mysql;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import lombok.val;

public class MySqlCommentDAO implements CommentDAO {

  private static final int DUPLICATE_PK_ERROR_CODE = 1062;
  private static final String TABLE = "comments";
  public static final int ID_SIZE = 36;
  public static final int AUTHOR_SIZE = 100;
  public static final int CONTENT_SIZE = 5000;

  private static final Logger LOG = LoggerFactory.getLogger(MySqlProposalDAO.class);

  private Connection conn;

  public MySqlCommentDAO(Connection conn) {
    this.conn = conn;
  }

  @Override
  public Comment create(Comment comment) {
    try {
      val id = UUID.randomUUID();
      comment.setId(id);

      comment.setContent(truncate(comment.getContent(), CONTENT_SIZE));
      val username = truncate(comment.getAuthor().getUsername(), AUTHOR_SIZE);

      val sql = "INSERT INTO `" + TABLE + "` "
          + "(id, proposal_id, root_comment_id, author_id, author_username, content) "
          + "VALUES(?, ?, ?, ?, ?, ?)";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, id.toString());
      statement.setString(2, comment.getProposalId().toString());
      statement.setString(3, comment.getRootCommentId().toString());
      statement.setString(4, comment.getAuthor().getId().toString());
      statement.setString(5, username);
      statement.setString(6, comment.getContent());

      if (statement.executeUpdate() > 0) {
        return comment;
      }

      return null;
    } catch (SQLException e) {
      if (e.getErrorCode() == DUPLICATE_PK_ERROR_CODE) {
        return create(comment);
      }

      LOG.error("Failed to create proposal " + comment, e);
      return null;
    }
  }

  @Override
  public Comment deleteById(UUID id) {
    return null;
  }

  @Override
  public Comment vote(UUID commentId, UUID voterId, CommentVoteOption option) {
    return null;
  }

  protected static String truncate(String string, int limit) {
    return string.length() > limit ? string.substring(0, limit) : string;
  }
}
