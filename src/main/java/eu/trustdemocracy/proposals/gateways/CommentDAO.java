package eu.trustdemocracy.proposals.gateways;

import eu.trustdemocracy.proposals.core.entities.Comment;
import java.util.UUID;

public interface CommentDAO {

  Comment create(Comment comment);

  Comment deleteById(UUID id);
}
