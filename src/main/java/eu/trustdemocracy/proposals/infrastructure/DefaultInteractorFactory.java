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

  private CommentRepository getCommentDAO() {
    return RepositoryFactory.getCommentRepository();
  }

  private ProposalRepository getProposalDAO() {
    return RepositoryFactory.getProposalRepository();
  }

  private EventsGateway getEventsGateway() {
    return new EventsGatewayImpl();
  }

  private VotesGateway getVotesGateway() {
    return new VotesGatewayImpl();
  }
}
