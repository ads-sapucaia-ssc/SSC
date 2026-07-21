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
 * F-06/F-09 (LGPD art. 18) - solicitacoes do titular (portabilidade e
 * eliminacao). NAO EXISTE no legado; os Grupos 2 (F-09) e 4/5 (F-06) devem
 * criar a tabela e evoluir esta entidade conforme o desenho deles.
 *
 * Ao contrario de consentimento/auditoria, NAO ha modelo de referencia
 * previo comentado em db/schema.sql para esta tabela - este e um primeiro
 * rascunho (tipo/status minimos), nao uma solucao pronta. O grupo decide o
 * fluxo real (ex.: quem aprova, prazo legal de resposta, etc.).
 */
@Entity
@Table(name = "solicitacao_titular")
@EntityListeners(AuditingEntityListener.class)
public class SolicitacaoTitular {

    /** EXPORTACAO (F-09, portabilidade) ou ELIMINACAO (F-06). */
    public static final String TIPO_EXPORTACAO = "EXPORTACAO";
    public static final String TIPO_ELIMINACAO = "ELIMINACAO";

    /** PENDENTE, CONCLUIDA ou NEGADA. */
    public static final String STATUS_PENDENTE = "PENDENTE";
    public static final String STATUS_CONCLUIDA = "CONCLUIDA";
    public static final String STATUS_NEGADA = "NEGADA";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(nullable = false)
    private String tipo;

    @Column(nullable = false)
    private String status;

    @CreatedDate
    @Column(name = "solicitado_em", nullable = false)
    private LocalDateTime solicitadoEm;

    @Column(name = "concluido_em")
    private LocalDateTime concluidoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = tipo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public LocalDateTime getSolicitadoEm() { return solicitadoEm; }
    public void setSolicitadoEm(LocalDateTime solicitadoEm) { this.solicitadoEm = solicitadoEm; }

    public LocalDateTime getConcluidoEm() { return concluidoEm; }
    public void setConcluidoEm(LocalDateTime concluidoEm) { this.concluidoEm = concluidoEm; }
}
