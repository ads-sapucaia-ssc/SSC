import React, { useState } from 'react';
import { Routes, Route, Navigate } from 'react-router-dom';
import { getUsuario } from './services/auth';
import Layout from './components/Layout';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import ExameDetalhe from './pages/ExameDetalhe';
import BuscaPacientes from './pages/BuscaPacientes';
import PacienteFicha from './pages/PacienteFicha';
import Usuarios from './pages/Usuarios';
import MeusPacientes from './pages/MeusPacientes';
import Agenda from './pages/Agenda';
import FilaLaudos from './pages/FilaLaudos';

/**
 * Portal VidaPlus (LEGADO) - React 19 + React Router.
 * Concentra os pontos vulneraveis-chave para uso em aula (ver README e docs/07
 * no repositorio do professor). A UI foi desenhada para parecer um sistema de
 * saude real; NENHUMA falha muda de comportamento por causa da UI - so a
 * apresentacao em volta delas.
 */
export default function App() {
  const [usuario, setUsuario] = useState(getUsuario());

  if (!usuario) {
    return <Login onEntrar={() => setUsuario(getUsuario())} />;
  }

  return (
    <Layout usuario={usuario}>
      <Routes>
        <Route path="/" element={<Dashboard usuario={usuario} />} />
        <Route path="/exames/:id" element={<ExameDetalhe usuario={usuario} />} />
        <Route path="/pacientes" element={<BuscaPacientes />} />
        <Route path="/pacientes/:id" element={<PacienteFicha />} />
        <Route path="/usuarios" element={<Usuarios />} />
        <Route path="/meus-pacientes" element={<MeusPacientes usuario={usuario} />} />
        <Route path="/agenda" element={<Agenda />} />
        <Route path="/fila-laudos" element={<FilaLaudos />} />
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </Layout>
  );
}
