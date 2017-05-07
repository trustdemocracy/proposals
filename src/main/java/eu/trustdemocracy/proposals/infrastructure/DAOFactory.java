package eu.trustdemocracy.proposals.infrastructure;

import eu.trustdemocracy.proposals.gateways.ProposalDAO;

public class DAOFactory {

  private static ProposalDAO proposalDAO;

  public static ProposalDAO getProposalDAO() {
    return proposalDAO;
  }
}
