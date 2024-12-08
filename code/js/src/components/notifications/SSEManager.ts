let sseInstance: EventSource | null = null;

export function initializeSSE(url: string): EventSource {
  if (!sseInstance) {
    sseInstance = new EventSource(url);

    sseInstance.onerror = error => {
      console.error('Error Event Source', error);
      sseInstance?.close();
      sseInstance = null;
    };
  }
  return sseInstance;
}

export function getSSE(): EventSource | null {
  return sseInstance;
}

export function closeSSE() {
  if (sseInstance) {
    sseInstance.close();
    sseInstance = null;
  }
}
