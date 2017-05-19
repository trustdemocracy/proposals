package eu.trustdemocracy.proposals.endpoints;

import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.CommentVoteRequestDTO;
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


  @Test
  public void createComment(TestContext context) {
    val async = context.async();
    val inputComment = FakeModelsFactory.getRandomComment();

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
      context.assertNotNull(responseComment.getAuthorUsername());
      context.assertNotNull(responseComment.getId());

      async.complete();
    }, error -> {
      context.fail(error);
      async.complete();
    });
  }

  @Test
  public void createAndDelete(TestContext context) {
    val async = context.async();
    val inputComment = FakeModelsFactory.getRandomComment();

    val single = client.post(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
        .rxSendJson(inputComment);

    single.subscribe(response -> {
      context.assertEquals(response.statusCode(), 201);
      client.get(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
          .rxSend()
          .subscribe(getResponse -> {
            context.assertEquals(getResponse.statusCode(), 200);

            val jsonArray = getResponse.bodyAsJsonArray();
            context.assertEquals(1, jsonArray.size());
            val comment = Json
                .decodeValue(jsonArray.getJsonObject(0).encode(), CommentResponseDTO.class);

            client.delete(port, HOST,
                "/proposals/" + inputComment.getProposalId() + "/comments/" + comment.getId())
                .rxSend()
                .subscribe(deleteResponse -> {
                  context.assertEquals(deleteResponse.statusCode(), 200);

                  val deleteComment = Json
                      .decodeValue(deleteResponse.body().toString(), CommentResponseDTO.class);
                  context.assertEquals(comment, deleteComment);

                  client.get(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
                      .rxSend()
                      .subscribe(emptyGetResponse -> {
                        context.assertEquals(emptyGetResponse.statusCode(), 200);

                        val emptyList = emptyGetResponse.bodyAsJsonArray();
                        context.assertEquals(0, emptyList.size());

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
    }, error -> {
      context.fail(error);
      async.complete();
    });
  }

  @Test
  public void voteComment(TestContext context) {
    val async = context.async(CommentVoteOption.values().length);



    for (val option : CommentVoteOption.values()) {
      val inputComment = FakeModelsFactory.getRandomComment();

      val single = client
          .post(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
          .rxSendJson(inputComment);

      single.subscribe(response -> {
        context.assertEquals(response.statusCode(), 201);
        val responseComment = Json
            .decodeValue(response.body().toString(), CommentResponseDTO.class);

        val vote = new CommentVoteRequestDTO()
            .setCommentId(responseComment.getId())
            .setVoterToken(TokenUtils.createToken(UUID.randomUUID(), "voter"))
            .setOption(option);

        client.post(port, HOST,
            "/proposals/" + responseComment.getProposalId() + "/comments/" + vote.getCommentId())
            .rxSendJson(vote)
            .subscribe(voteResponse -> {
              context.assertEquals(voteResponse.statusCode(), 200);

              client.get(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
                  .rxSend()
                  .subscribe(getResponse -> {
                    context.assertEquals(getResponse.statusCode(), 200);

                    val jsonArray = getResponse.bodyAsJsonArray();
                    val comment = Json
                        .decodeValue(jsonArray.getJsonObject(0).encode(), CommentResponseDTO.class);

                    for (val commentOption : CommentVoteOption.values()) {
                      val count = commentOption == option ? 1 : 0;
                      context.assertEquals(count, comment.getVotes().get(commentOption));
                    }

                    async.countDown();
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
  }

}
