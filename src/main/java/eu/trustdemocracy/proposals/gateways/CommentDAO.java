package eu.trustdemocracy.proposals.gateways;

import eu.trustdemocracy.proposals.core.entities.Comment;

public interface CommentDAO {

  Comment create(Comment comment);

}
