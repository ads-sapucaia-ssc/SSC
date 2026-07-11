import axios from 'axios';
import { API_BASE } from '../config/api';
import { getToken } from './auth';

/**
 * Acesso a exames pelo front (LEGADO).
 * V-03 (IDOR do lado cliente): o app monta a URL com o id do exame vindo da tela,
 * confiando que o backend fara a checagem (que NAO faz). Demonstra em aula que
 * "esconder no front" nao e controle: trocar o id no request retorna laudo alheio.
 */
function authHeader() {
  return { headers: { Authorization: `Bearer ${getToken()}` } };
}

export function getExame(id) {
  return axios.get(`${API_BASE}/exames/${id}`, authHeader()).then((r) => r.data);
}

export function getPdf(id) {
  // V-03/V-09: qualquer id retorna o caminho do PDF (pasta publica)
  return axios.get(`${API_BASE}/exames/${id}/pdf`, authHeader()).then((r) => r.data);
}
