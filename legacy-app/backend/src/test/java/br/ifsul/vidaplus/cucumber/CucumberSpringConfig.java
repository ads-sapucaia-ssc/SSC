package br.ifsul.vidaplus.cucumber;

import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * INFRAESTRUTURA (fornecida pelo professor, uma vez).
 * Liga o Cucumber ao contexto Spring Boot + MockMvc — os passos (glue)
 * podem @Autowired o MockMvc a partir daqui.
 */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
public class CucumberSpringConfig {
}
