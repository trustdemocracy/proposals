package eu.trustdemocracy.proposals.endpoints;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.mysql.MySqlProposalDAO;
import eu.trustdemocracy.proposals.infrastructure.FakeInteractorFactory;
import eu.trustdemocracy.proposals.infrastructure.InteractorFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import java.net.ServerSocket;
import java.util.UUID;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ProposalControllerTest {

  private static final String HOST = "localhost";

  private Vertx vertx;
  private Integer port;
  private WebClient client;
  private InteractorFactory interactorFactory;

  private Lorem lorem = LoremIpsum.getInstance();
  private String currentUsername;

  @Before
  public void setUp(TestContext context) throws IOException, JoseException {
    TokenUtils.generateKeys();

    vertx = Vertx.vertx();
    client = WebClient.create(vertx);

    val socket = new ServerSocket(0);
    port = socket.getLocalPort();
    socket.close();

    val options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));

    interactorFactory = new FakeInteractorFactory();

    App.setInteractorFactory(interactorFactory);
    vertx.deployVerticle(App.class.getName(), options, context.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void createProposal(TestContext context) {
    val async = context.async();
    val inputProposal = createRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      context.assertEquals(response.statusCode(), 201);
      context.assertTrue(response.headers().get("content-type").contains("application/json"));

      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);
      context.assertEquals(currentUsername, responseProposal.getAuthorUsername());
      context.assertEquals(inputProposal.getTitle(), responseProposal.getTitle());
      context.assertEquals(inputProposal.getBrief(), responseProposal.getBrief());
      context.assertEquals(inputProposal.getSource(), responseProposal.getSource());
      context.assertEquals(inputProposal.getMotivation(), responseProposal.getMotivation());
      context.assertEquals(inputProposal.getMeasures(), responseProposal.getMeasures());
      context.assertEquals(ProposalStatus.UNPUBLISHED, responseProposal.getStatus());
      context.assertNotNull(responseProposal.getId());

      async.complete();
    }, error -> {
      context.fail(error);
      async.complete();
    });
  }

  @Test
  public void createAndFindProposal(TestContext context) {
    val async = context.async();
    val inputProposal = createRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);
      client.get(port, HOST, "/proposals/" + responseProposal.getId())
          .rxSend()

          .subscribe(getResponse -> {
            context.assertEquals(getResponse.statusCode(), 200);
            context
                .assertTrue(getResponse.headers().get("content-type").contains("application/json"));
            val foundProposal = Json
                .decodeValue(getResponse.body().toString(), ProposalResponseDTO.class);

            context.assertEquals(responseProposal.getId(), foundProposal.getId());
            context.assertEquals(currentUsername, foundProposal.getAuthorUsername());
            context.assertEquals(inputProposal.getTitle(), foundProposal.getTitle());
            context.assertEquals(inputProposal.getBrief(), foundProposal.getBrief());
            context.assertEquals(inputProposal.getSource(), foundProposal.getSource());
            context.assertEquals(inputProposal.getMotivation(), foundProposal.getMotivation());
            context.assertEquals(inputProposal.getMeasures(), foundProposal.getMeasures());
            context.assertEquals(ProposalStatus.UNPUBLISHED, foundProposal.getStatus());

            async.complete();
          }, error -> {
            context.fail(error);
            async.complete();
          });

    }, error -> {
      context.fail(error);
      async.complete();
    });
  }

  @Test
  public void createAndPublishProposal(TestContext context) {
    val async = context.async();
    val inputProposal = createRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);
      client.get(port, HOST, "/proposals/" + responseProposal.getId() + "/publish")
          .rxSend()
          .subscribe(publishResponse -> {
            context.assertEquals(publishResponse.statusCode(), 200);


            client.get(port, HOST, "/proposals/" + responseProposal.getId())
                .rxSend()

                .subscribe(getResponse -> {
            val updatedProposal = Json
                .decodeValue(getResponse.body().toString(), ProposalResponseDTO.class);

            context.assertEquals(responseProposal.getId(), updatedProposal.getId());
            context.assertEquals(currentUsername, updatedProposal.getAuthorUsername());
            context.assertEquals(inputProposal.getTitle(), updatedProposal.getTitle());
            context.assertEquals(inputProposal.getBrief(), updatedProposal.getBrief());
            context.assertEquals(inputProposal.getSource(), updatedProposal.getSource());
            context.assertEquals(inputProposal.getMotivation(), updatedProposal.getMotivation());
            context.assertEquals(inputProposal.getMeasures(), updatedProposal.getMeasures());
            context.assertEquals(ProposalStatus.PUBLISHED, updatedProposal.getStatus());

            async.complete();
          }, error -> {
            context.fail(error);
            async.complete();
          });
        }, error -> {
          context.fail(error);
          async.complete();
        });

    }, error -> {
      context.fail(error);
      async.complete();
    });
  }

  private ProposalRequestDTO createRandomProposal() {
    currentUsername = truncate(lorem.getEmail(), MySqlProposalDAO.AUTHOR_SIZE);
    val title = truncate(lorem.getTitle(5, 30), MySqlProposalDAO.TITLE_SIZE);
    val brief = truncate(lorem.getParagraphs(1, 1), MySqlProposalDAO.BRIEF_SIZE);
    val source = truncate(lorem.getUrl(), MySqlProposalDAO.SOURCE_SIZE);
    val motivation = truncate(lorem.getParagraphs(1, 5), MySqlProposalDAO.MOTIVATION_SIZE);
    val measures = truncate(lorem.getParagraphs(1, 5), MySqlProposalDAO.MEASURES_SIZE);

    val user = TokenUtils.createToken(UUID.randomUUID(), currentUsername);
    return new ProposalRequestDTO()
        .setAuthorToken(user)
        .setTitle(title)
        .setBrief(brief)
        .setSource(source)
        .setMotivation(motivation)
        .setMeasures(measures);
  }

  private static String truncate(String string, int limit) {
    return string.length() > limit ? string.substring(0, limit) : string;
  }

}
