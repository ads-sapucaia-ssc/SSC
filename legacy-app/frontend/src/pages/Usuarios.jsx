import React, { useEffect, useState } from 'react';
import { listar, alternarAtivo } from '../services/usuario';
import { BadgePapel } from '../components/Badge';

/**
 * Gestao de usuarios (ADMIN).
 * V-04: a tela funciona para QUALQUER papel autenticado - o backend nao
 * verifica se quem chama e ADMIN (tente logar como paciente e abrir /usuarios).
 */
export default function Usuarios() {
  const [usuarios, setUsuarios] = useState(null);
  const [ocupado, setOcupado] = useState(null);

  const carregar = () => listar().then(setUsuarios);

  useEffect(() => { carregar(); }, []);

  const toggle = async (id) => {
    setOcupado(id);
    try {
      await alternarAtivo(id); // V-04: sem checagem de papel nem auditoria
      await carregar();
    } finally {
      setOcupado(null);
    }
  };

  return (
    <>
      <div className="page-header">
        <h1>Usuários</h1>
        <div className="subtitle">Gestão de contas do VidaPlus — ativar/desativar acesso.</div>
      </div>

      <div className="card">
        {usuarios === null && <div className="empty-state">Carregando…</div>}
        {usuarios && (
          <table className="data-table">
            <thead>
              <tr><th>E-mail</th><th>Papel</th><th>Status</th><th></th></tr>
            </thead>
            <tbody>
              {usuarios.map((u) => (
                <tr key={u.id}>
                  <td>{u.email}</td>
                  <td><BadgePapel papel={u.papel} /></td>
                  <td>
                    <span className={`badge ${u.ativo ? 'badge-status-ok' : 'badge-status-pendente'}`}>
                      {u.ativo ? 'Ativo' : 'Inativo'}
                    </span>
                  </td>
                  <td>
                    <button className="btn btn-secondary btn-sm" disabled={ocupado === u.id}
                            onClick={() => toggle(u.id)}>
                      {ocupado === u.id ? <span className="spinner" /> : (u.ativo ? 'Desativar' : 'Ativar')}
                    </button>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        )}
      </div>
    </>
  );
}
