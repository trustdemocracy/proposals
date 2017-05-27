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
import eu.trustdemocracy.proposals.core.interactors.proposal.UpdateResult;
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
    return new CreateProposal(getProposalRepository());
  }

  @Override
  public DeleteProposal getDeleteProposal() {
    return new DeleteProposal(getProposalRepository());
  }

  @Override
  public GetProposal getGetProposal() {
    return new GetProposal(getProposalRepository());
  }

  @Override
  public GetProposals getGetProposals() {
    return new GetProposals(getProposalRepository());
  }

  @Override
  public PublishProposal getPublishProposal() {
    return new PublishProposal(getProposalRepository(), getEventsGateway(), getVotesGateway());
  }

  @Override
  public UnpublishProposal getUnpublishProposal() {
    return new UnpublishProposal(getProposalRepository(), getVotesGateway());
  }

  @Override
  public CreateComment getCreateComment() {
    return new CreateComment(getCommentRepository(), getProposalRepository(), getEventsGateway());
  }

  @Override
  public DeleteComment getDeleteComment() {
    return new DeleteComment(getCommentRepository());
  }

  @Override
  public GetComments getGetComments() {
    return new GetComments(getCommentRepository(), getProposalRepository());
  }

  @Override
  public VoteComment getVoteComment() {
    return new VoteComment(getCommentRepository());
  }

  @Override
  public UpdateResult getUpdateResult() {
    return new UpdateResult(getProposalRepository());
  }

  private ProposalRepository getProposalRepository() {
    return new MySqlProposalRepository(getConnection());
  }

  private CommentRepository getCommentRepository() {
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
