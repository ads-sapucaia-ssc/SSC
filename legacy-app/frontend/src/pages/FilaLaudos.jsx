import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listarPendentes } from '../services/exame';

/**
 * Fila de laudos pendentes de publicacao (BIOQUIMICO).
 * V-04: lista o sistema INTEIRO (todos os pacientes), sem checar papel
 * BIOQUIMICO - qualquer autenticado ve a fila completa de rascunhos,
 * inclusive resultados sensiveis ainda nao liberados (ver docs/04, nota
 * F-07 x F-08: "publicado" e so rotulo de fluxo, nao controle de acesso).
 */
export default function FilaLaudos() {
  const [exames, setExames] = useState(null);
  const navigate = useNavigate();

  useEffect(() => { listarPendentes().then(setExames); }, []);

  return (
    <>
      <div className="page-header">
        <h1>Fila de laudos pendentes</h1>
        <div className="subtitle">Exames de todos os pacientes ainda sem publicação.</div>
      </div>

      <div className="card">
        {exames === null && <div className="empty-state">Carregando…</div>}
        {exames?.length === 0 && <div className="empty-state">Nenhum laudo pendente. 🎉</div>}
        {exames?.map((ex) => (
          <div key={ex.id} className="list-item" style={{ cursor: 'pointer' }}
               onClick={() => navigate(`/exames/${ex.id}`)}>
            <div>
              <div className="titulo">{ex.tipo} — {ex.paciente_nome}</div>
              <div className="meta">
                paciente #{ex.paciente_id} · protocolo #{ex.id} ·{' '}
                {ex.criado_em ? new Date(ex.criado_em).toLocaleDateString('pt-BR') : '—'}
              </div>
            </div>
            <span className="badge badge-status-pendente">Pendente</span>
          </div>
        ))}
      </div>
    </>
  );
}
