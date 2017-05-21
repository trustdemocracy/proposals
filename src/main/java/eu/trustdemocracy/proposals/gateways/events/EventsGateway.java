package eu.trustdemocracy.proposals.gateways.events;

import eu.trustdemocracy.proposals.core.entities.Comment;
import eu.trustdemocracy.proposals.core.entities.Proposal;

public interface EventsGateway {

  void createPublicationEvent(Proposal proposal);

  void createCommentEvent(Comment comment);
}
