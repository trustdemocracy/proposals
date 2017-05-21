package eu.trustdemocracy.proposals.gateways.repositories.mysql;

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
import eu.trustdemocracy.proposals.gateways.repositories.ProposalRepository;
import java.sql.SQLException;
import java.util.UUID;
import lombok.val;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class MySqlProposalRepositoryTest {

  private SqlUtils sqlUtils;

  private Lorem lorem = LoremIpsum.getInstance();

  private ProposalRepository proposalRepository;

  @BeforeEach
  public void init() throws Exception {
    TokenUtils.generateKeys();

    sqlUtils = new SqlUtils();
    sqlUtils.startDB();

    sqlUtils.createProposalsTable();

    proposalRepository = new eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository(sqlUtils.getConnection());
  }

  @AfterEach
  public void tearDown() throws SQLException, ManagedProcessException {
    sqlUtils.stopDB();
  }

  @Test
  public void createProposal() throws SQLException {
    val proposal = createRandomProposal();

    val resultProposal = proposalRepository.create(proposal);

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

    val createdProposal = proposalRepository.create(proposal);
    val resultProposal = proposalRepository.findById(createdProposal.getId());

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
    val resultProposal = proposalRepository.create(proposal);

    assertNotNull(resultProposal.getId());

    proposalRepository.delete(resultProposal.getId());

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

    val resultProposal = proposalRepository.create(proposal);
    assertEquals(ProposalStatus.UNPUBLISHED, resultProposal.getStatus());
    val publishedProposal = proposalRepository.setStatus(resultProposal.getId(), ProposalStatus.PUBLISHED);
    assertEquals(ProposalStatus.PUBLISHED, publishedProposal.getStatus());

    val connection = sqlUtils.getConnection();
    val sql = "SELECT * FROM `proposals` WHERE id = ?";
    val statement = connection.prepareStatement(sql);
    statement.setString(1, resultProposal.getId().toString());
    val publishedResultSet = statement.executeQuery();

    assertTrue(publishedResultSet.next());
    assertEquals(ProposalStatus.PUBLISHED.toString(), publishedResultSet.getString("status"));

    val unpublishedProposal = proposalRepository
        .setStatus(resultProposal.getId(), ProposalStatus.UNPUBLISHED);
    assertEquals(ProposalStatus.UNPUBLISHED, unpublishedProposal.getStatus());
    val unpublishedResultSet = statement.executeQuery();

    assertTrue(unpublishedResultSet.next());
    assertEquals(ProposalStatus.UNPUBLISHED.toString(), unpublishedResultSet.getString("status"));
  }

  @Test
  public void findByAuthorId() {
    val authorId = UUID.randomUUID();
    for (int i = 0; i < 20; i++) {
      val proposal = createRandomProposal();
      val user = proposal.getAuthor();
      user.setId(authorId);
      proposal.setAuthor(user);
      proposalRepository.create(proposal);
    }

    val proposals = proposalRepository.findByAuthorId(authorId);

    assertEquals(20, proposals.size());
  }

  @Test
  public void findByAuthorIdAndStatus() {
    val authorId = UUID.randomUUID();
    for (int i = 0; i < 30; i++) {
      val proposal = createRandomProposal();
      val user = proposal.getAuthor();
      user.setId(authorId);
      proposal.setAuthor(user);
      proposalRepository.create(proposal);
      if (i % 3 == 0) {
        proposalRepository.setStatus(proposal.getId(), ProposalStatus.PUBLISHED);
      }
    }

    val publishedProposals = proposalRepository.findByAuthorId(authorId, ProposalStatus.PUBLISHED);
    assertEquals(10, publishedProposals.size());

    val unpublishedProposals = proposalRepository.findByAuthorId(authorId, ProposalStatus.UNPUBLISHED);
    assertEquals(20, unpublishedProposals.size());
  }

  @Test
  public void findAllPublished() {
    for (int i = 0; i < 30; i++) {
      val proposal = createRandomProposal();
      proposalRepository.create(proposal);
      if (i % 3 == 0) {
        proposalRepository.setStatus(proposal.getId(), ProposalStatus.PUBLISHED);
      }
    }

    val publishedProposals = proposalRepository.findAllPublished();
    assertEquals(10, publishedProposals.size());
  }

  private Proposal createRandomProposal() {
    val username = eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository
        .truncate(lorem.getEmail(), eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository.AUTHOR_SIZE);
    val title = eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository
        .truncate(lorem.getTitle(5, 30), eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository.TITLE_SIZE);
    val brief = eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository
        .truncate(lorem.getParagraphs(1, 1), eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository.BRIEF_SIZE);
    val source = eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository
        .truncate(lorem.getUrl(), eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository.SOURCE_SIZE);
    val motivation =
        eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository
            .truncate(lorem.getParagraphs(1, 5), eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository.MOTIVATION_SIZE);
    val measures =
        eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository
            .truncate(lorem.getParagraphs(1, 5), eu.trustdemocracy.proposals.gateways.repositories.mysql.MySqlProposalRepository.MEASURES_SIZE);

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
