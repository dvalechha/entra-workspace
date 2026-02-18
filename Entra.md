# 🚀 CLI COMMAND: React Frontend Integration (Proxy & Dashboard)

**Goal:** Configure the `react-bff-app` to route calls through a Node proxy to the `orchestrator-backend` (Port 8081) and implement the Credit Intake Dashboard.

---

## Step 1: Node Proxy Configuration
Update `react-bff-app/package.json` to include the proxy entry. This tells the React development server to forward any unknown requests (like `/workflow/*`) to the orchestrator.

```json
{
  "name": "react-bff-app",
  "version": "0.1.0",
  "private": true,
  "proxy": "http://localhost:8081",
  "dependencies": {
    "react": "^18.2.0",
    "react-dom": "^18.2.0",
    "react-scripts": "5.0.1"
  }
}

Step 2: Workflow Service Layer
Create react-bff-app/src/services/workflowService.js. By using relative paths, we ensure all traffic hits the Node Proxy first.

/**
 * workflowService.js
 * Handles all communication with the Orchestrator via Node Proxy.
 */
export const workflowService = {
  
  // Fetches cases based on status: '', 'running', or 'completed'
  fetchCases: async (status = '') => {
    const url = status ? `/workflow/cases?status=${status}` : '/workflow/cases';
    const response = await fetch(url);
    if (!response.ok) throw new Error('Network response was not ok');
    return response.json();
  },

  // Fetches the macro roadmap for a specific case
  fetchMacroView: async (lookupId) => {
    const response = await fetch(`/workflow/case/${lookupId}/macro`);
    if (!response.ok) throw new Error('Failed to fetch roadmap');
    return response.json();
  }
};

Step 3: Credit Intake Dashboard Component
Create react-bff-app/src/components/CreditDashboard.js. This provides the interactive table and status filters we defined in the backend.

import React, { useState, useEffect } from 'react';
import { workflowService } from '../services/workflowService';

const CreditDashboard = () => {
  const [cases, setCases] = useState([]);
  const [statusFilter, setStatusFilter] = useState(''); // Default: All
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    setIsLoading(true);
    workflowService.fetchCases(statusFilter)
      .then(data => {
        setCases(data);
        setIsLoading(false);
      })
      .catch(err => console.error("Error loading dashboard:", err));
  }, [statusFilter]);

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <header style={{ display: 'flex', justifyContent: 'space-pair', alignItems: 'center', marginBottom: '20px' }}>
        <h2>Commercial Credit Intake</h2>
        <div style={{ display: 'flex', gap: '10px' }}>
          <button onClick={() => setStatusFilter('')} style={btnStyle(statusFilter === '')}>All</button>
          <button onClick={() => setStatusFilter('running')} style={btnStyle(statusFilter === 'running')}>Active</button>
          <button onClick={() => setStatusFilter('completed')} style={btnStyle(statusFilter === 'completed')}>Completed</button>
        </div>
      </header>

      {isLoading ? (
        <p>Syncing with Orchestrator...</p>
      ) : (
        <table style={{ width: '100%', borderCollapse: 'collapse', marginTop: '10px' }}>
          <thead style={{ backgroundColor: '#f4f4f4' }}>
            <tr>
              <th style={tdStyle}>Business Key</th>
              <th style={tdStyle}>Status</th>
              <th style={tdStyle}>Action</th>
            </tr>
          </thead>
          <tbody>
            {cases.map(item => (
              <tr key={item.caseId}>
                <td style={tdStyle}>{item.businessKey || 'N/A'}</td>
                <td style={tdStyle}>
                  <span style={statusBadge(item.status)}>
                    {item.status}
                  </span>
                </td>
                <td style={tdStyle}>
                  <button style={{ color: '#007bff', border: 'none', background: 'none', cursor: 'pointer' }}>
                    View Roadmap
                  </button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      )}
    </div>
  );
};

// Simple inline styles for rapid CLI deployment
const tdStyle = { padding: '12px', border: '1px solid #ddd', textAlign: 'left' };
const btnStyle = (active) => ({
  padding: '8px 16px',
  cursor: 'pointer',
  backgroundColor: active ? '#007bff' : '#eee',
  color: active ? '#fff' : '#333',
  border: 'none',
  borderRadius: '4px'
});
const statusBadge = (status) => ({
  padding: '4px 8px',
  borderRadius: '12px',
  fontSize: '12px',
  fontWeight: 'bold',
  backgroundColor: status === 'RUNNING' ? '#e7f3ff' : '#e6ffed',
  color: status === 'RUNNING' ? '#007bff' : '#28a745'
});

export default CreditDashboard;


Step 4: Final Injection
Update react-bff-app/src/App.js to render the CreditDashboard.

import CreditDashboard from './components/CreditDashboard';

function App() {
  return (
    <div className="App">
       <CreditDashboard />
    </div>
  );
}

export default App;