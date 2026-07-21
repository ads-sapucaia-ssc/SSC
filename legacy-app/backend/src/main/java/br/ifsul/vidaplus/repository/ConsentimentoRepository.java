package br.ifsul.vidaplus.repository;

import br.ifsul.vidaplus.entity.Consentimento;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * F-04: CRUD completo (save/findById/findAll/delete/...) ja pronto via
 * JpaRepository - nenhuma linha de SQL escrita a mao. Estenda com metodos
 * derivados conforme a necessidade do grupo, ex.:
 *   findByPacienteIdAndRevogadoEmIsNull(Long pacienteId)
 */
public interface ConsentimentoRepository extends JpaRepository<Consentimento, Long> {

    List<Consentimento> findByPacienteId(Long pacienteId);
}
