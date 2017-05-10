package eu.trustdemocracy.proposals.infrastructure;

import eu.trustdemocracy.proposals.gateways.CommentDAO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;

public class DAOFactory {

  private static ProposalDAO proposalDAO;
  private static CommentDAO commentDAO;

  public static ProposalDAO getProposalDAO() {
    return proposalDAO;
  }

  public static CommentDAO getCommentDAO() {
    return commentDAO;
  }
}
