package eu.trustdemocracy.proposals.infrastructure;

import eu.trustdemocracy.proposals.core.interactors.comment.CreateComment;
import eu.trustdemocracy.proposals.core.interactors.comment.DeleteComment;
import eu.trustdemocracy.proposals.core.interactors.comment.GetComments;
import eu.trustdemocracy.proposals.core.interactors.comment.VoteComment;
import eu.trustdemocracy.proposals.core.interactors.proposal.CreateProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.DeleteProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.GetProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.PublishProposal;
import eu.trustdemocracy.proposals.core.interactors.proposal.UnpublishProposal;

public interface InteractorFactory {

  CreateProposal getCreateProposal();

  DeleteProposal getDeleteProposal();

  GetProposal getGetProposal();

  PublishProposal getPublishProposal();

  UnpublishProposal getUnpublishProposal();


  CreateComment getCreateComment();

  DeleteComment getDeleteComment();

  GetComments getGetComments();

  VoteComment getVoteComment();
}
