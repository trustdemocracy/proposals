package eu.trustdemocracy.proposals.infrastructure;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.interactors.comment.GetComments;
import eu.trustdemocracy.proposals.core.interactors.comment.VoteComment;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import lombok.val;

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
  public Interactor<ProposalRequestDTO, ProposalResponseDTO> createProposalInteractor(
      Class<? extends Interactor<ProposalRequestDTO, ProposalResponseDTO>> concreteClass) {
    try {
      val constructor = concreteClass.getConstructor(ProposalDAO.class);
      val proposalDAO = DAOFactory.getProposalDAO();
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
      val commentDAO = DAOFactory.getCommentDAO();
      return constructor.newInstance(commentDAO);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public GetComments createGetCommentsInteractor() {
    return new GetComments(DAOFactory.getCommentDAO());
  }

  @Override
  public VoteComment createVoteCommentInteractor() {
    return new VoteComment(DAOFactory.getCommentDAO());
  }
}
