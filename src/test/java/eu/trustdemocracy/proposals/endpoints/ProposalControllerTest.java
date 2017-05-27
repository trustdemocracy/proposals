package eu.trustdemocracy.proposals.endpoints;

import eu.trustdemocracy.proposals.core.entities.ProposalStatus;
import eu.trustdemocracy.proposals.core.models.FakeModelsFactory;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import io.vertx.core.json.Json;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.unit.TestContext;
import io.vertx.ext.unit.junit.VertxUnitRunner;
import lombok.val;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(VertxUnitRunner.class)
public class ProposalControllerTest extends ControllerTest {

  @Test
  public void createProposal(TestContext context) {
    val async = context.async();
    val inputProposal = FakeModelsFactory.getRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      context.assertEquals(response.statusCode(), 201);
      context.assertTrue(response.headers().get("content-type").contains("application/json"));

      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);
      context.assertNotNull(responseProposal.getAuthorUsername());
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
    val inputProposal = FakeModelsFactory.getRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);
      client.get(port, HOST, "/proposals/" + responseProposal.getId())
          .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
          .rxSend()

          .subscribe(getResponse -> {
            context.assertEquals(getResponse.statusCode(), 200);
            context
                .assertTrue(getResponse.headers().get("content-type").contains("application/json"));
            val foundProposal = Json
                .decodeValue(getResponse.body().toString(), ProposalResponseDTO.class);

            context.assertEquals(responseProposal.getId(), foundProposal.getId());
            context.assertNotNull(foundProposal.getAuthorUsername());
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
    val inputProposal = FakeModelsFactory.getRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);
      client.get(port, HOST, "/proposals/" + responseProposal.getId() + "/publish")
          .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
          .rxSend()
          .subscribe(publishResponse -> {
            context.assertEquals(publishResponse.statusCode(), 200);

            client.get(port, HOST, "/proposals/" + responseProposal.getId())
                .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
                .rxSend()

                .subscribe(getResponse -> {
                  val updatedProposal = Json
                      .decodeValue(getResponse.body().toString(), ProposalResponseDTO.class);

                  context.assertEquals(responseProposal.getId(), updatedProposal.getId());
                  context.assertNotNull(updatedProposal.getAuthorUsername());
                  context.assertEquals(inputProposal.getTitle(), updatedProposal.getTitle());
                  context.assertEquals(inputProposal.getBrief(), updatedProposal.getBrief());
                  context.assertEquals(inputProposal.getSource(), updatedProposal.getSource());
                  context
                      .assertEquals(inputProposal.getMotivation(), updatedProposal.getMotivation());
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

  @Test
  public void createPublishAndUnpublishProposal(TestContext context) {
    val async = context.async();
    val inputProposal = FakeModelsFactory.getRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);
      client.get(port, HOST, "/proposals/" + responseProposal.getId() + "/publish")
          .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
          .rxSend()
          .subscribe(publishResponse -> {
            context.assertEquals(publishResponse.statusCode(), 200);

            client.get(port, HOST, "/proposals/" + responseProposal.getId() + "/unpublish")
                .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
                .rxSend()
                .subscribe(unpublishResponse -> {
                  context.assertEquals(unpublishResponse.statusCode(), 200);
                  client.get(port, HOST, "/proposals/" + responseProposal.getId())
                      .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
                      .rxSend()

                      .subscribe(getResponse -> {
                        val updatedProposal = Json
                            .decodeValue(getResponse.body().toString(), ProposalResponseDTO.class);

                        context.assertEquals(responseProposal.getId(), updatedProposal.getId());
                        context.assertNotNull(updatedProposal.getAuthorUsername());
                        context.assertEquals(inputProposal.getTitle(), updatedProposal.getTitle());
                        context.assertEquals(inputProposal.getBrief(), updatedProposal.getBrief());
                        context
                            .assertEquals(inputProposal.getSource(), updatedProposal.getSource());
                        context.assertEquals(inputProposal.getMotivation(),
                            updatedProposal.getMotivation());
                        context.assertEquals(inputProposal.getMeasures(),
                            updatedProposal.getMeasures());
                        context
                            .assertEquals(ProposalStatus.UNPUBLISHED, updatedProposal.getStatus());

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
  public void createAndDeleteProposal(TestContext context) {
    val async = context.async();
    val inputProposal = FakeModelsFactory.getRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);
      client.delete(port, HOST, "/proposals/" + responseProposal.getId())
          .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
          .rxSend()
          .subscribe(deleteResponse -> {
            context.assertEquals(deleteResponse.statusCode(), 200);

            client.get(port, HOST, "/proposals/" + responseProposal.getId())
                .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
                .rxSend()

                .subscribe(getResponse -> {
                  context.assertEquals(getResponse.statusCode(), 404);
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


  @Test
  public void updateResult(TestContext context) {
    val async = context.async();
    val inputProposal = FakeModelsFactory.getRandomProposal();

    val single = client.post(port, HOST, "/proposals")
        .putHeader("Authorization", "Bearer " + inputProposal.getAuthorToken())
        .rxSendJson(inputProposal);

    single.subscribe(response -> {
      val responseProposal = Json
          .decodeValue(response.body().toString(), ProposalResponseDTO.class);

      val results = new JsonObject()
          .put("FAVOUR", 0.4)
          .put("AGAINST", 0.3);

      val request = new JsonObject()
          .put(responseProposal.getId().toString(), new JsonObject().put("results", results));

      client.post(port, HOST, "/proposals/results/")
          .rxSendJson(request)
          .subscribe(deleteResponse -> {
            context.assertEquals(deleteResponse.statusCode(), 200);

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

}
