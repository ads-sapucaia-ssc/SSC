import React, { useEffect, useState } from 'react';
import { notasClinicas } from '../services/paciente';

/**
 * Prontuario / notas clinicas (LEGADO).
 *
 * V-10 (Stored XSS): a nota clinica vem do backend sem sanitizacao e e
 * renderizada com dangerouslySetInnerHTML -> executa scripts plantados
 * (ver seed.sql: <img onerror=...> que rouba o token do localStorage).
 * V-19: o endpoint de notas nao exige autenticacao nem checa vinculo.
 *
 * Correcao esperada (Grupo 5): NAO usar dangerouslySetInnerHTML; renderizar como
 * texto (JSX escapa por padrao) ou sanitizar (DOMPurify) no cliente E no servidor,
 * e aplicar CSP. Correcao esperada (Grupo 2, V-19): exigir autenticacao + vinculo.
 */
export default function Prontuario({ pacienteId, titulo = 'Prontuário — nota clínica' }) {
  const [nota, setNota] = useState(null);
  const [carregando, setCarregando] = useState(true);

  useEffect(() => {
    setCarregando(true);
    notasClinicas(pacienteId)
      .then((data) => setNota(data)) // V-10: confia cegamente no conteudo vindo do banco
      .catch(() => setNota(null))
      .finally(() => setCarregando(false));
  }, [pacienteId]);

  return (
    <div className="card">
      <div className="card-header">
        <h3>{titulo}</h3>
        {!carregando && <span className="muted text-sm">paciente #{pacienteId}</span>}
      </div>
      <div className="card-pad">
        {carregando && <span className="faint text-sm">Carregando nota clínica…</span>}
        {!carregando && (!nota || !nota.conteudo) && (
          <div className="empty-state">Nenhuma nota clínica registrada para este paciente.</div>
        )}
        {!carregando && nota?.conteudo && (
          <>
            {/* V-10: dangerouslySetInnerHTML com conteudo confiado indevidamente */}
            <div className="prontuario-nota" dangerouslySetInnerHTML={{ __html: nota.conteudo }} />
            <div className="prontuario-meta">
              Registrado em {nota.criado_em ? new Date(nota.criado_em).toLocaleString('pt-BR') : '—'}
            </div>
          </>
        )}
      </div>
    </div>
  );
}
