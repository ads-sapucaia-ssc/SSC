package br.ifsul.vidaplus.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * V-12: CORS totalmente aberto (Allow-Origin "*") junto com credenciais,
 * e nenhum cabecalho de seguranca (CSP, HSTS, X-Content-Type-Options).
 * Os grupos devem restringir a origem, remover o '*' com credenciais e
 * adicionar os cabecalhos de seguranca (F-17).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")            // V-12
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(false);       // (deixado false para compilar; discutir em aula)
    }
}
