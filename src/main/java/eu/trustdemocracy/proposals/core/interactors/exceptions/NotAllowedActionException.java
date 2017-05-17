package eu.trustdemocracy.proposals.core.interactors.exceptions;

public class NotAllowedActionException extends RuntimeException {

  public NotAllowedActionException(String message) {
    super(message);
  }

}
