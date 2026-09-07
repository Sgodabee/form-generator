import { Component, OnInit } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router } from '@angular/router';
import { FormGenerationService } from '../../core/services/form-generation.service';
import { AuditService } from '../../core/services/audit.service';
import { AuthService } from '../../core/services/auth.service';
import { GenerationResult } from '../../core/models/generation-result.model';
import { AuditLog } from '../../core/models/audit-log.model';

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent implements OnInit {

  username: string | null = null;
  isGenerating = false;
  lastResult: GenerationResult | null = null;
  generationError = '';

  pdfFiles: string[] = [];
  isLoadingFiles = false;

  auditLogs: AuditLog[] = [];
  isLoadingAudit = false;

  constructor(
    private formGenerationService: FormGenerationService,
    private auditService: AuditService,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.username = this.authService.getUsername();
    this.loadFiles();
    this.loadAuditLogs();
  }

  /** Triggers the CSV-to-PDF generation pipeline. */
  generate(): void {
    this.isGenerating = true;
    this.generationError = '';
    this.lastResult = null;

    this.formGenerationService.generate().subscribe({
      next: (result) => {
        this.isGenerating = false;
        this.lastResult = result;
        // Refresh the file list and audit logs after generation
        this.loadFiles();
        this.loadAuditLogs();
      },
      error: (err) => {
        this.isGenerating = false;
        this.generationError = err?.error?.message ?? 'Generation failed. Please try again.';
      }
    });
  }

  loadFiles(): void {
    this.isLoadingFiles = true;
    this.formGenerationService.listFiles().subscribe({
      next: (files) => {
        this.pdfFiles = files;
        this.isLoadingFiles = false;
      },
      error: () => {
        this.isLoadingFiles = false;
      }
    });
  }

  loadAuditLogs(): void {
    this.isLoadingAudit = true;
    this.auditService.getAuditLogs().subscribe({
      next: (logs) => {
        this.auditLogs = logs;
        this.isLoadingAudit = false;
      },
      error: () => {
        this.isLoadingAudit = false;
      }
    });
  }

  logout(): void {
    this.authService.logout().subscribe({
      next: () => this.router.navigate(['/login']),
      error: () => this.router.navigate(['/login'])
    });
  }

  fasterStore(result: GenerationResult): string {
    return result.localTransferMillis <= result.s3TransferMillis ? 'Local' : 'S3';
  }
}
