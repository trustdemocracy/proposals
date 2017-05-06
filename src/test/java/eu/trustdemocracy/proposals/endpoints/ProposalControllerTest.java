package eu.trustdemocracy.proposals.endpoints;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.infrastructure.FakeInteractorFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import io.vertx.rxjava.core.Vertx;
import io.vertx.ext.unit.TestContext;
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

  private Lorem lorem = LoremIpsum.getInstance();

  @Before
  public void setUp(TestContext context) throws IOException, JoseException {
    TokenUtils.generateKeys();

    vertx = Vertx.vertx();
    client = WebClient.create(vertx);

    val socket = new ServerSocket(0);
    port = socket.getLocalPort();
    socket.close();

    val options = new DeploymentOptions().setConfig(new JsonObject().put("http.port", port));

    App.setInteractorFactory(new FakeInteractorFactory());
    vertx.deployVerticle(App.class.getName(), options, context.asyncAssertSuccess());
  }

  @After
  public void tearDown(TestContext context) {
    vertx.close(context.asyncAssertSuccess());
  }

  @Test
  public void createProposal(TestContext context) {
    val async = context.async();

    val username = lorem.getEmail();

    val inputProposal = new ProposalRequestDTO()
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), username))
        .setTitle(lorem.getTitle(5, 30))
        .setBrief(lorem.getParagraphs(1, 1))
        .setSource(lorem.getUrl())
        .setMotivation(lorem.getParagraphs(1, 5))
        .setMeasures(lorem.getParagraphs(1, 5));

    val single = client.post(port, HOST, "/proposal")
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      context.assertEquals(response.statusCode(), 201);
      context.assertTrue(response.headers().get("content-type").contains("application/json"));

      val responseProposal = Json.decodeValue(response.body().toString(), ProposalResponseDTO.class);
      context.assertEquals(username, responseProposal.getAuthorUsername());
      context.assertEquals(inputProposal.getTitle(), responseProposal.getTitle());
      context.assertEquals(inputProposal.getBrief(), responseProposal.getBrief());
      context.assertEquals(inputProposal.getSource(), responseProposal.getSource());
      context.assertEquals(inputProposal.getMotivation(), responseProposal.getMotivation());
      context.assertEquals(inputProposal.getMeasures(), responseProposal.getMeasures());
      context.assertNotNull(inputProposal.getId());

      async.complete();
    }, error -> {
      context.fail(error);
      async.complete();
    });
  }

}
