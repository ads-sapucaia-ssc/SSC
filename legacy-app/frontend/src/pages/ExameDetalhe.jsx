import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { getExame, getPdf, publicar } from '../services/exame';
import { BadgeStatus } from '../components/Badge';

/**
 * V-03 (IDOR): esta pagina busca o exame pelo :id da URL, sem checar dono no
 * backend. Troque o numero na barra de enderecos (ex.: /exames/102 estando
 * logado como Ana, dona apenas do 101/103) para reproduzir - exatamente como
 * um usuario real faria, nao so via curl.
 */
export default function ExameDetalhe({ usuario }) {
  const { id } = useParams();
  const [exame, setExame] = useState(null);
  const [pdf, setPdf] = useState(null);
  const [erro, setErro] = useState('');
  const [publicando, setPublicando] = useState(false);

  useEffect(() => {
    setExame(null); setErro('');
    getExame(id).then(setExame).catch(() => setErro('Exame não encontrado ou acesso negado.'));
    getPdf(id).then(setPdf).catch(() => setPdf(null));
  }, [id]);

  const publicarLaudo = async () => {
    setPublicando(true);
    try {
      await publicar(id); // V-04: backend nao checa papel BIOQUIMICO
      setExame((e) => ({ ...e, publicado: 1 }));
    } finally {
      setPublicando(false);
    }
  };

  return (
    <>
      <div className="breadcrumb"><Link to="/">Início</Link> / Exame #{id}</div>
      <div className="page-header">
        <h1>Exame — protocolo #{id}</h1>
        <div className="subtitle">Resultado, laudo e ações associadas a este exame.</div>
      </div>

      {erro && <div className="alert alert-danger">{erro}</div>}

      {exame && (
        <div className="stack">
          <div className="card">
            <div className="card-header">
              <h3>{exame.tipo || 'Exame'}</h3>
              <BadgeStatus publicado={!!exame.publicado} />
            </div>
            <div className="card-pad grid grid-2">
              <div>
                <label className="field-label">Paciente</label>
                <p>#{exame.paciente_id}</p>
              </div>
              <div>
                <label className="field-label">Solicitado em</label>
                <p>{exame.criado_em ? new Date(exame.criado_em).toLocaleString('pt-BR') : '—'}</p>
              </div>
              <div style={{ gridColumn: '1 / -1' }}>
                <label className="field-label">Resultado</label>
                <p>{exame.resultado || <span className="faint">Sem resultado registrado.</span>}</p>
              </div>
            </div>
          </div>

          <div className="card">
            <div className="card-header"><h3>Laudo</h3></div>
            <div className="card-pad row-between">
              <span className="muted text-sm mono">{pdf?.laudo_pdf || '—'}</span>
              <div className="row">
                {pdf?.laudo_pdf && (
                  <a className="btn btn-secondary btn-sm" href={pdf.laudo_pdf} target="_blank" rel="noreferrer">
                    Baixar PDF
                  </a>
                )}
                {!exame.publicado && (
                  <button className="btn btn-primary btn-sm" onClick={publicarLaudo} disabled={publicando}>
                    {publicando ? <span className="spinner" /> : 'Publicar laudo'}
                  </button>
                )}
              </div>
            </div>
            {/*
              Nota: o botao "Publicar laudo" so aparece para deixar o fluxo
              usavel na demo - o backend NAO verifica papel (V-04). Esconder o
              botao no front nao e controle de acesso: qualquer usuario
              autenticado ainda publica via POST direto, mesmo sem ver o botao.
            */}
          </div>
        </div>
      )}
    </>
  );
}
