package br.ifsul.vidaplus.repository;

import br.ifsul.vidaplus.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * F-05: CRUD basico pronto via JpaRepository. ATENCAO: a trilha deve ser
 * append-only - o grupo NAO deve chamar delete()/deleteById() em producao;
 * considere expor so save()/findAll() na camada de servico, ou um
 * repositorio customizado que nao herde os metodos de escrita/remocao.
 */
public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {

    List<Auditoria> findByRecursoOrderByTsDesc(String recurso);
}
