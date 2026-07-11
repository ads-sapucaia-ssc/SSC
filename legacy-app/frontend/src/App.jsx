import React, { useState } from 'react';
import { login, salvarToken, getToken } from './services/auth';
import { getExame } from './services/exame';
import Prontuario from './components/Prontuario';

/**
 * Portal VidaPlus (LEGADO) - esqueleto representativo em React.
 * Concentra os pontos vulneraveis-chave para uso em aula (ver README).
 */
export default function App() {
  const [email, setEmail] = useState('');
  const [senha, setSenha] = useState('');
  const [logado, setLogado] = useState(!!getToken());
  const [exameId, setExameId] = useState('101');
  const [exame, setExame] = useState(null);

  const entrar = async (e) => {
    e.preventDefault();
    const resp = await login(email, senha); // V-01/V-13: login sobre HTTP, SQLi no backend
    if (resp && resp.access_token) {
      salvarToken(resp.access_token); // V-07/V-10: token em localStorage
      setLogado(true);
    }
  };

  // V-03 (IDOR do lado cliente): busca por id livre; backend nao checa dono
  const buscarExame = async () => setExame(await getExame(exameId));

  if (!logado) {
    return (
      <form onSubmit={entrar}>
        <h1>VidaPlus — Login</h1>
        <input placeholder="e-mail" value={email} onChange={(e) => setEmail(e.target.value)} />
        <input placeholder="senha" type="password" value={senha} onChange={(e) => setSenha(e.target.value)} />
        <button type="submit">Entrar</button>
      </form>
    );
  }

  return (
    <div>
      <h1>VidaPlus</h1>
      {/* V-03: troque o id para tentar acessar exame de outro paciente */}
      <input value={exameId} onChange={(e) => setExameId(e.target.value)} />
      <button onClick={buscarExame}>Ver exame</button>
      {exame && <pre>{JSON.stringify(exame, null, 2)}</pre>}
      <Prontuario pacienteId={1} />
    </div>
  );
}
