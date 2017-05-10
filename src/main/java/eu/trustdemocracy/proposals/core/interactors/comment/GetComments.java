package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.util.CommentMapper;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import java.util.ArrayList;
import java.util.List;
import lombok.val;

public class GetComments implements Interactor<ProposalRequestDTO, List<CommentResponseDTO>> {

  private CommentDAO commentDAO;

  public GetComments(CommentDAO commentDAO) {
    this.commentDAO = commentDAO;
  }

  @Override
  public List<CommentResponseDTO> execute(ProposalRequestDTO proposalRequestDTO) {
    List<Comment> commentList = commentDAO.findByProposalId(proposalRequestDTO.getId());

    List<CommentResponseDTO> responseComments = new ArrayList<>();
    for (val comment : commentList) {
      responseComments.add(CommentMapper.createResponse(comment));
    }

    return responseComments;
  }
}
