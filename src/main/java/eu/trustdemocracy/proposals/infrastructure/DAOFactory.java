package eu.trustdemocracy.proposals.infrastructure;

import eu.trustdemocracy.proposals.gateways.ProposalDAO;

public class DAOFactory {

  private static ProposalDAO proposalDAO;
  private static Object commentDAO;

  public static ProposalDAO getProposalDAO() {
    return proposalDAO;
  }

  public static Object getCommentDAO() {
    return commentDAO;
  }
}
