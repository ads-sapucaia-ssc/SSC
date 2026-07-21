import axios from 'axios';
import { API_BASE } from '../config/api';

/**
 * Servico de autenticacao (LEGADO).
 * V-13: usa HTTP puro (API_BASE).
 * V-07/V-10 (variante front): guarda o JWT eterno em localStorage (acessivel por XSS).
 *   Os grupos devem discutir cookie HttpOnly + SameSite e sessao de curta duracao.
 */
export function login(email, senha) {
  // axios sobre HTTP puro (V-13)
  return axios.post(`${API_BASE}/auth/login`, { email, senha }).then((r) => r.data);
}

export function solicitarReset(email) {
  return axios.post(`${API_BASE}/auth/reset`, { email }).then((r) => r.data);
}

/**
 * Guarda a sessao inteira no cliente (token + dados do usuario retornados pelo login).
 * V-07/V-10: tudo em localStorage - roubavel por XSS (ver Prontuario.jsx / V-10).
 */
export function salvarSessao(resp) {
  localStorage.setItem('access_token', resp.access_token);
  localStorage.setItem('papel', resp.papel || '');
  localStorage.setItem('paciente_id', resp.paciente_id ?? '');
  localStorage.setItem('nome', resp.nome || '');
}

export function getToken() {
  return localStorage.getItem('access_token');
}

export function getUsuario() {
  const token = getToken();
  if (!token) return null;
  return {
    papel: localStorage.getItem('papel') || '',
    pacienteId: localStorage.getItem('paciente_id') || '',
    nome: localStorage.getItem('nome') || '',
  };
}

export function logout() {
  // V-07: "logout" SOMENTE no cliente - limpa o storage local, mas o token
  // continua valido no servidor (sem expiracao nem invalidacao - ver JwtUtil).
  localStorage.removeItem('access_token');
  localStorage.removeItem('papel');
  localStorage.removeItem('paciente_id');
  localStorage.removeItem('nome');
}
