package br.ifsul.vidaplus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Habilita @CreatedDate (e @LastModifiedDate, se usado) nas entities JPA
 * novas (F-04/F-05/F-06/F-09). @CreatedBy exigiria um AuditorAware ligado
 * ao usuario autenticado - ainda nao existe (a autenticacao hoje e checada
 * na mao em cada controller, sem SecurityContext central) - fica para
 * se/quando o RBAC migrar para Spring Security de verdade.
 */
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {
}
