package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.entities.util.CommentMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import lombok.val;

public class DeleteComment implements Interactor<CommentRequestDTO, CommentResponseDTO> {

  private CommentDAO commentDAO;

  public DeleteComment(CommentDAO commentDAO) {
    this.commentDAO = commentDAO;
  }

  @Override
  public CommentResponseDTO execute(CommentRequestDTO commentRequestDTO) {
    val user = UserMapper.createEntity(commentRequestDTO.getAuthorToken());
    return CommentMapper.createResponse(commentDAO.deleteById(commentRequestDTO.getId()));
  }
}
