package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.entities.util.CommentMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
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

    val foundComment = commentDAO.findById(commentRequestDTO.getId());

    if (foundComment == null) {
      throw new ResourceNotFoundException(
          "Trying to delete non-existing comment [" + commentRequestDTO.getId() + "]");
    }

    if (!foundComment.getAuthor().getId().equals(user.getId())) {
      throw new NotAllowedActionException(
          "Failed to delete comment [" + foundComment.getId()
              + "] in proposal [" + foundComment.getProposalId()
              + "]. User [" + user.getId() + "] is not the owner");
    }

    val comment = commentDAO.deleteById(commentRequestDTO.getId());
    return CommentMapper.createResponse(comment);
  }
}
