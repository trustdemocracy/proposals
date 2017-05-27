package eu.trustdemocracy.proposals.gateways.out;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.Proposal;

public class FakeEventsGateway implements EventsGateway {

  @Override
  public void createPublicationEvent(Proposal proposal) {
    
  }

  @Override
  public void createCommentEvent(Comment comment) {

  }
}
