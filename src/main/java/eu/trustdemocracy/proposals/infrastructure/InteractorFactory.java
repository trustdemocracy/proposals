package eu.trustdemocracy.proposals.infrastructure;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;

public interface InteractorFactory {

  Interactor<ProposalRequestDTO, ProposalResponseDTO> createProposalInteractor(
      Class<? extends Interactor<ProposalRequestDTO, ProposalResponseDTO>> concreteClass);

  Interactor<CommentRequestDTO, CommentResponseDTO> createCommentInteractor(
      Class<? extends Interactor<CommentRequestDTO, CommentResponseDTO>> concreteClass);
}
