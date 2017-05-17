package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.util.CommentMapper;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.interactors.exceptions.ResourceNotFoundException;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import java.util.ArrayList;
import java.util.List;
import lombok.val;

public class GetComments implements Interactor<ProposalRequestDTO, List<CommentResponseDTO>> {

  private CommentDAO commentDAO;
  private ProposalDAO proposalDAO;

  public GetComments(CommentDAO commentDAO, ProposalDAO proposalDAO) {
    this.commentDAO = commentDAO;
    this.proposalDAO = proposalDAO;
  }

  @Override
  public List<CommentResponseDTO> execute(ProposalRequestDTO proposalRequestDTO) {
    val user = UserMapper.createEntity(proposalRequestDTO.getAuthorToken());

    val foundProposal = proposalDAO.findById(proposalRequestDTO.getId());
    if (foundProposal == null) {
      throw new ResourceNotFoundException(
          "Trying to get comments on non-existing proposal [" + proposalRequestDTO.getId() + "]");
    }
    if (!user.hasAccess(foundProposal)) {
      throw new ResourceNotFoundException(
          "Trying to get comments on unpublished proposal [" + foundProposal.getId()  + "]");
    }

    List<Comment> commentList = commentDAO.findByProposalId(proposalRequestDTO.getId());

    List<CommentResponseDTO> responseComments = new ArrayList<>();
    for (val comment : commentList) {
      responseComments.add(CommentMapper.createResponse(comment));
    }

    return responseComments;
  }
}
