package eu.trustdemocracy.proposals.infrastructure;

import eu.trustdemocracy.proposals.core.interactors.Interactor;
import eu.trustdemocracy.proposals.core.models.request.ProposalRequestDTO;
import eu.trustdemocracy.proposals.core.models.response.ProposalResponseDTO;
import eu.trustdemocracy.proposals.gateways.ProposalDAO;
import lombok.val;

public class DefaultInteractorFactory implements InteractorFactory {

  private static DefaultInteractorFactory instance;

  private DefaultInteractorFactory() {
  }

  public static DefaultInteractorFactory getInstance() {
    if (instance == null) {
      instance = new DefaultInteractorFactory();
    }
    return instance;
  }

  @Override
  public Interactor<ProposalRequestDTO, ProposalResponseDTO> createProposalInteractor(
      Class<? extends Interactor<ProposalRequestDTO, ProposalResponseDTO>> concreteClass) {
    try {
      val constructor = concreteClass.getConstructor(ProposalDAO.class);
      val proposalDAO = DAOFactory.getProposalDAO();
      return constructor.newInstance(proposalDAO);
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }
}
