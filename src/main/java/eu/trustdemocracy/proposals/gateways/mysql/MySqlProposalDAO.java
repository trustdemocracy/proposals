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
      statement.setString(2, proposal.getAuthor().getUsername());
      statement.setString(3, proposal.getTitle());
      statement.setString(4, proposal.getBrief());
      statement.setString(5, proposal.getSource());
      statement.setString(6, proposal.getMotivation());
      statement.setString(7, proposal.getMeasures());
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
}
