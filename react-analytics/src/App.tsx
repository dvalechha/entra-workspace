import { useState, useEffect } from 'react'
import './App.css'

interface AnalyticsData {
  source: string
  type: string
  growth: string
  users: number
}

function App() {
  const [data, setData] = useState<AnalyticsData | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)

  useEffect(() => {
    const fetchAnalytics = async () => {
      try {
        setLoading(true)
        setError(null)

        const response = await fetch('http://localhost:3001/v1/proxy/data/analytics', {
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
        const errorMessage = err instanceof Error ? err.message : 'Failed to fetch analytics data'
        setError(errorMessage)
        console.error('Error fetching analytics:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchAnalytics()
  }, [])

  return (
    <div className="analytics-container">
      <h2>Analytics Dashboard</h2>

      {loading && <div className="loading">Loading analytics data...</div>}

      {error && <div className="error">Error: {error}</div>}

      {data && (
        <div className="analytics-data">
          <div className="data-section">
            <h3>Data Source</h3>
            <p>{data.source}</p>
          </div>

          <div className="data-section">
            <h3>Type</h3>
            <p>{data.type}</p>
          </div>

          <div className="data-section">
            <h3>Growth</h3>
            <p className="growth-value">{data.growth}</p>
          </div>

          <div className="data-section">
            <h3>Users</h3>
            <p className="users-value">{data.users.toLocaleString()}</p>
          </div>
        </div>
      )}
    </div>
  )
}

export default App
