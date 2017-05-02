package eu.trustdemocracy.proposals.gateways.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
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

        "`id` VARCHAR(36) NOT NULL, " +
        "`author` VARCHAR(255), " +
        "`title` VARCHAR(255), " +
        "`brief` VARCHAR(500), " +
        "`source` VARCHAR(1000), " +
        "`motivation` TEXT(20000), " +
        "`measures` TEXT(20000), " +
        "`status` VARCHAR(30), " +

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
    val user = UserMapper.createEntity(TokenUtils.createToken(UUID.randomUUID(), lorem.getEmail()));
    val proposal = new Proposal()
        .setAuthor(user)
        .setTitle(lorem.getTitle(5, 30))
        .setBrief(lorem.getParagraphs(1, 1))
        .setSource(lorem.getUrl())
        .setMotivation(lorem.getParagraphs(1, 5))
        .setMeasures(lorem.getParagraphs(1, 5));

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

}
