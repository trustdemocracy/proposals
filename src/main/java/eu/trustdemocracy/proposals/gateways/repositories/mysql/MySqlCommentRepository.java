package eu.trustdemocracy.proposals.gateways.repositories.mysql;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.User;
import eu.trustdemocracy.proposals.gateways.repositories.CommentRepository;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;

public class MySqlCommentRepository implements CommentRepository {

  private static final int DUPLICATE_PK_ERROR_CODE = 1062;
  private static final String COMMENTS_TABLE = "comments";
  private static final String VOTES_TABLE = "votes";
  private static final String PROPOSALS_TABLE = "proposals";
  public static final int ID_SIZE = 36;
  public static final int AUTHOR_SIZE = 100;
  public static final int CONTENT_SIZE = 5000;
  public static final int OPTION_SIZE = 10;

  private static final Logger LOG = LoggerFactory.getLogger(MySqlProposalRepository.class);


  private Connection conn;

  public MySqlCommentRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public Comment create(Comment comment) {
    try {
      val id = UUID.randomUUID();
      comment.setId(id);

      comment.setContent(truncate(comment.getContent(), CONTENT_SIZE));
      val username = truncate(comment.getAuthor().getUsername(), AUTHOR_SIZE);

      val sql = "INSERT INTO `" + COMMENTS_TABLE + "` "
          + "(id, proposal_id, root_comment_id, author_id, author_username, content) "
          + "VALUES(?, ?, ?, ?, ?, ?)";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, id.toString());
      statement.setString(2, comment.getProposal().getId().toString());
      statement.setString(3, comment.getRootCommentId().toString());
      statement.setString(4, comment.getAuthor().getId().toString());
      statement.setString(5, username);
      statement.setString(6, comment.getContent());

      if (statement.executeUpdate() > 0) {
        return findById(id);
      }

      return null;
    } catch (SQLException e) {
      if (e.getErrorCode() == DUPLICATE_PK_ERROR_CODE) {
        return create(comment);
      }

      LOG.error("Failed to create comment " + comment, e);
      return null;
    }
  }

  @Override
  public Comment deleteById(UUID id) {
    try {
      val comment = findById(id);
      if (comment == null) {
        return null;
      }

      val sql = "DELETE FROM `" + COMMENTS_TABLE + "` WHERE id = ? ";
      val statement = conn.prepareStatement(sql);
      statement.setString(1, id.toString());

      if (statement.executeUpdate() > 0) {
        return comment;
      }

      return null;
    } catch (SQLException e) {
      LOG.error("Failed to delete comment with id " + id, e);
      return null;
    }
  }

  public Comment findById(UUID id) {
    try {
      val sql = "SELECT proposals.*, comments.*, votes.option, COUNT(votes.option) AS `count` "
          + "FROM `" + COMMENTS_TABLE + "` AS comments "
          + "LEFT JOIN `" + PROPOSALS_TABLE + "` AS proposals "
          + "ON comments.proposal_id = proposals.id "
          + "LEFT JOIN `" + VOTES_TABLE + "` AS votes "
          + "ON comments.id = votes.comment_id "
          + "WHERE comments.id = ? "
          + "GROUP BY comments.id, votes.option";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, id.toString());
      val resultSet = statement.executeQuery();

      if (!resultSet.next()) {
        return null;
      }

      val author = new User()
          .setId(UUID.fromString(resultSet.getString("comments.author_id")))
          .setUsername(resultSet.getString("comments.author_username"));

      val proposal = new Proposal()
          .setId(UUID.fromString(resultSet.getString("comments.proposal_id")))
          .setTitle(resultSet.getString("proposals.title"));

      val comment = new Comment()
          .setId(UUID.fromString(resultSet.getString("comments.id")))
          .setProposal(proposal)
          .setRootCommentId(UUID.fromString(resultSet.getString("comments.root_comment_id")))
          .setRootCommentId(UUID.fromString(resultSet.getString("comments.root_comment_id")))
          .setAuthor(author)
          .setContent(resultSet.getString("comments.content"))
          .setTimestamp(resultSet.getTimestamp("comments.created_at").getTime());

      if (resultSet.getString("votes.option") == null) {
        return comment;
      }

      do {
        val option = CommentVoteOption.valueOf(resultSet.getString("votes.option"));
        val count = Integer.valueOf(resultSet.getString("count"));
        comment.getVotes().put(option, count);
      } while (resultSet.next());

      return comment;
    } catch (SQLException e) {
      LOG.error("Failed to find comment with id " + id, e);
      return null;
    }
  }

  @Override
  public Comment vote(UUID commentId, UUID voterId, CommentVoteOption option) {
    try {
      deleteExistingVote(commentId, voterId);

      if (option == null) {
        return findById(commentId);
      }

      return insertNewVote(commentId, voterId, option);
    } catch (SQLException e) {
      if (e.getErrorCode() == DUPLICATE_PK_ERROR_CODE) {
        return vote(commentId, voterId, option);
      }

      LOG.error("Failed to create vote for comment [" + commentId
          + "], by voter [" + voterId + "] and with the option [" + option + "]", e);
      return null;
    }
  }

  @Override
  public List<Comment> findByProposalId(UUID proposalId) {
    try {
      val sql = "SELECT proposals.*, comments.*, votes.option, COUNT(votes.option) AS `count` "
          + "FROM `" + COMMENTS_TABLE + "` AS comments "
          + "LEFT JOIN `" + PROPOSALS_TABLE + "` AS proposals "
          + "ON comments.proposal_id = proposals.id "
          + "LEFT JOIN `" + VOTES_TABLE + "` AS votes "
          + "ON comments.id = votes.comment_id "
          + "WHERE comments.proposal_id = ? "
          + "GROUP BY comments.id, votes.option "
          + "ORDER BY comments.created_at, comments.id";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, proposalId.toString());
      val resultSet = statement.executeQuery();

      List<Comment> comments = new ArrayList<>();
      Comment currentComment = new Comment();

      while (resultSet.next()) {
        val commentId = UUID.fromString(resultSet.getString("comments.id"));

        if (!commentId.equals(currentComment.getId())) {
          val author = new User()
              .setId(UUID.fromString(resultSet.getString("comments.author_id")))
              .setUsername(resultSet.getString("comments.author_username"));

          val proposal = new Proposal()
              .setId(UUID.fromString(resultSet.getString("comments.proposal_id")))
              .setTitle(resultSet.getString("proposals.title"));

          currentComment = new Comment()
              .setId(commentId)
              .setProposal(proposal)
              .setRootCommentId(UUID.fromString(resultSet.getString("comments.root_comment_id")))
              .setRootCommentId(UUID.fromString(resultSet.getString("comments.root_comment_id")))
              .setAuthor(author)
              .setContent(resultSet.getString("comments.content"))
              .setTimestamp(resultSet.getTimestamp("comments.created_at").getTime());

          comments.add(currentComment);
        }

        if (resultSet.getString("votes.option") != null) {
          val option = CommentVoteOption.valueOf(resultSet.getString("votes.option"));
          val count = Integer.valueOf(resultSet.getString("count"));
          currentComment.getVotes().put(option, count);
        }
      }

      return comments;
    } catch (SQLException e) {
      LOG.error("Failed to find comments with proposal id " + proposalId, e);
      return null;
    }
  }

  private void deleteExistingVote(UUID commentId, UUID voterId) throws SQLException {
    val sql = "DELETE FROM `" + VOTES_TABLE + "` WHERE comment_id = ? AND voter_id = ?";
    val statement = conn.prepareStatement(sql);
    statement.setString(1, commentId.toString());
    statement.setString(2, voterId.toString());
    statement.executeUpdate();
  }

  private Comment insertNewVote(UUID commentId, UUID voterId, CommentVoteOption option)
      throws SQLException {
    val sql = "INSERT INTO `" + VOTES_TABLE + "` "
        + "(comment_id, voter_id, option) "
        + "VALUES(?, ?, ?)";
    val statement = conn.prepareStatement(sql);

    statement.setString(1, commentId.toString());
    statement.setString(2, voterId.toString());
    statement.setString(3, option.toString());

    if (statement.executeUpdate() > 0) {
      return findById(commentId);
    }

    return null;
  }

  protected static String truncate(String string, int limit) {
    return string.length() > limit ? string.substring(0, limit) : string;
  }
}
