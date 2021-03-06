package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.entities.util.CommentMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.interactors.exceptions.NotAllowedActionException;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.models.request.CommentVoteRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.repositories.CommentRepository;
import lombok.val;

public class VoteComment implements Interactor<CommentVoteRequestDTO, CommentResponseDTO> {

  private CommentRepository commentRepository;

  public VoteComment(CommentRepository commentRepository) {
    this.commentRepository = commentRepository;
  }

  @Override
  public CommentResponseDTO execute(CommentVoteRequestDTO commentVoteRequestDTO) {
    val voter = UserMapper.createEntity(commentVoteRequestDTO.getVoterToken());
    val commentId = commentVoteRequestDTO.getCommentId();

    val foundComment = commentRepository.findById(commentId);

    if (foundComment == null) {
      throw new ResourceNotFoundException(
          "Trying to vote non-existing comment [" + commentId + "]");
    }

    if (foundComment.getAuthor().getId().equals(voter.getId())) {
      throw new NotAllowedActionException(
          "Failed to vote comment [" + foundComment.getId()
              + "] in proposal [" + foundComment.getProposal().getId()
              + "]. User [" + voter.getId() + "] is the owner of the comment");
    }

    val option = commentVoteRequestDTO.getOption();

    val comment = commentRepository.vote(commentId, voter.getId(), option);
    return CommentMapper.createResponse(comment);
  }
}
