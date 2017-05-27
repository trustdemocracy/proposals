package eu.trustdemocracy.proposals.infrastructure;

import ch.vorburger.exec.ManagedProcessException;
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
import eu.trustdemocracy.proposals.gateways.out.EventsGateway;
import eu.trustdemocracy.proposals.gateways.out.FakeEventsGateway;
import eu.trustdemocracy.proposals.gateways.out.FakeVotesGateway;
import eu.trustdemocracy.proposals.gateways.out.VotesGateway;
import eu.trustdemocracy.proposals.gateways.repositories.CommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlCommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository;
import eu.trustdemocracy.proposals.gateways.repositories.mysql.SqlUtils;
import java.sql.Connection;
import java.sql.SQLException;

public class FakeInteractorFactory implements InteractorFactory {

  private SqlUtils sql;

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
    if (sql == null) {
      try {
        sql = new SqlUtils();
        sql.startDB();
        sql.createAllTables();
      } catch (SQLException | ManagedProcessException e) {
        throw new RuntimeException(e);
      }
    }
    return sql.getConnection();
  }
}
