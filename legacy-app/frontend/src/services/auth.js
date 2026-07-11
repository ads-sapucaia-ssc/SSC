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

export function salvarToken(token) {
  // V-07/V-10: token em localStorage e roubavel via XSS (ver V-10 em Prontuario.jsx)
  localStorage.setItem('access_token', token);
}

export function getToken() {
  return localStorage.getItem('access_token');
}

export function logout() {
  localStorage.removeItem('access_token');
}
