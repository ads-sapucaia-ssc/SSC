import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { pacientesDoMedico } from '../services/atendimento';

/**
 * Carteira de pacientes do medico (via tabela atendimento).
 * V-03/V-04: o backend nao verifica se o medicoId consultado e o do proprio
 * medico autenticado - troque /api/atendimentos/medico/{id} por outro id
 * (ex.: via DevTools) e veja a carteira de outro medico.
 */
export default function MeusPacientes({ usuario }) {
  const [pacientes, setPacientes] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    if (!usuario.medicoId) { setPacientes([]); return; }
    pacientesDoMedico(usuario.medicoId).then(setPacientes);
  }, [usuario.medicoId]);

  return (
    <>
      <div className="page-header">
        <h1>Meus pacientes</h1>
        <div className="subtitle">Pacientes vinculados a você por atendimento.</div>
      </div>

      <div className="card">
        {pacientes === null && <div className="empty-state">Carregando…</div>}
        {pacientes?.length === 0 && (
          <div className="empty-state">
            {usuario.medicoId ? 'Nenhum paciente vinculado.' : 'Este usuário não tem registro de médico vinculado.'}
          </div>
        )}
        {pacientes?.map((p) => (
          <div key={p.id} className="list-item" style={{ cursor: 'pointer' }}
               onClick={() => navigate(`/pacientes/${p.id}`)}>
            <div>
              <div className="titulo">{p.nome}</div>
              <div className="meta mono">{p.cpf}</div>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
