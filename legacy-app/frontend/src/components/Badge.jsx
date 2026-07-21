import React from 'react';

export const ROTULO_PAPEL = {
  ADMIN: 'Administrador',
  MEDICO: 'Médico(a)',
  RECEPCAO: 'Recepção',
  BIOQUIMICO: 'Bioquímico(a)',
  PACIENTE: 'Paciente',
};

export function BadgePapel({ papel }) {
  if (!papel) return null;
  return <span className={`badge badge-${papel}`}>{ROTULO_PAPEL[papel] || papel}</span>;
}

export function BadgeStatus({ publicado }) {
  return publicado
    ? <span className="badge badge-status-ok">Publicado</span>
    : <span className="badge badge-status-pendente">Pendente</span>;
}
