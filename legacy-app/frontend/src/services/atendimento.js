import axios from 'axios';
import { API_BASE } from '../config/api';
import { getToken } from './auth';

/**
 * Atendimentos pelo front (LEGADO) - agenda (RECEPCAO) e carteira de
 * pacientes (MEDICO).
 * V-04/V-03: nenhum dos tres endpoints checa papel nem ownership do medicoId.
 */
function authHeader() {
  return { headers: { Authorization: `Bearer ${getToken()}` } };
}

export function listarAgenda() {
  return axios.get(`${API_BASE}/atendimentos`, authHeader()).then((r) => r.data);
}

export function agendar(medicoId, pacienteId) {
  return axios.post(`${API_BASE}/atendimentos`, { medicoId, pacienteId }, authHeader())
    .then((r) => r.data);
}

export function pacientesDoMedico(medicoId) {
  return axios.get(`${API_BASE}/atendimentos/medico/${medicoId}`, authHeader())
    .then((r) => r.data);
}
