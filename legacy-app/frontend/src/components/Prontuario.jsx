import React, { useEffect, useState } from 'react';
import axios from 'axios';
import { API_BASE } from '../config/api';

/**
 * Prontuario / notas clinicas (LEGADO).
 *
 * V-10 (Stored XSS): a nota clinica vem do backend sem sanitizacao e e
 * renderizada com dangerouslySetInnerHTML -> executa scripts plantados
 * (ver seed.sql: '<img src=x onerror=...>').
 *
 * Correcao esperada (Grupo D): NAO usar dangerouslySetInnerHTML; renderizar como
 * texto (JSX escapa por padrao) ou sanitizar (DOMPurify) no cliente E no servidor,
 * e aplicar CSP.
 */
export default function Prontuario({ pacienteId }) {
  const [nota, setNota] = useState('');

  useEffect(() => {
    axios.get(`${API_BASE}/pacientes/${pacienteId}/notas`).then((r) => {
      // V-10: confia cegamente no conteudo vindo do banco
      setNota(r.data.conteudo);
    });
  }, [pacienteId]);

  return (
    <div>
      <h2>Prontuário</h2>
      {/* V-10: dangerouslySetInnerHTML com conteudo confiado indevidamente */}
      <div className="nota" dangerouslySetInnerHTML={{ __html: nota }} />
    </div>
  );
}
