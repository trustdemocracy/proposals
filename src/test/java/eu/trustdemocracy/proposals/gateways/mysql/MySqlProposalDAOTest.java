package eu.trustdemocracy.proposals.gateways.mysql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import ch.vorburger.exec.ManagedProcessException;
import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.Proposal;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.entities.util.UserMapper;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import java.sql.SQLException;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MySqlProposalDAOTest {

  private SqlUtils sqlUtils;

  private Lorem lorem = LoremIpsum.getInstance();

  private ProposalDAO proposalDAO;

  @BeforeEach
  public void init() throws Exception {
    TokenUtils.generateKeys();

    sqlUtils = new SqlUtils();
    sqlUtils.startDB();

    sqlUtils.createProposalsTable();

    proposalDAO = new MySqlProposalDAO(sqlUtils.getConnection());
  }

  @AfterEach
  public void tearDown() throws SQLException, ManagedProcessException {
    sqlUtils.stopDB();
  }

  @Test
  public void createProposal() throws SQLException {
    val proposal = createRandomProposal();

    val resultProposal = proposalDAO.create(proposal);

    val connection = sqlUtils.getConnection();
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
  public void findProposal() {
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

    val connection = sqlUtils.getConnection();
    val sql = "SELECT * FROM `proposals` WHERE id = ?";
    val statement = connection.prepareStatement(sql);
    statement.setString(1, resultProposal.getId().toString());
    val resultSet = statement.executeQuery();

    assertFalse(resultSet.next());
  }

  @Test
  public void setStatus() throws SQLException {
    val proposal = createRandomProposal();

    val resultProposal = proposalDAO.create(proposal);
    assertEquals(ProposalStatus.UNPUBLISHED, resultProposal.getStatus());
    val publishedProposal = proposalDAO.setStatus(resultProposal.getId(), ProposalStatus.PUBLISHED);
    assertEquals(ProposalStatus.PUBLISHED, publishedProposal.getStatus());

    val connection = sqlUtils.getConnection();
    val sql = "SELECT * FROM `proposals` WHERE id = ?";
    val statement = connection.prepareStatement(sql);
    statement.setString(1, resultProposal.getId().toString());
    val publishedResultSet = statement.executeQuery();

    assertTrue(publishedResultSet.next());
    assertEquals(ProposalStatus.PUBLISHED.toString(), publishedResultSet.getString("status"));

    val unpublishedProposal = proposalDAO.setStatus(resultProposal.getId(), ProposalStatus.UNPUBLISHED);
    assertEquals(ProposalStatus.UNPUBLISHED, unpublishedProposal.getStatus());
    val unpublishedResultSet = statement.executeQuery();

    assertTrue(unpublishedResultSet.next());
    assertEquals(ProposalStatus.UNPUBLISHED.toString(), unpublishedResultSet.getString("status"));
  }

  @Test
  public void findByAuthorId() {
    val authorId = UUID.randomUUID();
    for (int i = 0; i< 20; i++) {
      val proposal = createRandomProposal();
      val user = proposal.getAuthor();
      user.setId(authorId);
      proposal.setAuthor(user);
      proposalDAO.create(proposal);
    }

    val proposals = proposalDAO.findByAuthorId(authorId);

    assertEquals(20, proposals.size());
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
        .setMeasures(measures)
        .setStatus(ProposalStatus.UNPUBLISHED);
  }

}
