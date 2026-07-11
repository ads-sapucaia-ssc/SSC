/**
 * Configuracao de acesso a API (LEGADO).
 * V-13: comunicacao em HTTP puro (sem TLS). Os grupos devem migrar para HTTPS
 *       e habilitar HSTS no proxy/ingress.
 */
export const API_BASE = 'http://localhost:8080/api';

// V-05 (variante front): nao guardar segredos aqui. O legado nao tem, mas
// alertar os alunos para nunca colocar tokens/keys em codigo do cliente.
