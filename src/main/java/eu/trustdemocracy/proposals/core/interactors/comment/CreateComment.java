package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.entities.util.CommentMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import lombok.val;

public class CreateComment implements Interactor<CommentRequestDTO, CommentResponseDTO> {

  private CommentDAO commentDAO;

  public CreateComment(CommentDAO commentDAO) {
    this.commentDAO = commentDAO;
  }

  @Override
  public CommentResponseDTO execute(CommentRequestDTO commentRequestDTO) {
    val comment = CommentMapper.createEntity(commentRequestDTO);
    return CommentMapper.createResponse(commentDAO.create(comment));
  }
}
