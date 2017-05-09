package eu.trustdemocracy.proposals.gateways.mysql;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import java.sql.Connection;
import java.util.UUID;

public class MySqlCommentDAO implements CommentDAO {

  public static final int ID_SIZE = 36;
  public static final int CONTENT_SIZE = 36;

  public MySqlCommentDAO(Connection connection) {
  }

  @Override
  public Comment create(Comment comment) {
    return null;
  }

  @Override
  public Comment deleteById(UUID id) {
    return null;
  }

  @Override
  public Comment vote(UUID commentId, UUID voterId, CommentVoteOption option) {
    return null;
  }
}
