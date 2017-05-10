package eu.trustdemocracy.proposals.endpoints;

import com.thedeanda.lorem.Lorem;
import com.thedeanda.lorem.LoremIpsum;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.request.CommentRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.UUID;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CommentControllerTest extends ControllerTest {

  private Lorem lorem = LoremIpsum.getInstance();
  private String username = lorem.getEmail();

  @Test
  public void createComment(TestContext context) {
    val async = context.async();
    val inputComment = createRandomComment();

    val single = client.post(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
        .rxSendJson(inputComment);

    val currentTime = System.currentTimeMillis();

    single.subscribe(response -> {
      context.assertEquals(response.statusCode(), 201);
      context.assertTrue(response.headers().get("content-type").contains("application/json"));

      val responseComment = Json
          .decodeValue(response.body().toString(), CommentResponseDTO.class);
      context.assertEquals(inputComment.getProposalId(), responseComment.getProposalId());
      context.assertEquals(inputComment.getRootCommentId(), responseComment.getRootCommentId());
      context.assertTrue(currentTime < responseComment.getTimestamp());
      context.assertEquals(username, responseComment.getAuthorUsername());
      context.assertNotNull(responseComment.getId());

      async.complete();
    }, error -> {
      context.fail(error);
      async.complete();
    });
  }

  private CommentRequestDTO createRandomComment() {
    return new CommentRequestDTO()
        .setAuthorToken(TokenUtils.createToken(UUID.randomUUID(), username))
        .setProposalId(UUID.randomUUID())
        .setRootCommentId(new UUID(0L, 0L))
        .setContent(lorem.getParagraphs(1, 2));
  }

}
