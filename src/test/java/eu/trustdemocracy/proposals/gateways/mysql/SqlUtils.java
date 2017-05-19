package eu.trustdemocracy.proposals.gateways.mysql;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Stack;
import lombok.val;

public final class SqlUtils {

  private Stack<Connection> connectionStack = new Stack<>();
  private DBConfigurationBuilder configBuilder;
  private DB db;

  public void createProposalsTable() throws SQLException {
    val statement = getConnection().createStatement();
    val sql = "CREATE TABLE `proposals` (" +

        "`id` VARCHAR(" + MySqlProposalDAO.ID_SIZE + ") NOT NULL, " +
        "`author_id` VARCHAR(" + MySqlProposalDAO.ID_SIZE + "), " +
        "`author_username` VARCHAR(" + MySqlProposalDAO.AUTHOR_SIZE + "), " +
        "`title` VARCHAR(" + MySqlProposalDAO.TITLE_SIZE + "), " +
        "`brief` VARCHAR(" + MySqlProposalDAO.BRIEF_SIZE + "), " +
        "`source` VARCHAR(" + MySqlProposalDAO.SOURCE_SIZE + "), " +
        "`motivation` TEXT(" + MySqlProposalDAO.MOTIVATION_SIZE + "), " +
        "`measures` TEXT(" + MySqlProposalDAO.MEASURES_SIZE + "), " +
        "`status` VARCHAR(" + MySqlProposalDAO.STATUS_SIZE + "), " +

        "PRIMARY KEY ( id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    statement.executeUpdate(sql);
    statement.close();
    connectionStack.pop();
  }

  public void createCommentsAndVotesTables() throws SQLException {
    createCommentsTable();
    createVotesTable();
  }

  private void createCommentsTable() throws SQLException {
    val statement = getConnection().createStatement();
    val sql = "CREATE TABLE `comments` (" +

        "`id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`proposal_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`root_comment_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`author_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`author_username` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`content` VARCHAR(" + MySqlCommentDAO.CONTENT_SIZE + "), " +
        "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +

        "PRIMARY KEY ( id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci; ";

    statement.executeUpdate(sql);
    statement.close();
    connectionStack.pop();
  }

  private void createVotesTable() throws SQLException {
    val statement = getConnection().createStatement();
    val sql = "CREATE TABLE `votes` (" +

        "`comment_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`voter_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`option` VARCHAR(" + MySqlCommentDAO.OPTION_SIZE + ") NOT NULL, " +

        "PRIMARY KEY ( comment_id, voter_id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    statement.executeUpdate(sql);
    statement.close();
    connectionStack.pop();
  }

  public void startDB() throws ManagedProcessException {
    configBuilder = DBConfigurationBuilder.newBuilder();
    configBuilder.setPort(0);

    db = DB.newEmbeddedDB(configBuilder.build());
    db.start();
  }

  public void stopDB() throws SQLException, ManagedProcessException {
    db.stop();
    while (!connectionStack.isEmpty()) {
      connectionStack.pop().close();
    }
  }

  public Connection getConnection() {
    try {
      val conn = DriverManager
          .getConnection(configBuilder.getURL("test"), "root", "");
      connectionStack.push(conn);
      return conn;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}
