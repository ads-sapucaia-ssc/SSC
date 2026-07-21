import React from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';
import { logout } from '../services/auth';
import { BadgePapel } from './Badge';

const NAV_POR_PAPEL = {
  PACIENTE: [{ to: '/', label: 'Início' }],
  MEDICO: [
    { to: '/', label: 'Início' },
    { to: '/pacientes', label: 'Pacientes' },
    { to: '/meus-pacientes', label: 'Meus Pacientes' },
  ],
  RECEPCAO: [
    { to: '/', label: 'Início' },
    { to: '/pacientes', label: 'Pacientes' },
    { to: '/agenda', label: 'Agenda' },
  ],
  BIOQUIMICO: [
    { to: '/', label: 'Início' },
    { to: '/pacientes', label: 'Pacientes' },
    { to: '/fila-laudos', label: 'Fila de Laudos' },
  ],
  ADMIN: [
    { to: '/', label: 'Início' },
    { to: '/pacientes', label: 'Pacientes' },
    { to: '/usuarios', label: 'Usuários' },
  ],
};

function iniciais(nome) {
  if (!nome) return '?';
  const partes = nome.trim().split(/\s+/);
  return (partes[0][0] + (partes[1]?.[0] || '')).toUpperCase();
}

export default function Layout({ usuario, children }) {
  const navigate = useNavigate();
  const itens = NAV_POR_PAPEL[usuario.papel] || NAV_POR_PAPEL.PACIENTE;

  const sair = () => {
    logout();
    navigate('/login', { replace: true });
    // V-07: o navigate() so troca a tela; o token antigo segue valido no
    // servidor (sem expiracao/invalidacao) - ver README para reproduzir.
    window.location.reload();
  };

  return (
    <div className="app-shell">
      <header className="topbar">
        <Link to="/" className="topbar-brand">
          <span className="logo-mark">V+</span>
          <span>
            VidaPlus
            <small>Sistema de Gestão Clínica</small>
          </span>
        </Link>

        <nav className="topbar-nav">
          {itens.map((it) => (
            <NavLink
              key={it.to}
              to={it.to}
              end={it.to === '/'}
              className={({ isActive }) => (isActive ? 'active' : '')}
            >
              {it.label}
            </NavLink>
          ))}
        </nav>

        <div className="topbar-user">
          <div className="who">
            <div className="nome">{usuario.nome || 'Usuário'}</div>
            <BadgePapel papel={usuario.papel} />
          </div>
          <div className="avatar">{iniciais(usuario.nome)}</div>
          <button className="btn-sair" onClick={sair}>Sair</button>
        </div>
      </header>

      <main className="content">{children}</main>
    </div>
  );
}
