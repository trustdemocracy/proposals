package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;

public class CreateComment implements Interactor<CommentRequestDTO, CommentResponseDTO> {

  public CreateComment(CommentDAO commentDAO) {
  }

  @Override
  public CommentResponseDTO execute(CommentRequestDTO commentRequestDTO) {
    return null;
  }
}
