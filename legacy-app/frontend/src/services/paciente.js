import axios from 'axios';
import { API_BASE } from '../config/api';
import { getToken } from './auth';

/**
 * Acesso a pacientes pelo front (LEGADO).
 * V-01 (F-13): busca por termo concatenado no backend -> SQL Injection.
 * V-14: a resposta traz campos sobrecoletados (religiao, renda_mensal...).
 */
function authHeader() {
  return { headers: { Authorization: `Bearer ${getToken()}` } };
}

export function buscar(termo) {
  return axios.get(`${API_BASE}/pacientes/busca`, { params: { q: termo }, ...authHeader() })
    .then((r) => r.data);
}

export function porId(id) {
  // (sem checagem de papel/vinculo no backend)
  return axios.get(`${API_BASE}/pacientes/${id}`, authHeader()).then((r) => r.data);
}

export function editar(id, dados) {
  // V-04: qualquer autenticado edita qualquer cadastro; sem auditoria (V-06)
  return axios.put(`${API_BASE}/pacientes/${id}`, dados, authHeader()).then((r) => r.data);
}

export function notasClinicas(pacienteId) {
  // V-19: sem autenticacao nem vinculo - qualquer id retorna a nota
  return axios.get(`${API_BASE}/pacientes/${pacienteId}/notas`).then((r) => r.data);
}
