package eu.trustdemocracy.proposals.endpoints;

import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.infrastructure.FakeInteractorFactory;
import eu.trustdemocracy.proposals.infrastructure.InteractorFactory;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.rxjava.core.Vertx;
import io.vertx.rxjava.ext.web.client.WebClient;
import java.io.IOException;
import java.net.ServerSocket;
import lombok.val;
import org.jose4j.lang.JoseException;
import org.junit.After;
import org.junit.Before;

public class ControllerTest {

  protected static final String HOST = "localhost";

  protected Vertx vertx;
  protected Integer port;
  protected WebClient client;
  protected InteractorFactory interactorFactory;

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

}
