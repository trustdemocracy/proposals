package eu.trustdemocracy.proposals.infrastructure;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.interactors.comment.GetComments;
import eu.trustdemocracy.proposals.core.interactors.comment.VoteComment;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
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
  public Interactor<ProposalRequestDTO, ProposalResponseDTO> createProposalInteractor(
      Class<? extends Interactor<ProposalRequestDTO, ProposalResponseDTO>> concreteClass) {
    try {
      val constructor = concreteClass.getConstructor(ProposalDAO.class);
      val proposalDAO = new MySqlProposalDAO(getConnection());
      return constructor.newInstance(proposalDAO);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public Interactor<CommentRequestDTO, CommentResponseDTO> createCommentInteractor(
      Class<? extends Interactor<CommentRequestDTO, CommentResponseDTO>> concreteClass) {
    try {
      val constructor = concreteClass.getConstructor(CommentDAO.class);
      val commentDAO = new MySqlCommentDAO(getConnection());
      return constructor.newInstance(commentDAO);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public GetComments createGetCommentsInteractor() {
    return new GetComments(new MySqlCommentDAO(getConnection()), new MySqlProposalDAO(getConnection()));
  }

  @Override
  public VoteComment createVoteCommentInteractor() {
    return new VoteComment(new MySqlCommentDAO(getConnection()));
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
        "`author` VARCHAR(" + MySqlProposalDAO.AUTHOR_SIZE + "), " +
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
