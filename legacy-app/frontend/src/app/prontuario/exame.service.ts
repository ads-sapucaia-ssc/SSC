import { Injectable } from '@angular/core';
import { HttpClient, HttpHeaders } from '@angular/common/http';
import { API_BASE } from '../core/api.config';
import { AuthService } from '../auth/auth.service';

/**
 * Acesso a exames pelo front (LEGADO).
 * V-03 (IDOR do lado cliente): o app monta a URL com o id do exame vindo da tela,
 * confiando que o backend fara a checagem (que NAO faz). Demonstra em aula que
 * "esconder no front" nao e controle: trocar o id no request retorna laudo alheio.
 */
@Injectable({ providedIn: 'root' })
export class ExameService {

  constructor(private http: HttpClient, private auth: AuthService) {}

  private headers() {
    return new HttpHeaders({ Authorization: `Bearer ${this.auth.token}` });
  }

  getExame(id: number) {
    return this.http.get<any>(`${API_BASE}/exames/${id}`, { headers: this.headers() });
  }

  getPdf(id: number) {
    // V-03/V-09: qualquer id retorna o caminho do PDF (pasta publica)
    return this.http.get<any>(`${API_BASE}/exames/${id}/pdf`, { headers: this.headers() });
  }
}
