import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { API_BASE } from '../core/api.config';

/**
 * Servico de autenticacao (LEGADO).
 * V-13: usa HTTP puro (API_BASE).
 * V-07 (variante front): guarda o JWT eterno em localStorage (acessivel por XSS).
 *   Os grupos devem discutir cookie HttpOnly + SameSite e sessao de curta duracao.
 */
@Injectable({ providedIn: 'root' })
export class AuthService {

  constructor(private http: HttpClient) {}

  login(email: string, senha: string) {
    return this.http.post<any>(`${API_BASE}/auth/login`, { email, senha });
  }

  salvarToken(token: string) {
    // V-07/V-10: token em localStorage e roubavel via XSS (ver V-10)
    localStorage.setItem('access_token', token);
  }

  get token(): string | null {
    return localStorage.getItem('access_token');
  }

  logout() {
    localStorage.removeItem('access_token');
  }
}
