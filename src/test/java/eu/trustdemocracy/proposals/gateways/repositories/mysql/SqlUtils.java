package eu.trustdemocracy.proposals.gateways.repositories.mysql;

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

        "`id` VARCHAR(" + MySqlProposalRepository.ID_SIZE + ") NOT NULL, " +
        "`author_id` VARCHAR(" + MySqlProposalRepository.ID_SIZE + "), " +
        "`author_username` VARCHAR(" + MySqlProposalRepository.AUTHOR_SIZE + "), " +
        "`title` VARCHAR(" + MySqlProposalRepository.TITLE_SIZE + "), " +
        "`brief` VARCHAR(" + MySqlProposalRepository.BRIEF_SIZE + "), " +
        "`source` VARCHAR(" + MySqlProposalRepository.SOURCE_SIZE + "), " +
        "`motivation` TEXT(" + MySqlProposalRepository.MOTIVATION_SIZE + "), " +
        "`measures` TEXT(" + MySqlProposalRepository.MEASURES_SIZE + "), " +
        "`status` VARCHAR(" + MySqlProposalRepository.STATUS_SIZE + "), " +

        "PRIMARY KEY ( id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    statement.executeUpdate(sql);
    statement.close();
    connectionStack.pop();
  }

  public void createAllTables() throws SQLException {
    createProposalsTable();
    createCommentsTable();
    createVotesTable();
  }

  private void createCommentsTable() throws SQLException {
    val statement = getConnection().createStatement();
    val sql = "CREATE TABLE `comments` (" +

        "`id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`proposal_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`root_comment_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`author_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`author_username` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`content` VARCHAR(" + MySqlCommentRepository.CONTENT_SIZE + "), " +
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

        "`comment_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`voter_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`option` VARCHAR(" + MySqlCommentRepository.OPTION_SIZE + ") NOT NULL, " +

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
