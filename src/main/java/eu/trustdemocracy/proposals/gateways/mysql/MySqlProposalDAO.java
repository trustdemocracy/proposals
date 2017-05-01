package eu.trustdemocracy.proposals.gateways.mysql;

import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import java.sql.Connection;
import java.util.UUID;

public class MySqlProposalDAO implements ProposalDAO {

  public MySqlProposalDAO(Connection conn) {
  }

  @Override
  public Proposal create(Proposal proposal) {
    return null;
  }

  @Override
  public Proposal findById(UUID id) {
    return null;
  }

  @Override
  public Proposal delete(UUID id) {
    return null;
  }

  @Override
  public Proposal setStatus(UUID id, ProposalStatus status) {
    return null;
  }
}
