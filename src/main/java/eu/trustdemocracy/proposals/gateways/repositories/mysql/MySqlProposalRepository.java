package eu.trustdemocracy.proposals.gateways.repositories.mysql;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.User;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.val;

public class MySqlProposalRepository implements ProposalRepository {

  private static final Logger LOG = LoggerFactory.getLogger(MySqlProposalRepository.class);

  private static final String TABLE = "proposals";
  public static final int ID_SIZE = 36;
  public static final int AUTHOR_SIZE = 100;
  public static final int TITLE_SIZE = 100;
  public static final int BRIEF_SIZE = 500;
  public static final int SOURCE_SIZE = 1000;
  public static final int MOTIVATION_SIZE = 20000;
  public static final int MEASURES_SIZE = 20000;
  public static final int STATUS_SIZE = 36;

  private Connection conn;

  public MySqlProposalRepository(Connection conn) {
    this.conn = conn;
  }

  @Override
  public Proposal create(Proposal proposal) {
    UUID id;
    do {
      id = UUID.randomUUID();
    } while (findById(id) != null);
    proposal.setId(id);

    try {
      val sql = "INSERT INTO `" + TABLE + "` "
          + "(id, author_id, author_username, title, brief, source, motivation, measures, status) "
          + "VALUES(?, ?, ?, ?, ?, ?, ?, ?, ?)";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, id.toString());
      statement.setString(2, proposal.getAuthor().getId().toString());
      statement.setString(3, truncate(proposal.getAuthor().getUsername(), AUTHOR_SIZE));
      statement.setString(4, truncate(proposal.getTitle(), TITLE_SIZE));
      statement.setString(5, truncate(proposal.getBrief(), BRIEF_SIZE));
      statement.setString(6, truncate(proposal.getSource(), SOURCE_SIZE));
      statement.setString(7, truncate(proposal.getMotivation(), MOTIVATION_SIZE));
      statement.setString(8, truncate(proposal.getMeasures(), MEASURES_SIZE));
      statement.setString(9, proposal.getStatus().toString());

      if (statement.executeUpdate() > 0) {
        return proposal;
      }

      return null;
    } catch (SQLException e) {
      LOG.error("Failed to create proposal " + proposal, e);
      return null;
    }
  }

  @Override
  public Proposal findById(UUID id) {
    try {
      val sql = "SELECT id, author_id, author_username, title, "
          + "brief, source, motivation, measures, status, due_date "
          + "FROM `" + TABLE + "` "
          + "WHERE id = ?";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, id.toString());
      val resultSet = statement.executeQuery();

      if (!resultSet.next()) {
        return null;
      }

      val user = new User()
          .setId(UUID.fromString(resultSet.getString("author_id")))
          .setUsername(resultSet.getString("author_username"));

      return new Proposal()
          .setId(UUID.fromString(resultSet.getString("id")))
          .setAuthor(user)
          .setTitle(resultSet.getString("title"))
          .setBrief(resultSet.getString("brief"))
          .setSource(resultSet.getString("source"))
          .setMotivation(resultSet.getString("motivation"))
          .setMeasures(resultSet.getString("measures"))
          .setStatus(ProposalStatus.valueOf(resultSet.getString("status")))
          .setDueDate(resultSet.getLong("due_date"));
    } catch (SQLException e) {
      LOG.error("Failed to find proposal with id " + id, e);
      return null;
    }
  }

  @Override
  public Proposal delete(UUID id) {
    try {
      val proposal = findById(id);
      if (proposal == null) {
        return null;
      }

      val sql = "DELETE FROM `" + TABLE + "` WHERE id = ? ";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, id.toString());

      if (statement.executeUpdate() > 0) {
        return proposal;
      }

      return null;
    } catch (SQLException e) {
      LOG.error("Failed to delete proposal with id " + id, e);
      return null;
    }
  }

  @Override
  public Proposal setStatus(UUID id, ProposalStatus status) {
    try {
      val proposal = findById(id);
      if (proposal == null) {
        return null;
      }

      val sql = "UPDATE `" + TABLE + "` "
          + "SET status = ?"
          + " WHERE id = ? ";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, status.toString());
      statement.setString(2, id.toString());

      if (statement.executeUpdate() > 0) {
        proposal.setStatus(status);
        return proposal;
      }

      return null;
    } catch (SQLException e) {
      LOG.error("Failed to update status in proposal with id " + id, e);
      return null;
    }
  }

  @Override
  public Proposal setStatus(UUID id, ProposalStatus status, long dueDate) {
    try {
      val proposal = findById(id);
      if (proposal == null) {
        return null;
      }

      val sql = "UPDATE `" + TABLE + "` "
          + "SET status = ?, due_date = ?"
          + " WHERE id = ? ";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, status.toString());
      statement.setLong(2, dueDate);
      statement.setString(3, id.toString());

      if (statement.executeUpdate() > 0) {
        proposal.setStatus(status);
        return proposal;
      }

      return null;
    } catch (SQLException e) {
      LOG.error("Failed to update status in proposal with id " + id, e);
      return null;
    }
  }

  @Override
  public List<Proposal> findByAuthorId(UUID authorId) {
    try {
      val sql = "SELECT id, author_id, author_username, title, "
          + "brief, source, motivation, measures, status, due_date "
          + "FROM `" + TABLE + "` "
          + "WHERE author_id = ? ";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, authorId.toString());
      val resultSet = statement.executeQuery();

      List<Proposal> proposals = new ArrayList<>();

      while (resultSet.next()) {
        val user = new User()
            .setId(UUID.fromString(resultSet.getString("author_id")))
            .setUsername(resultSet.getString("author_username"));

        val proposal = new Proposal()
            .setId(UUID.fromString(resultSet.getString("id")))
            .setAuthor(user)
            .setTitle(resultSet.getString("title"))
            .setBrief(resultSet.getString("brief"))
            .setSource(resultSet.getString("source"))
            .setMotivation(resultSet.getString("motivation"))
            .setMeasures(resultSet.getString("measures"))
            .setStatus(ProposalStatus.valueOf(resultSet.getString("status")))
            .setDueDate(resultSet.getLong("due_date"));

        proposals.add(proposal);
      }

      return proposals;
    } catch (SQLException e) {
      LOG.error("Failed to find proposals for author " + authorId, e);
      return null;
    }
  }

  @Override
  public List<Proposal> findByAuthorId(UUID authorId, ProposalStatus status) {
    try {
      val sql = "SELECT id, author_id, author_username, title, "
          + "brief, source, motivation, measures, status, due_date "
          + "FROM `" + TABLE + "` "
          + "WHERE author_id = ? AND status = ?";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, authorId.toString());
      statement.setString(2, status.toString());
      val resultSet = statement.executeQuery();

      List<Proposal> proposals = new ArrayList<>();

      while (resultSet.next()) {
        val user = new User()
            .setId(UUID.fromString(resultSet.getString("author_id")))
            .setUsername(resultSet.getString("author_username"));

        val proposal = new Proposal()
            .setId(UUID.fromString(resultSet.getString("id")))
            .setAuthor(user)
            .setTitle(resultSet.getString("title"))
            .setBrief(resultSet.getString("brief"))
            .setSource(resultSet.getString("source"))
            .setMotivation(resultSet.getString("motivation"))
            .setMeasures(resultSet.getString("measures"))
            .setStatus(ProposalStatus.valueOf(resultSet.getString("status")))
            .setDueDate(resultSet.getLong("due_date"));

        proposals.add(proposal);
      }

      return proposals;
    } catch (SQLException e) {
      LOG.error("Failed to find proposals for author " + authorId + " and status " + status, e);
      return null;
    }
  }

  @Override
  public List<Proposal> findAllPublished() {
    try {
      val sql = "SELECT id, author_id, author_username, title, "
          + "brief, source, motivation, measures, status, due_date "
          + "FROM `" + TABLE + "` "
          + "WHERE status = ?";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, ProposalStatus.PUBLISHED.toString());
      val resultSet = statement.executeQuery();

      List<Proposal> proposals = new ArrayList<>();

      while (resultSet.next()) {
        val user = new User()
            .setId(UUID.fromString(resultSet.getString("author_id")))
            .setUsername(resultSet.getString("author_username"));

        val proposal = new Proposal()
            .setId(UUID.fromString(resultSet.getString("id")))
            .setAuthor(user)
            .setTitle(resultSet.getString("title"))
            .setBrief(resultSet.getString("brief"))
            .setSource(resultSet.getString("source"))
            .setMotivation(resultSet.getString("motivation"))
            .setMeasures(resultSet.getString("measures"))
            .setStatus(ProposalStatus.valueOf(resultSet.getString("status")))
            .setDueDate(resultSet.getLong("due_date"));

        proposals.add(proposal);
      }

      return proposals;
    } catch (SQLException e) {
      LOG.error("Failed to find published proposals", e);
      return null;
    }
  }

  protected static String truncate(String string, int limit) {
    return string.length() > limit ? string.substring(0, limit) : string;
  }
}
