package eu.trustdemocracy.proposals.core.interactors.comment;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.gateways.CommentDAO;
import java.util.List;

public class GetComments implements Interactor<ProposalRequestDTO, List<CommentResponseDTO>> {

  public GetComments(CommentDAO commentDAO) {
  }

  @Override
  public List<CommentResponseDTO> execute(ProposalRequestDTO proposalRequestDTO) {
    return null;
  }
}
