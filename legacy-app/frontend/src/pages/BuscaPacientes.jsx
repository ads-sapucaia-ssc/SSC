import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { buscar } from '../services/paciente';

/**
 * V-13 (SQLi): 'termo' vai direto para uma query concatenada no backend.
 *   Experimente: ' OR '1'='1
 * V-14 (sobrecoleta): a resposta traz religiao/renda_mensal - dados sem
 *   finalidade clara para uma busca de paciente (minimizacao, LGPD art. 6).
 */
export default function BuscaPacientes() {
  const [termo, setTermo] = useState('');
  const [resultados, setResultados] = useState(null);
  const [buscando, setBuscando] = useState(false);
  const navigate = useNavigate();

  const pesquisar = async (e) => {
    e.preventDefault();
    setBuscando(true);
    try {
      setResultados(await buscar(termo));
    } finally {
      setBuscando(false);
    }
  };

  return (
    <>
      <div className="page-header">
        <h1>Pacientes</h1>
        <div className="subtitle">Busque por nome ou CPF para abrir a ficha do paciente.</div>
      </div>

      <div className="card">
        <div className="card-pad">
          <form onSubmit={pesquisar} className="row">
            <input
              type="search" placeholder="Nome ou CPF…" value={termo}
              onChange={(e) => setTermo(e.target.value)} autoFocus
            />
            <button type="submit" className="btn btn-primary" disabled={buscando}>
              {buscando ? <span className="spinner" /> : 'Buscar'}
            </button>
          </form>
        </div>

        {resultados !== null && (
          resultados.length === 0 ? (
            <div className="empty-state">Nenhum paciente encontrado para "{termo}".</div>
          ) : (
            <table className="data-table">
              <thead>
                <tr>
                  <th>Nome</th><th>CPF</th><th>E-mail</th><th>Religião</th><th>Renda mensal</th>
                </tr>
              </thead>
              <tbody>
                {resultados.map((p) => (
                  <tr key={p.id} className="clickable" onClick={() => navigate(`/pacientes/${p.id}`)}>
                    <td>{p.nome}</td>
                    <td className="mono">{p.cpf}</td>
                    <td>{p.email}</td>
                    <td>{p.religiao || '—'}</td>
                    <td>{p.renda_mensal != null ? `R$ ${Number(p.renda_mensal).toFixed(2)}` : '—'}</td>
                  </tr>
                ))}
              </tbody>
            </table>
          )
        )}
      </div>
    </>
  );
}
