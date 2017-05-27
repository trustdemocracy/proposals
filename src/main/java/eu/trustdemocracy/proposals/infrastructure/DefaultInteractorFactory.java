package eu.trustdemocracy.proposals.infrastructure;

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
import eu.trustdemocracy.proposals.gateways.out.EventsGatewayImpl;
import eu.trustdemocracy.proposals.gateways.out.VotesGateway;
import eu.trustdemocracy.proposals.gateways.out.VotesGatewayImpl;
import eu.trustdemocracy.proposals.gateways.repositories.CommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;

public class DefaultInteractorFactory implements InteractorFactory {

  private static DefaultInteractorFactory instance;

  private DefaultInteractorFactory() {
  }

  public static DefaultInteractorFactory getInstance() {
    if (instance == null) {
      instance = new DefaultInteractorFactory();
    }
    return instance;
  }

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

  private CommentRepository getCommentRepository() {
    return RepositoryFactory.getCommentRepository();
  }

  private ProposalRepository getProposalRepository() {
    return RepositoryFactory.getProposalRepository();
  }

  private EventsGateway getEventsGateway() {
    return new EventsGatewayImpl();
  }

  private VotesGateway getVotesGateway() {
    return new VotesGatewayImpl();
  }
}
