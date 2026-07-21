package br.ifsul.vidaplus.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * F-04 (LGPD arts. 8/9) - gestao de consentimento. NAO EXISTE no legado; o
 * Grupo 3 deve criar a tabela (modelo de referencia comentado em
 * db/schema.sql) e evoluir esta entidade conforme o desenho do grupo.
 *
 * Isto e so a BASE (entity + repository prontos, ver ConsentimentoRepository) -
 * o CRUD ja funciona sem SQL nenhum escrito a mao. O que falta e trabalho de
 * design/analise do grupo: por finalidade, versionado, revogavel.
 */
@Entity
@Table(name = "consentimento")
public class Consentimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "paciente_id", nullable = false)
    private Long pacienteId;

    @Column(nullable = false)
    private String finalidade;

    @Column(name = "base_legal", nullable = false)
    private String baseLegal;

    @Column(name = "termo_versao", nullable = false)
    private String termoVersao;

    @Column(name = "concedido_em", nullable = false)
    private LocalDateTime concedidoEm;

    @Column(name = "revogado_em")
    private LocalDateTime revogadoEm;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getPacienteId() { return pacienteId; }
    public void setPacienteId(Long pacienteId) { this.pacienteId = pacienteId; }

    public String getFinalidade() { return finalidade; }
    public void setFinalidade(String finalidade) { this.finalidade = finalidade; }

    public String getBaseLegal() { return baseLegal; }
    public void setBaseLegal(String baseLegal) { this.baseLegal = baseLegal; }

    public String getTermoVersao() { return termoVersao; }
    public void setTermoVersao(String termoVersao) { this.termoVersao = termoVersao; }

    public LocalDateTime getConcedidoEm() { return concedidoEm; }
    public void setConcedidoEm(LocalDateTime concedidoEm) { this.concedidoEm = concedidoEm; }

    public LocalDateTime getRevogadoEm() { return revogadoEm; }
    public void setRevogadoEm(LocalDateTime revogadoEm) { this.revogadoEm = revogadoEm; }
}
