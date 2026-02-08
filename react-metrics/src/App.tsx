import { useState, useEffect } from 'react'
import './App.css'

interface MetricsData {
  source: string
  type: string
  value: number
  status: string
}

function App() {
  const [data, setData] = useState<MetricsData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchMetrics = async () => {
      try {
        setLoading(true)
        setError(null)

        const response = await fetch('http://localhost:3001/v1/proxy/data/metrics', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
          },
        })

        if (!response.ok) {
          throw new Error(`HTTP error! status: ${response.status}`)
        }

        const jsonData = await response.json()
        setData(jsonData)
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to fetch metrics data'
        setError(errorMessage)
        console.error('Error fetching metrics:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchMetrics()
  }, [])

  const getStatusColor = (status: string) => {
    switch (status.toLowerCase()) {
      case 'healthy':
        return '#4ade80'
      case 'warning':
        return '#facc15'
      case 'critical':
        return '#ef4444'
      default:
        return '#60a5fa'
    }
  }

  return (
    <div className="metrics-container">
      <h2>Metrics Dashboard</h2>

      {loading && <div className="loading">Loading metrics data...</div>}

      {error && <div className="error">Error: {error}</div>}

      {data && (
        <div className="metrics-data">
          <div className="metric-card">
            <div className="metric-label">Data Source</div>
            <div className="metric-value">{data.source}</div>
          </div>

          <div className="metric-card">
            <div className="metric-label">Type</div>
            <div className="metric-value">{data.type}</div>
          </div>

          <div className="metric-card main-metric">
            <div className="metric-label">Current Value</div>
            <div className="metric-large-value">{data.value.toLocaleString()}</div>
          </div>

          <div className="metric-card status-card">
            <div className="metric-label">Status</div>
            <div
              className="metric-status"
              style={{ color: getStatusColor(data.status) }}
            >
              {data.status}
            </div>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
