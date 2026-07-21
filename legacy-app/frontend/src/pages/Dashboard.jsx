import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { listarExamesDoPaciente } from '../services/exame';
import { BadgeStatus } from '../components/Badge';
import Prontuario from '../components/Prontuario';

function saudacao() {
  const h = new Date().getHours();
  if (h < 12) return 'Bom dia';
  if (h < 18) return 'Boa tarde';
  return 'Boa noite';
}

function DashboardPaciente({ usuario }) {
  const [exames, setExames] = useState(null);
  const navigate = useNavigate();

  useEffect(() => {
    listarExamesDoPaciente(usuario.pacienteId).then(setExames).catch(() => setExames([]));
  }, [usuario.pacienteId]);

  const pendentes = (exames || []).filter((e) => !e.publicado).length;

  return (
    <>
      <div className="page-header">
        <h1>{saudacao()}, {usuario.nome?.split(' ')[0] || 'paciente'}.</h1>
        <div className="subtitle">Este é o seu espaço no VidaPlus — exames, laudos e prontuário.</div>
      </div>

      <div className="grid grid-2 mt-16">
        <div className="card stat-tile">
          <div className="stat-icon" style={{ background: 'var(--info-bg)', color: 'var(--info)' }}>🧪</div>
          <div className="stat-value">{exames === null ? '—' : exames.length}</div>
          <div className="stat-label">Exames no seu histórico</div>
        </div>
        <div className="card stat-tile">
          <div className="stat-icon" style={{ background: 'var(--warning-bg)', color: 'var(--warning)' }}>⏳</div>
          <div className="stat-value">{exames === null ? '—' : pendentes}</div>
          <div className="stat-label">Laudos pendentes de publicação</div>
        </div>
      </div>

      <div className="card mt-24">
        <div className="card-header">
          <h3>Meus exames</h3>
          <span className="muted text-sm">paciente #{usuario.pacienteId}</span>
        </div>
        {exames === null && <div className="empty-state">Carregando…</div>}
        {exames?.length === 0 && <div className="empty-state">Nenhum exame encontrado.</div>}
        {exames?.map((ex) => (
          <div key={ex.id} className="list-item" style={{ cursor: 'pointer' }}
               onClick={() => navigate(`/exames/${ex.id}`)}>
            <div>
              <div className="titulo">{ex.tipo}</div>
              <div className="meta">
                {ex.criado_em ? new Date(ex.criado_em).toLocaleDateString('pt-BR') : '—'} · protocolo #{ex.id}
              </div>
            </div>
            <BadgeStatus publicado={!!ex.publicado} />
          </div>
        ))}
      </div>

      <div className="mt-24">
        <Prontuario pacienteId={usuario.pacienteId} titulo="Meu prontuário" />
      </div>
    </>
  );
}

function DashboardEquipe({ usuario }) {
  const navigate = useNavigate();
  const [protocolo, setProtocolo] = useState('');

  const abrirExame = (e) => {
    e.preventDefault();
    if (protocolo.trim()) navigate(`/exames/${protocolo.trim()}`);
  };

  return (
    <>
      <div className="page-header">
        <h1>{saudacao()}, {usuario.nome?.split(' ')[0] || ''}.</h1>
        <div className="subtitle">Painel operacional — VidaPlus.</div>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <div className="card-header"><h3>Buscar paciente</h3></div>
          <div className="card-pad">
            <p className="muted text-sm">
              Localize um paciente por nome ou CPF para abrir a ficha, os exames e o prontuário.
            </p>
            <button className="btn btn-primary mt-16" onClick={() => navigate('/pacientes')}>
              Ir para Pacientes
            </button>
          </div>
        </div>

        <div className="card">
          <div className="card-header"><h3>Consulta por protocolo</h3></div>
          <div className="card-pad">
            <p className="muted text-sm">Abra diretamente um exame pelo número de protocolo.</p>
            <form onSubmit={abrirExame} className="row mt-16">
              <input
                type="text" placeholder="Ex.: 101" value={protocolo}
                onChange={(e) => setProtocolo(e.target.value)}
              />
              <button type="submit" className="btn btn-secondary">Abrir</button>
            </form>
          </div>
        </div>
      </div>
    </>
  );
}

export default function Dashboard({ usuario }) {
  return usuario.papel === 'PACIENTE'
    ? <DashboardPaciente usuario={usuario} />
    : <DashboardEquipe usuario={usuario} />;
}
