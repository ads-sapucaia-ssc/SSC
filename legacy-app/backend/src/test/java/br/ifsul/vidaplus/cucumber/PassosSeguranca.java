package br.ifsul.vidaplus.cucumber;

import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Então;
import io.cucumber.java.pt.Quando;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * GLUE (passos reutilizaveis) — a "execucao" do requisito.
 * Escrito UMA vez; serve a qualquer .feature. Um grupo que escreva um
 * requisito novo em Gherkin normalmente SO reusa estes passos; escreve um
 * passo novo aqui apenas se precisar de uma acao/verificacao inedita.
 */
public class PassosSeguranca {

    @Autowired
    private MockMvc mvc;

    private String token;
    private MvcResult resposta;

    @Dado("que estou autenticado como {string} com senha {string}")
    public void autenticado(String email, String senha) throws Exception {
        String r = mvc.perform(post("/api/auth/login").contentType("application/json")
                        .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}"))
                .andReturn().getResponse().getContentAsString();
        token = r.replaceAll(".*\"access_token\":\"([^\"]+)\".*", "$1");
    }

    @Quando("faço GET em {string}")
    public void faco_get(String url) throws Exception {
        resposta = mvc.perform(get(url).header("Authorization", "Bearer " + token)).andReturn();
    }

    @Quando("faço POST em {string}")
    public void faco_post(String url) throws Exception {
        resposta = mvc.perform(post(url).header("Authorization", "Bearer " + token)).andReturn();
    }

    @Quando("tento logar com email {string} e senha {string}")
    public void tento_logar(String email, String senha) throws Exception {
        resposta = mvc.perform(post("/api/auth/login").contentType("application/json")
                .content("{\"email\":\"" + email + "\",\"senha\":\"" + senha + "\"}")).andReturn();
    }

    @Então("o status HTTP é {int}")
    public void status_http(int esperado) {
        assertEquals(esperado, resposta.getResponse().getStatus());
    }

    @E("a resposta não contém {string}")
    public void nao_contem(String txt) throws Exception {
        assertFalse(resposta.getResponse().getContentAsString().contains(txt),
                "A resposta NAO deveria conter: " + txt);
    }

    @E("a resposta contém {string}")
    public void contem(String txt) throws Exception {
        assertTrue(resposta.getResponse().getContentAsString().contains(txt),
                "A resposta deveria conter: " + txt);
    }
}
