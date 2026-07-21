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

export function publicar(id) {
  // V-04: qualquer autenticado publica (backend nao checa papel BIOQUIMICO)
  return axios.post(`${API_BASE}/exames/${id}/publicar`, {}, authHeader()).then((r) => r.data);
}

export function listarExamesDoPaciente(pacienteId) {
  // V-03 tambem na listagem: nenhum vinculo checado - id de paciente livre.
  return axios.get(`${API_BASE}/exames/paciente/${pacienteId}`, authHeader()).then((r) => r.data);
}

export function listarPendentes() {
  // V-04: fila de TODOS os pacientes - nao checa papel BIOQUIMICO.
  return axios.get(`${API_BASE}/exames/pendentes`, authHeader()).then((r) => r.data);
}

export function solicitar(pacienteId, tipo) {
  // V-03: nao checa se pacienteId e o proprio autenticado.
  return axios.post(`${API_BASE}/exames/solicitar`, { pacienteId, tipo }, authHeader())
    .then((r) => r.data);
}
