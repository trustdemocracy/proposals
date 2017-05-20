package eu.trustdemocracy.proposals.core.entities.util;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import java.util.HashMap;
import java.util.UUID;

public class CommentMapper {

  public static Comment createEntity(CommentRequestDTO commentRequestDTO) {
    Comment comment = new Comment()
        .setId(commentRequestDTO.getId())
        .setProposalId(commentRequestDTO.getProposalId())
        .setRootCommentId(commentRequestDTO.getRootCommentId())
        .setAuthor(UserMapper.createEntity(commentRequestDTO.getAuthorToken()))
        .setContent(commentRequestDTO.getContent());

    if (comment.getRootCommentId() == null) {
      comment.setRootCommentId(new UUID(0L, 0L));
    }

    return comment;
  }

  public static CommentResponseDTO createResponse(Comment comment) {
    return new CommentResponseDTO()
        .setId(comment.getId())
        .setProposalId(comment.getProposalId())
        .setRootCommentId(comment.getRootCommentId())
        .setAuthorUsername(comment.getAuthor().getUsername())
        .setContent(comment.getContent())
        .setTimestamp(comment.getTimestamp())
        .setVotes(new HashMap<>(comment.getVotes()));
  }
}
