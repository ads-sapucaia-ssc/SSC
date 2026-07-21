import React, { useEffect, useState } from 'react';
import { listarAgenda, agendar } from '../services/atendimento';

/**
 * Agenda de atendimentos (RECEPCAO).
 * V-04: qualquer autenticado agenda (POST /api/atendimentos) sem checar papel
 * nem validar se medicoId/pacienteId existem de fato.
 */
export default function Agenda() {
  const [atendimentos, setAtendimentos] = useState(null);
  const [form, setForm] = useState({ medicoId: '', pacienteId: '' });
  const [salvando, setSalvando] = useState(false);
  const [erro, setErro] = useState('');

  const carregar = () => listarAgenda().then(setAtendimentos);

  useEffect(() => { carregar(); }, []);

  const submeter = async (e) => {
    e.preventDefault();
    setErro(''); setSalvando(true);
    try {
      await agendar(Number(form.medicoId), Number(form.pacienteId)); // V-04
      setForm({ medicoId: '', pacienteId: '' });
      await carregar();
    } catch {
      setErro('Não foi possível agendar — confira os ids de médico/paciente.');
    } finally {
      setSalvando(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <h1>Agenda</h1>
        <div className="subtitle">Atendimentos marcados — médico × paciente.</div>
      </div>

      <div className="card">
        <div className="card-header"><h3>Novo atendimento</h3></div>
        <form onSubmit={submeter} className="card-pad grid grid-2">
          <div className="field">
            <label className="field-label">ID do médico</label>
            <input type="text" placeholder="Ex.: 1" value={form.medicoId}
                   onChange={(e) => setForm({ ...form, medicoId: e.target.value })} required />
          </div>
          <div className="field">
            <label className="field-label">ID do paciente</label>
            <input type="text" placeholder="Ex.: 2" value={form.pacienteId}
                   onChange={(e) => setForm({ ...form, pacienteId: e.target.value })} required />
          </div>
          <div style={{ gridColumn: '1 / -1' }}>
            {erro && <div className="alert alert-danger mt-8">{erro}</div>}
            <button className="btn btn-primary mt-8" disabled={salvando}>
              {salvando ? <span className="spinner" /> : 'Agendar'}
            </button>
          </div>
        </form>
      </div>

      <div className="card mt-24">
        <div className="card-header"><h3>Atendimentos marcados</h3></div>
        {atendimentos === null && <div className="empty-state">Carregando…</div>}
        {atendimentos?.length === 0 && <div className="empty-state">Nenhum atendimento.</div>}
        {atendimentos?.map((a) => (
          <div key={a.id} className="list-item">
            <div>
              <div className="titulo">{a.paciente_nome} <span className="faint">com</span> {a.medico_nome}</div>
              <div className="meta">{new Date(a.data).toLocaleString('pt-BR')}</div>
            </div>
          </div>
        ))}
      </div>
    </>
  );
}
