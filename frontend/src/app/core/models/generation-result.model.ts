export interface GenerationResult {
  pdfFileName: string;
  localDestination: string;
  s3Destination: string;
  localTransferMillis: number;
  s3TransferMillis: number;
  message: string;
}
