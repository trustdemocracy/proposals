package eu.trustdemocracy.proposals.infrastructure;

import ch.vorburger.exec.ManagedProcessException;
import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import eu.trustdemocracy.proposals.gateways.mysql.MySqlProposalDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import lombok.val;

public class FakeInteractorFactory implements InteractorFactory {

  private ProposalDAO proposalDAO;

  @Override
  public Interactor<ProposalRequestDTO, ProposalResponseDTO> createProposalInteractor(
      Class<? extends Interactor<ProposalRequestDTO, ProposalResponseDTO>> concreteClass) {
    try {
      val constructor = concreteClass.getConstructor(ProposalDAO.class);
      val proposalDAO = getProposalFakeDAO();
      return constructor.newInstance(proposalDAO);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  private ProposalDAO getProposalFakeDAO() {
    if (proposalDAO == null) {

      try {
        val configBuilder = DBConfigurationBuilder.newBuilder();
        configBuilder.setPort(0);

        val db = DB.newEmbeddedDB(configBuilder.build());
        db.start();
        val conn = DriverManager
            .getConnection(configBuilder.getURL("test"), "root", "");

        buildTables(conn);

        proposalDAO = new MySqlProposalDAO(conn);
      } catch (SQLException | ManagedProcessException e) {
        throw new RuntimeException(e);
      }
    }
    return proposalDAO;
  }

  private void buildTables(Connection connection) throws SQLException {
    val statement = connection.createStatement();
    val sql = "CREATE TABLE `proposals` (" +

        "`id` VARCHAR(" + MySqlProposalDAO.ID_SIZE + ") NOT NULL, " +
        "`author` VARCHAR(" + MySqlProposalDAO.AUTHOR_SIZE + "), " +
        "`title` VARCHAR(" + MySqlProposalDAO.TITLE_SIZE + "), " +
        "`brief` VARCHAR(" + MySqlProposalDAO.BRIEF_SIZE + "), " +
        "`source` VARCHAR(" + MySqlProposalDAO.SOURCE_SIZE + "), " +
        "`motivation` TEXT(" + MySqlProposalDAO.MOTIVATION_SIZE + "), " +
        "`measures` TEXT(" + MySqlProposalDAO.MEASURES_SIZE + "), " +
        "`status` VARCHAR(" + MySqlProposalDAO.STATUS_SIZE + "), " +

        "PRIMARY KEY ( id ) " +
        ") ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_unicode_ci;";

    statement.executeUpdate(sql);
  }
}
