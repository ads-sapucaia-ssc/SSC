package br.ifsul.vidaplus.security;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * TESTES DE ACEITE DE SEGURANCA (fornecidos pelo professor).
 *
 * Definem o "pronto" das falhas P0 mais criticas. Rodam no gate do CI via
 * `mvn test -Dgroups=security` (precisa do MySQL de servico - ver o pipeline).
 *
 *  >>> No codigo LEGADO estes testes FALHAM de proposito (gate VERMELHO). <<<
 *  Quando o grupo corrigir de verdade, eles passam (gate VERDE).
 *
 * Voce (grupo) deve EXPANDIR a cobertura com seus proprios testes na Fase 5 -
 * estes sao apenas o alvo minimo.
 *
 * Dados-semente: Ana (paciente id 1) e dona do exame 101; o exame 102 e do Bruno.
 */
@SpringBootTest
@AutoConfigureMockMvc
@Tag("security")
class SegurancaAceiteTest {

    @Autowired
    private MockMvc mvc;

    private String login(String email, String senha) throws Exception {
        String resp = mvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andReturn().getResponse().getContentAsString();
        return resp.replaceAll(".*\"access_token\":\"([^\"]+)\".*", "$1");
    }

    /** F-02 / V-03 (IDOR): Ana NAO pode acessar o exame do Bruno. */
    @Test
    void idor_paciente_nao_acessa_exame_de_outro_deve_403() throws Exception {
        String t = login("ana.paciente@email.com", "Paciente123");
        mvc.perform(get("/api/exames/102").header("Authorization", "Bearer " + t))
                .andExpect(status().isForbidden())
                .andExpect(content().string(not(containsString("Glicemia")))); // sem vazar metadados
    }

    /** F-03/F-07 / V-04 (RBAC): paciente NAO pode publicar laudo. */
    @Test
    void rbac_paciente_nao_publica_laudo_deve_403() throws Exception {
        String t = login("ana.paciente@email.com", "Paciente123");
        mvc.perform(post("/api/exames/104/publicar").header("Authorization", "Bearer " + t))
                .andExpect(status().isForbidden());
    }

    /** F-01 / V-01 (SQLi): payload de injecao no login NAO deve autenticar. */
    @Test
    void sqli_no_login_nao_autentica() throws Exception {
        String payload = "{\"email\":\"admin@vidaplus.local' -- \",\"senha\":\"qualquer\"}";
        mvc.perform(post("/api/auth/login").contentType("application/json").content(payload))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(not(containsString("access_token"))));
    }

    /** Sanidade: login legitimo continua funcionando apos as correcoes. */
    @Test
    void login_valido_funciona() throws Exception {
        mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"ana.paciente@email.com\",\"senha\":\"Paciente123\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("access_token")));
    }
}
