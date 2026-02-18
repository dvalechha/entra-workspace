/**
 * workflowService.ts
 * Handles all communication with the Orchestrator via Node Proxy.
 */

export interface Case {
  caseId: string;
  businessKey: string;
  status: 'RUNNING' | 'COMPLETED' | string;
}

export const workflowService = {
  
  // Fetches cases based on status: '', 'running', or 'completed'
  fetchCases: async (status = ''): Promise<Case[]> => {
    const url = status ? `/workflow/cases?status=${status}` : '/workflow/cases';
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    return response.json();
  },

  // Fetches the macro roadmap for a specific case
  fetchMacroView: async (lookupId: string) => {
    const response = await fetch(`/workflow/case/${lookupId}/macro`);
    if (!response.ok) throw new Error('Failed to fetch roadmap');
    return response.json();
  }
};
