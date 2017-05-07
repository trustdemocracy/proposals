package eu.trustdemocracy.proposals.core.interactors;

public interface Interactor<RequestDTO, ResponseDTO> {

  ResponseDTO execute(RequestDTO requestDTO);

}
