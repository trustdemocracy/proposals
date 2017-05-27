package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.util.CommentMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.out.EventsGateway;
import eu.trustdemocracy.proposals.gateways.repositories.CommentRepository;
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import lombok.val;

public class CreateComment implements Interactor<CommentRequestDTO, CommentResponseDTO> {

  private CommentRepository commentRepository;
  private ProposalRepository proposalRepository;
  private EventsGateway eventsGateway;

  public CreateComment(
      CommentRepository commentRepository,
      ProposalRepository proposalRepository,
      EventsGateway eventsGateway
  ) {
    this.commentRepository = commentRepository;
    this.proposalRepository = proposalRepository;
    this.eventsGateway = eventsGateway;
  }

  @Override
  public CommentResponseDTO execute(CommentRequestDTO commentRequestDTO) {
    val user = UserMapper.createEntity(commentRequestDTO.getAuthorToken());

    val foundProposal = proposalRepository.findById(commentRequestDTO.getProposalId());
    if (foundProposal == null) {
      throw new ResourceNotFoundException(
          "Trying to comment on non-existing proposal [" + commentRequestDTO.getProposalId() + "]");
    }
    if (foundProposal.getStatus().equals(ProposalStatus.UNPUBLISHED)) {
      throw new ResourceNotFoundException(
          "Trying to comment on unpublished proposal [" + commentRequestDTO.getProposalId() + "]");
    }

    val comment = CommentMapper.createEntity(commentRequestDTO);
    comment.setProposal(foundProposal);

    val createdComment = commentRepository.create(comment);
    createdComment.setProposal(foundProposal);

    eventsGateway.createCommentEvent(createdComment);

    return CommentMapper.createResponse(createdComment);
  }
}
