package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.entities.util.CommentMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.CommentVoteRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import lombok.val;

public class VoteComment implements Interactor<CommentVoteRequestDTO, CommentResponseDTO> {

  private CommentDAO commentDAO;

  public VoteComment(CommentDAO commentDAO) {
    this.commentDAO = commentDAO;
  }

  @Override
  public CommentResponseDTO execute(CommentVoteRequestDTO commentVoteRequestDTO) {
    val commentId = commentVoteRequestDTO.getCommentId();
    val voter = UserMapper.createEntity(commentVoteRequestDTO.getVoterToken());
    val option = commentVoteRequestDTO.getOption();

    return CommentMapper.createResponse(commentDAO.vote(commentId, voter.getId(), option));
  }
}
