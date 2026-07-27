package br.ifsul.vidaplus.cucumber;

import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

import static io.cucumber.junit.platform.engine.Constants.FILTER_TAGS_PROPERTY_NAME;
import static io.cucumber.junit.platform.engine.Constants.GLUE_PROPERTY_NAME;

/**
 * INFRAESTRUTURA (fornecida pelo professor, uma vez).
 *
 * Descobre os .feature em resources/.../features e casa com o glue no
 * pacote 'cucumber'. Roda no `mvn test` (Surefire).
 *
 * FILTRO: só executa cenários que NÃO sejam @analise. Assim, os requisitos
 * apenas ANALISADOS (spec-only, @analise) nunca rodam — podem inclusive ter
 * passos sem glue — sem quebrar o CI. Os IMPLEMENTADOS (@security) viram
 * tags do JUnit Platform e entram no gate `mvn test -Dgroups=security`.
 */
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("br/ifsul/vidaplus/features")
@ConfigurationParameter(key = GLUE_PROPERTY_NAME, value = "br.ifsul.vidaplus.cucumber")
@ConfigurationParameter(key = FILTER_TAGS_PROPERTY_NAME, value = "not @analise")
public class RunCucumberTest {
}
