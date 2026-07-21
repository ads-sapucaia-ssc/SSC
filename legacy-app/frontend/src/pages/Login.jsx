import React, { useState } from 'react';
import { login, salvarSessao } from '../services/auth';

export default function Login({ onEntrar }) {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [erro, setErro] = useState('');
  const [carregando, setCarregando] = useState(false);

  const entrar = async (e) => {
    e.preventDefault();
    setErro('');
    setCarregando(true);
    try {
      const resp = await login(email, senha); // V-01/V-13: login sobre HTTP, SQLi no backend
      if (resp && resp.access_token) {
        salvarSessao(resp); // V-07: token eterno em localStorage
        onEntrar();
      } else {
        setErro(resp?.erro || 'Credenciais inválidas.');
      }
    } catch {
      setErro('Não foi possível entrar. Tente novamente.');
    } finally {
      setCarregando(false);
    }
  };

  return (
    <div className="login-screen">
      <div className="login-card">
        <div className="login-brand">
          <div className="logo-mark">V+</div>
          <h1>VidaPlus</h1>
          <div className="tagline">Sistema de Gestão Clínica</div>
        </div>

        {erro && <div className="alert alert-danger mt-8">{erro}</div>}

        <form onSubmit={entrar} className="mt-16">
          <div className="field">
            <label className="field-label" htmlFor="email">E-mail</label>
            <input
              id="email" type="email" placeholder="seu.email@vidaplus.local"
              value={email} onChange={(e) => setEmail(e.target.value)} autoFocus required
            />
          </div>
          <div className="field">
            <label className="field-label" htmlFor="senha">Senha</label>
            <input
              id="senha" type="password" placeholder="••••••••"
              value={senha} onChange={(e) => setSenha(e.target.value)} required
            />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={carregando}>
            {carregando ? <span className="spinner" /> : 'Entrar'}
          </button>
        </form>

        <div className="login-hint">
          Acesso restrito a profissionais e pacientes cadastrados.<br />
          Esqueceu a senha? Use a recuperação em <span className="mono">/api/auth/reset</span>.
        </div>
      </div>
    </div>
  );
}
