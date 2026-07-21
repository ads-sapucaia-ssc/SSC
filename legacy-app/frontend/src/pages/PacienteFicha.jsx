import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import { porId, editar } from '../services/paciente';
import { listarExamesDoPaciente } from '../services/exame';
import { BadgeStatus } from '../components/Badge';
import Prontuario from '../components/Prontuario';

/**
 * Ficha do paciente. Reune 3 endpoints que, juntos, ilustram o mesmo padrao
 * de falha (autorizacao ausente/insuficiente no backend):
 *   - GET  /api/pacientes/{id}         (sem checagem de papel/vinculo)
 *   - PUT  /api/pacientes/{id}         (V-04: qualquer autenticado edita)
 *   - GET  /api/exames/paciente/{id}   (V-03: lista sem checar vinculo)
 * Nenhuma checagem foi adicionada aqui - a tela so torna essas rotas usaveis.
 */
export default function PacienteFicha() {
  const { id } = useParams();
  const navigate = useNavigate();
  const [paciente, setPaciente] = useState(null);
  const [exames, setExames] = useState(null);
  const [editando, setEditando] = useState(false);
  const [form, setForm] = useState({});
  const [salvando, setSalvando] = useState(false);

  const carregar = () => {
    porId(id).then((p) => { setPaciente(p); setForm({ telefone: p.telefone || '', endereco: p.endereco || '' }); });
    listarExamesDoPaciente(id).then(setExames).catch(() => setExames([]));
  };

  useEffect(() => { carregar(); /* eslint-disable-next-line */ }, [id]);

  const salvar = async (e) => {
    e.preventDefault();
    setSalvando(true);
    try {
      await editar(id, form); // V-04: sem checagem de papel nem auditoria (V-06)
      setEditando(false);
      carregar();
    } finally {
      setSalvando(false);
    }
  };

  if (!paciente) return <div className="empty-state">Carregando ficha…</div>;

  return (
    <>
      <div className="breadcrumb"><Link to="/pacientes">Pacientes</Link> / {paciente.nome}</div>
      <div className="page-header row-between">
        <div>
          <h1>{paciente.nome}</h1>
          <div className="subtitle">CPF {paciente.cpf} · paciente #{paciente.id}</div>
        </div>
        <button className="btn btn-secondary btn-sm" onClick={() => setEditando((v) => !v)}>
          {editando ? 'Cancelar' : 'Editar cadastro'}
        </button>
      </div>

      <div className="stack">
        <div className="card">
          <div className="card-header"><h3>Dados cadastrais</h3></div>
          {!editando ? (
            <div className="card-pad grid grid-2">
              <div><label className="field-label">E-mail</label><p>{paciente.email || '—'}</p></div>
              <div><label className="field-label">CPF</label><p className="mono">{paciente.cpf}</p></div>
            </div>
          ) : (
            <form onSubmit={salvar} className="card-pad">
              <div className="field">
                <label className="field-label">Telefone</label>
                <input value={form.telefone} onChange={(e) => setForm({ ...form, telefone: e.target.value })} />
              </div>
              <div className="field">
                <label className="field-label">Endereço</label>
                <input value={form.endereco} onChange={(e) => setForm({ ...form, endereco: e.target.value })} />
              </div>
              <button className="btn btn-primary" disabled={salvando}>
                {salvando ? <span className="spinner" /> : 'Salvar alterações'}
              </button>
            </form>
          )}
        </div>

        <div className="card">
          <div className="card-header"><h3>Histórico de exames</h3></div>
          {exames === null && <div className="empty-state">Carregando…</div>}
          {exames?.length === 0 && <div className="empty-state">Nenhum exame registrado.</div>}
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

        <Prontuario pacienteId={id} />
      </div>
    </>
  );
}
