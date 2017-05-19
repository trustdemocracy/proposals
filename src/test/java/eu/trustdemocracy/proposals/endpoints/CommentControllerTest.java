package eu.trustdemocracy.proposals.endpoints;

import eu.trustdemocracy.proposals.core.entities.CommentVoteOption;
import eu.trustdemocracy.proposals.core.interactors.util.TokenUtils;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.request.CommentVoteRequestDTO;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.CommentResponseDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import io.vertx.core.json.Json;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import java.util.UUID;
import lombok.val;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class CommentControllerTest extends ControllerTest {

  private ProposalResponseDTO existingProposal;

  @Before
  public void setUp() {
    val createProposal = interactorFactory.getCreateProposal();
    val publishProposal = interactorFactory.getPublishProposal();

    val randomProposal = FakeModelsFactory.getRandomProposal();

    val createdProposal = createProposal.execute(randomProposal);
    existingProposal = publishProposal.execute(new ProposalRequestDTO()
        .setId(createdProposal.getId())
        .setAuthorToken(randomProposal.getAuthorToken()));
  }

  @Test
  public void createComment(TestContext context) {
    val async = context.async();
    val inputComment = FakeModelsFactory.getRandomComment()
        .setProposalId(existingProposal.getId());

    val single = client.post(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
        .putHeader("Authorization", "Bearer " + inputComment.getAuthorToken())
        .rxSendJson(inputComment);

    single.subscribe(response -> {
      context.assertEquals(response.statusCode(), 201);
      context.assertTrue(response.headers().get("content-type").contains("application/json"));

      val responseComment = Json
          .decodeValue(response.body().toString(), CommentResponseDTO.class);
      context.assertEquals(inputComment.getProposalId(), responseComment.getProposalId());
      context.assertEquals(inputComment.getRootCommentId(), responseComment.getRootCommentId());
      context.assertNotNull(responseComment.getTimestamp());
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
    val inputComment = FakeModelsFactory.getRandomComment()
        .setProposalId(existingProposal.getId());

    val single = client.post(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
        .putHeader("Authorization", "Bearer " + inputComment.getAuthorToken())
        .rxSendJson(inputComment);

    single.subscribe(response -> {
      context.assertEquals(response.statusCode(), 201);
      client.get(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
          .putHeader("Authorization", "Bearer " + inputComment.getAuthorToken())
          .rxSend()
          .subscribe(getResponse -> {
            context.assertEquals(getResponse.statusCode(), 200);

            val jsonArray = getResponse.bodyAsJsonArray();
            context.assertEquals(1, jsonArray.size());
            val comment = Json
                .decodeValue(jsonArray.getJsonObject(0).encode(), CommentResponseDTO.class);

            client.delete(port, HOST,
                "/proposals/" + inputComment.getProposalId() + "/comments/" + comment.getId())
                .putHeader("Authorization", "Bearer " + inputComment.getAuthorToken())
                .rxSend()
                .subscribe(deleteResponse -> {
                  context.assertEquals(deleteResponse.statusCode(), 200);

                  val deleteComment = Json
                      .decodeValue(deleteResponse.body().toString(), CommentResponseDTO.class);
                  context.assertEquals(comment, deleteComment);

                  client.get(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
                      .putHeader("Authorization", "Bearer " + inputComment.getAuthorToken())
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
    val async = context.async();

    val option = CommentVoteOption.UP;
    val inputComment = FakeModelsFactory.getRandomComment()
        .setProposalId(existingProposal.getId());

    val single = client
        .post(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
        .putHeader("Authorization", "Bearer " + inputComment.getAuthorToken())
        .rxSendJson(inputComment);

    single.subscribe(response -> {
      context.assertEquals(response.statusCode(), 201);
      val responseComment = Json
          .decodeValue(response.body().toString(), CommentResponseDTO.class);

      val randomVoterToken = TokenUtils.createToken(UUID.randomUUID(), "voter");
      val vote = new CommentVoteRequestDTO()
          .setCommentId(responseComment.getId())
          .setVoterToken(randomVoterToken)
          .setOption(option);

      client.post(port, HOST,
          "/proposals/" + responseComment.getProposalId() + "/comments/" + vote.getCommentId()
              + "/vote")
          .putHeader("Authorization", "Bearer " + randomVoterToken)
          .rxSendJson(vote)
          .subscribe(voteResponse -> {
            context.assertEquals(voteResponse.statusCode(), 200);

            client.get(port, HOST, "/proposals/" + inputComment.getProposalId() + "/comments")
                .putHeader("Authorization", "Bearer " + inputComment.getAuthorToken())
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


