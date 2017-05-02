package eu.trustdemocracy.proposals.gateways.mysql;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.User;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;
import lombok.val;

public class MySqlProposalDAO implements ProposalDAO {

  private static final Logger LOG = LoggerFactory.getLogger(MySqlProposalDAO.class);

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

  public MySqlProposalDAO(Connection conn) {
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
          + "(id, author, title, brief, source, motivation, measures, status) "
          + "VALUES(?, ?, ?, ?, ?, ?, ?, ?)";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, id.toString());
      statement.setString(2, truncate(proposal.getAuthor().getUsername(), AUTHOR_SIZE));
      statement.setString(3, truncate(proposal.getTitle(), TITLE_SIZE));
      statement.setString(4, truncate(proposal.getBrief(), BRIEF_SIZE));
      statement.setString(5, truncate(proposal.getSource(), SOURCE_SIZE));
      statement.setString(6, truncate(proposal.getMotivation(), MOTIVATION_SIZE));
      statement.setString(7, truncate(proposal.getMeasures(), MEASURES_SIZE));
      statement.setString(8, ProposalStatus.UNPUBLISHED.toString());

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
      val sql = "SELECT id, author, title, brief, source, motivation, measures, status "
          + "FROM `" + TABLE + "` WHERE id = ?";
      val statement = conn.prepareStatement(sql);

      statement.setString(1, id.toString());
      val resultSet = statement.executeQuery();

      if (!resultSet.next()) {
        return null;
      }

      return new Proposal()
          .setId(UUID.fromString(resultSet.getString("id")))
          .setAuthor(new User().setUsername(resultSet.getString("author")))
          .setTitle(resultSet.getString("title"))
          .setBrief(resultSet.getString("brief"))
          .setSource(resultSet.getString("source"))
          .setMotivation(resultSet.getString("motivation"))
          .setMeasures(resultSet.getString("measures"))
          .setStatus(ProposalStatus.valueOf(resultSet.getString("status")));
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
      LOG.error("Failed to create proposal with id " + id, e);
      return null;
    }
  }

  @Override
  public Proposal setStatus(UUID id, ProposalStatus status) {
    return null;
  }

  protected static String truncate(String string, int limit) {
    return string.length() > limit ? string.substring(0, limit) : string;
  }
}
