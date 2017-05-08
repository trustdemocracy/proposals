package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.CommentVoteRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;

public class VoteComment implements Interactor<CommentVoteRequestDTO, CommentResponseDTO> {

  public VoteComment(CommentDAO commentDAO) {
  }

  @Override
  public CommentResponseDTO execute(CommentVoteRequestDTO commentVoteRequestDTO) {
    return null;
  }
}
