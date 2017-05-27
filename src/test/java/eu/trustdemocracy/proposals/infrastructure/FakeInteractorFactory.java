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
import eu.trustdemocracy.proposals.core.interactors.proposal.GetProposals;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.UnpublishProposal;
import eu.trustdemocracy.proposals.gateways.events.EventsGateway;
import eu.trustdemocracy.proposals.gateways.events.FakeEventsGateway;
import eu.trustdemocracy.proposals.gateways.out.FakeVotesGateway;
import eu.trustdemocracy.proposals.gateways.out.VotesGateway;
import eu.trustdemocracy.proposals.gateways.repositories.CommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlCommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository;
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
  public GetProposals getGetProposals() {
    return new GetProposals(getProposalDAO());
  }

  @Override
  public PublishProposal getPublishProposal() {
    return new PublishProposal(getProposalDAO(), getEventsGateway(), getVotesGateway());
  }

  @Override
  public UnpublishProposal getUnpublishProposal() {
    return new UnpublishProposal(getProposalDAO(), getVotesGateway());
  }

  @Override
  public CreateComment getCreateComment() {
    return new CreateComment(getCommentDAO(), getProposalDAO(), getEventsGateway());
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

  private ProposalRepository getProposalDAO() {
    return new MySqlProposalRepository(getConnection());
  }

  private CommentRepository getCommentDAO() {
    return new MySqlCommentRepository(getConnection());
  }

  private EventsGateway getEventsGateway() {
    return new FakeEventsGateway();
  }

  private VotesGateway getVotesGateway() {
    return new FakeVotesGateway();
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
        "`id` VARCHAR(" + MySqlProposalRepository.ID_SIZE + ") NOT NULL, " +
        "`author_id` VARCHAR(" + MySqlProposalRepository.AUTHOR_SIZE + "), " +
        "`author_username` VARCHAR(" + MySqlProposalRepository.AUTHOR_SIZE + "), " +
        "`title` VARCHAR(" + MySqlProposalRepository.TITLE_SIZE + "), " +
        "`brief` VARCHAR(" + MySqlProposalRepository.BRIEF_SIZE + "), " +
        "`source` VARCHAR(" + MySqlProposalRepository.SOURCE_SIZE + "), " +
        "`motivation` TEXT(" + MySqlProposalRepository.MOTIVATION_SIZE + "), " +
        "`measures` TEXT(" + MySqlProposalRepository.MEASURES_SIZE + "), " +
        "`status` VARCHAR(" + MySqlProposalRepository.STATUS_SIZE + "), " +
        "PRIMARY KEY ( id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    val comments = "CREATE TABLE `comments` (" +
        "`id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`proposal_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`root_comment_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`author_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`author_username` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`content` VARCHAR(" + MySqlCommentRepository.CONTENT_SIZE + "), " +
        "`created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP, " +
        "PRIMARY KEY ( id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci; ";

    val votes = "CREATE TABLE `votes` (" +
        "`comment_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`voter_id` VARCHAR(" + MySqlCommentRepository.ID_SIZE + ") NOT NULL, " +
        "`option` VARCHAR(" + MySqlCommentRepository.OPTION_SIZE + ") NOT NULL, " +
        "PRIMARY KEY ( comment_id, voter_id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    val tables = new String[]{proposals, comments, votes};

    for (val sql : tables) {
      val statement = connection.createStatement();
      statement.executeUpdate(sql);
    }
  }
}
