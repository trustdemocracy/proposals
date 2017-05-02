package eu.trustdemocracy.proposals.gateways.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.vorburger.mariadb4j.DB;
import ch.vorburger.mariadb4j.DBConfigurationBuilder;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Stack;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MySqlProposalDAOTest {

  private DB db;
  private DBConfigurationBuilder configBuilder;
  private Stack<Connection> connectionStack = new Stack<>();

  private Lorem lorem = LoremIpsum.getInstance();

  private ProposalDAO proposalDAO;

  @BeforeEach
  public void init() throws Exception {
    TokenUtils.generateKeys();

    configBuilder = DBConfigurationBuilder.newBuilder();
    configBuilder.setPort(0);

    db = DB.newEmbeddedDB(configBuilder.build());
    db.start();

    val connection = getConnection();
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

    proposalDAO = new MySqlProposalDAO(getConnection());
  }

  @AfterEach
  public void tearDown() throws SQLException {
    while (!connectionStack.isEmpty()) {
      connectionStack.pop().close();
    }
  }

  @Test
  public void createProposal() throws SQLException {
    val proposal = createRandomProposal();

    val resultProposal = proposalDAO.create(proposal);

    val connection = getConnection();
    val sql = "SELECT * FROM `proposals` WHERE id = ?";
    val statement = connection.prepareStatement(sql);
    statement.setString(1, resultProposal.getId().toString());
    val resultSet = statement.executeQuery();

    assertTrue(resultSet.next());
    assertEquals(resultProposal.getId(), UUID.fromString(resultSet.getString("id")));
    assertEquals(proposal.getTitle(), resultSet.getString("title"));
    assertEquals(proposal.getBrief(), resultSet.getString("brief"));
    assertEquals(proposal.getSource(), resultSet.getString("source"));
    assertEquals(proposal.getMotivation(), resultSet.getString("motivation"));
    assertEquals(proposal.getMeasures(), resultSet.getString("measures"));
    assertEquals(ProposalStatus.UNPUBLISHED.toString(), resultSet.getString("status"));
  }

  @Test
  public void findProposal() throws SQLException {
    val proposal = createRandomProposal();

    val createdProposal = proposalDAO.create(proposal);
    val resultProposal = proposalDAO.findById(createdProposal.getId());

    assertEquals(createdProposal.getId(), resultProposal.getId());
    assertEquals(proposal.getTitle(), resultProposal.getTitle());
    assertEquals(proposal.getBrief(), resultProposal.getBrief());
    assertEquals(proposal.getSource(), resultProposal.getSource());
    assertEquals(proposal.getMotivation(), resultProposal.getMotivation());
    assertEquals(proposal.getMeasures(), resultProposal.getMeasures());
    assertEquals(ProposalStatus.UNPUBLISHED, resultProposal.getStatus());
  }

  @Test
  public void deleteProposal() throws SQLException {
    val proposal = createRandomProposal();
    val resultProposal = proposalDAO.create(proposal);

    assertNotNull(resultProposal.getId());

    proposalDAO.delete(resultProposal.getId());

    val connection = getConnection();
    val sql = "SELECT * FROM `proposals` WHERE id = ?";
    val statement = connection.prepareStatement(sql);
    statement.setString(1, resultProposal.getId().toString());
    val resultSet = statement.executeQuery();

    assertFalse(resultSet.next());
  }

  private Connection getConnection() {
    try {
      val conn = DriverManager
          .getConnection(configBuilder.getURL("test"), "root", "");
      connectionStack.push(conn);
      return conn;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private Proposal createRandomProposal() {
    val username = MySqlProposalDAO.truncate(lorem.getEmail(), MySqlProposalDAO.AUTHOR_SIZE);
    val title = MySqlProposalDAO.truncate(lorem.getTitle(5, 30), MySqlProposalDAO.TITLE_SIZE);
    val brief = MySqlProposalDAO.truncate(lorem.getParagraphs(1, 1), MySqlProposalDAO.BRIEF_SIZE);
    val source = MySqlProposalDAO.truncate(lorem.getUrl(), MySqlProposalDAO.SOURCE_SIZE);
    val motivation =
        MySqlProposalDAO.truncate(lorem.getParagraphs(1, 5), MySqlProposalDAO.MOTIVATION_SIZE);
    val measures =
        MySqlProposalDAO.truncate(lorem.getParagraphs(1, 5), MySqlProposalDAO.MEASURES_SIZE);

    val user = UserMapper.createEntity(TokenUtils.createToken(UUID.randomUUID(), username));
    return new Proposal()
        .setAuthor(user)
        .setTitle(title)
        .setBrief(brief)
        .setSource(source)
        .setMotivation(motivation)
        .setMeasures(measures);
  }

}
