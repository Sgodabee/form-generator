export interface AuditLog {
  id: number;
  username: string;
  csvFileInput: string;
  pdfFileOutput: string;
  fileDestination: string;
  timestamp: string;
}
