import axios from 'axios';
import { API_BASE } from '../config/api';
import { getToken } from './auth';

/**
 * Gestao de usuarios pelo front (LEGADO) - tela do ADMIN.
 * V-04: nem listar() nem alternarAtivo() sao restritos no backend - qualquer
 * autenticado (inclusive PACIENTE) consegue chamar os dois.
 */
function authHeader() {
  return { headers: { Authorization: `Bearer ${getToken()}` } };
}

export function listar() {
  return axios.get(`${API_BASE}/usuarios`, authHeader()).then((r) => r.data);
}

export function alternarAtivo(id) {
  return axios.post(`${API_BASE}/usuarios/${id}/alternar-ativo`, {}, authHeader())
    .then((r) => r.data);
}
