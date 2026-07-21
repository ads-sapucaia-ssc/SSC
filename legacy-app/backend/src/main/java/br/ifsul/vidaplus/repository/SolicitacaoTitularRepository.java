package br.ifsul.vidaplus.repository;

import br.ifsul.vidaplus.entity.SolicitacaoTitular;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * F-06/F-09: CRUD basico pronto via JpaRepository.
 */
public interface SolicitacaoTitularRepository extends JpaRepository<SolicitacaoTitular, Long> {

    List<SolicitacaoTitular> findByPacienteId(Long pacienteId);

    List<SolicitacaoTitular> findByStatus(String status);
}
