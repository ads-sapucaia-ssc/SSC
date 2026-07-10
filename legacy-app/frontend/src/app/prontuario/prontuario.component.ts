import { Component } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { DomSanitizer, SafeHtml } from '@angular/platform-browser';
import { API_BASE } from '../core/api.config';

/**
 * Prontuario / notas clinicas (LEGADO).
 *
 * V-10 (Stored XSS): a nota clinica vem do backend sem sanitizacao e e
 * renderizada com [innerHTML] usando bypassSecurityTrustHtml -> executa scripts
 * plantados (ver seed.sql: '<img src=x onerror=...>').
 *
 * Correcao esperada (Grupo D): remover bypassSecurityTrust, sanitizar no servidor,
 * usar interpolacao/output encoding e aplicar CSP.
 */
@Component({
  selector: 'app-prontuario',
  template: `
    <h2>Prontuário</h2>
    <!-- V-10: innerHTML com conteudo confiado indevidamente -->
    <div class="nota" [innerHTML]="notaHtml"></div>
  `
})
export class ProntuarioComponent {
  notaHtml: SafeHtml = '';

  constructor(private http: HttpClient, private sanitizer: DomSanitizer) {}

  carregar(pacienteId: number) {
    this.http.get<any>(`${API_BASE}/pacientes/${pacienteId}/notas`).subscribe(nota => {
      // V-10: confia cegamente no conteudo vindo do banco
      this.notaHtml = this.sanitizer.bypassSecurityTrustHtml(nota.conteudo);
    });
  }
}
