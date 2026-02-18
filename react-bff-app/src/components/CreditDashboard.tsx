import React, { useState, useEffect } from 'react';
import { workflowService, type Case } from '../services/workflowService';

const CreditDashboard: React.FC = () => {
  const [cases, setCases] = useState<Case[]>([]);
  const [statusFilter, setStatusFilter] = useState<string>(''); // Default: All
  const [isLoading, setIsLoading] = useState<boolean>(true);

  useEffect(() => {
    setIsLoading(true);
    workflowService.fetchCases(statusFilter)
      .then(data => {
        setCases(data);
        setIsLoading(false);
      })
      .catch(err => {
        console.error("Error loading dashboard:", err);
        setIsLoading(false);
      });
  }, [statusFilter]);

  return (
    <div style={{ padding: '20px', fontFamily: 'sans-serif' }}>
      <header style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '20px' }}>
        <h2 style={{ margin: 0 }}>Commercial Credit Intake</h2>
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
            {cases.length > 0 ? (
              cases.map(item => (
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
              ))
            ) : (
              <tr>
                <td colSpan={3} style={{ ...tdStyle, textAlign: 'center' }}>No cases found</td>
              </tr>
            )}
          </tbody>
        </table>
      )}
    </div>
  );
};

// Simple inline styles for rapid CLI deployment
const tdStyle: React.CSSProperties = { padding: '12px', border: '1px solid #ddd', textAlign: 'left' };
const btnStyle = (active: boolean): React.CSSProperties => ({
  padding: '8px 16px',
  cursor: 'pointer',
  backgroundColor: active ? '#007bff' : '#eee',
  color: active ? '#fff' : '#333',
  border: 'none',
  borderRadius: '4px'
});
const statusBadge = (status: string): React.CSSProperties => ({
  padding: '4px 8px',
  borderRadius: '12px',
  fontSize: '12px',
  fontWeight: 'bold',
  backgroundColor: status === 'RUNNING' ? '#e7f3ff' : '#e6ffed',
  color: status === 'RUNNING' ? '#007bff' : '#28a745'
});

export default CreditDashboard;
