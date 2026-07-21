package br.ifsul.vidaplus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * F-05 (LGPD arts. 37/46) - trilha de auditoria. NAO EXISTE no legado; o
 * Grupo 1 deve criar a tabela (modelo de referencia comentado em
 * db/schema.sql) e implementar o encadeamento de hash e a insercao
 * append-only (nunca UPDATE/DELETE nesta tabela).
 *
 * @CreatedDate preenche 'ts' sozinho (via JpaAuditingConfig) - isso e o que
 * o Spring Data resolve de graca. @CreatedBy (quem fez) exigiria um
 * AuditorAware ligado a um usuario autenticado de verdade - ainda nao
 * existe (a checagem hoje e manual em cada controller, sem SecurityContext
 * central) - fica para se/quando o RBAC migrar para Spring Security.
 *
 * user_id e opaco - NUNCA gravar CPF/nome aqui (V-17). prevHash/hash (o
 * encadeamento em si, a parte que prova integridade) e o cerne da PoC do
 * grupo - nao vem pronto.
 */
@Entity
@Table(name = "auditoria")
@EntityListeners(AuditingEntityListener.class)
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(nullable = false)
    private LocalDateTime ts;

    @Column(nullable = false)
    private String evento;

    @Column(name = "user_id")
    private Long userId;

    private String recurso;

    @Column(name = "prev_hash", nullable = false)
    private String prevHash;

    @Column(nullable = false)
    private String hash;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDateTime getTs() { return ts; }
    public void setTs(LocalDateTime ts) { this.ts = ts; }

    public String getEvento() { return evento; }
    public void setEvento(String evento) { this.evento = evento; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getRecurso() { return recurso; }
    public void setRecurso(String recurso) { this.recurso = recurso; }

    public String getPrevHash() { return prevHash; }
    public void setPrevHash(String prevHash) { this.prevHash = prevHash; }

    public String getHash() { return hash; }
    public void setHash(String hash) { this.hash = hash; }
}
