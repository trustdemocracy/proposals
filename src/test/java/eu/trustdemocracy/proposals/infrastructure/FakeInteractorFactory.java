package eu.trustdemocracy.proposals.infrastructure;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import eu.trustdemocracy.proposals.core.interactors.comment.CreateComment;
import eu.trustdemocracy.proposals.core.interactors.comment.DeleteComment;
import eu.trustdemocracy.proposals.core.interactors.comment.GetComments;
import eu.trustdemocracy.proposals.core.interactors.comment.VoteComment;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.DeleteProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.GetProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.UnpublishProposal;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import eu.trustdemocracy.proposals.gateways.mysql.MySqlCommentDAO;
import eu.trustdemocracy.proposals.gateways.mysql.MySqlProposalDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.val;

public class FakeInteractorFactory implements InteractorFactory {

  private Connection connection;

  @Override
  public CreateProposal getCreateProposal() {
    return new CreateProposal(getProposalDAO());
  }

  @Override
  public DeleteProposal getDeleteProposal() {
    return new DeleteProposal(getProposalDAO());
  }

  @Override
  public GetProposal getGetProposal() {
    return new GetProposal(getProposalDAO());
  }

  @Override
  public PublishProposal getPublishProposal() {
    return new PublishProposal(getProposalDAO());
  }

  @Override
  public UnpublishProposal getUnpublishProposal() {
    return new UnpublishProposal(getProposalDAO());
  }

  @Override
  public CreateComment getCreateComment() {
    return new CreateComment(getCommentDAO(), getProposalDAO());
  }

  @Override
  public DeleteComment getDeleteComment() {
    return new DeleteComment(getCommentDAO());
  }

  @Override
  public GetComments getGetComments() {
    return new GetComments(getCommentDAO(), getProposalDAO());
  }

  @Override
  public VoteComment getVoteComment() {
    return new VoteComment(getCommentDAO());
  }

  private ProposalDAO getProposalDAO() {
    return new MySqlProposalDAO(getConnection());
  }

  private CommentDAO getCommentDAO() {
    return new MySqlCommentDAO(getConnection());
  }


  private Connection getConnection() {
    if (connection == null) {
      try {
        val configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(0);

        val db = DB.newEmbeddedDB(configBuilder.build());
        db.start();
        connection = DriverManager
            .getConnection(configBuilder.getURL("test"), "root", "");

        buildTables(connection);
      } catch (SQLException | ManagedProcessException e) {
        throw new RuntimeException(e);
      }
    }
    return connection;
  }

  private void buildTables(Connection connection) throws SQLException {
    val proposals = "CREATE TABLE `proposals` (" +
        "`id` VARCHAR(" + MySqlProposalDAO.ID_SIZE + ") NOT NULL, " +
        "`author_id` VARCHAR(" + MySqlProposalDAO.AUTHOR_SIZE + "), " +
        "`author_username` VARCHAR(" + MySqlProposalDAO.AUTHOR_SIZE + "), " +
        "`title` VARCHAR(" + MySqlProposalDAO.TITLE_SIZE + "), " +
        "`brief` VARCHAR(" + MySqlProposalDAO.BRIEF_SIZE + "), " +
        "`source` VARCHAR(" + MySqlProposalDAO.SOURCE_SIZE + "), " +
        "`motivation` TEXT(" + MySqlProposalDAO.MOTIVATION_SIZE + "), " +
        "`measures` TEXT(" + MySqlProposalDAO.MEASURES_SIZE + "), " +
        "`status` VARCHAR(" + MySqlProposalDAO.STATUS_SIZE + "), " +
        "PRIMARY KEY ( id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    val comments = "CREATE TABLE `comments` (" +
        "`id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`proposal_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`root_comment_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`author_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`author_username` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`content` VARCHAR(" + MySqlCommentDAO.CONTENT_SIZE + "), " +
        "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
        "PRIMARY KEY ( id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci; ";

    val votes = "CREATE TABLE `votes` (" +
        "`comment_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`voter_id` VARCHAR(" + MySqlCommentDAO.ID_SIZE + ") NOT NULL, " +
        "`option` VARCHAR(" + MySqlCommentDAO.OPTION_SIZE + ") NOT NULL, " +
        "PRIMARY KEY ( comment_id, voter_id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    val tables = new String[]{proposals, comments, votes};

    for (val sql : tables) {
      val statement = connection.createStatement();
      statement.executeUpdate(sql);
    }
  }
}
