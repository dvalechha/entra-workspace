import { useState, useEffect, Suspense, lazy } from 'react'
import './App.css'
import { menuConfig } from './menuConfig'

interface UserInfo {
  name: string
  roles: string[]
}

// Lazy load remote MFE components
const MetricsApp = lazy(() => import('react_metrics/App'))
const AnalyticsApp = lazy(() => import('react_analytics/App'))

function App() {
  const [user, setUser] = useState<UserInfo | null>(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState<string | null>(null)
  const [selectedMenu, setSelectedMenu] = useState<string | null>(null)

  // Fetch user info and roles on component mount
  useEffect(() => {
    const fetchUserInfo = async () => {
      try {
        setLoading(true)
        setError(null)

        const response = await fetch('http://localhost:3001/v1/auth/me', {
          method: 'GET',
          credentials: 'include',
          headers: {
            'Content-Type': 'application/json',
          },
        })

        if (!response.ok) {
          if (response.status === 401 || response.status === 403) {
            setError(null)
            setUser(null)
          } else {
            throw new Error(`HTTP error! status: ${response.status}`)
          }
          return
        }

        const userInfo = await response.json()
        setUser(userInfo)

        // Auto-select first available menu item
        const availableMenu = menuConfig.find(item =>
          userInfo.roles.includes(item.role)
        )
        if (availableMenu) {
          setSelectedMenu(availableMenu.id)
        }
      } catch (err) {
        const errorMessage = err instanceof Error ? err.message : 'Failed to fetch user info'
        setError(errorMessage)
        console.error('Error fetching user info:', err)
      } finally {
        setLoading(false)
      }
    }

    fetchUserInfo()
  }, [])

  // Filter menu items based on user roles
  const filteredMenu = user
    ? menuConfig.filter(item => user.roles.includes(item.role))
    : []

  // Handle login redirect
  const handleLogin = async () => {
    try {
      const response = await fetch('http://localhost:3001/v1/auth/session/codeUrl', {
        method: 'GET',
        credentials: 'include',
      })

      if (!response.ok) {
        throw new Error('Failed to get auth code URL')
      }

      const { url } = await response.json()
      window.location.href = url
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Login failed'
      setError(errorMessage)
      console.error('Error during login:', err)
    }
  }

  // Handle logout
  const handleLogout = async () => {
    try {
      const response = await fetch('http://localhost:3001/v1/auth/session/clear', {
        method: 'POST',
        credentials: 'include',
      })

      if (!response.ok) {
        throw new Error(`Logout failed with status: ${response.status}`)
      }

      // Clear local state
      setUser(null)
      setSelectedMenu(null)
      setError(null)

      // Reload page to ensure clean state
      window.location.href = '/'
    } catch (err) {
      const errorMessage = err instanceof Error ? err.message : 'Logout failed'
      setError(errorMessage)
      console.error('Error during logout:', err)
    }
  }

  if (loading) {
    return (
      <div className="app-container">
        <div className="loading-container">
          <div className="loading">Loading...</div>
        </div>
      </div>
    )
  }

  if (!user) {
    return (
      <div className="app-container">
        <div className="auth-container">
          <div className="auth-card">
            <h1>Entra ID BFF Shell</h1>
            <p className="auth-subtitle">Composable Micro-Frontend Architecture</p>
            {error && <div className="error">{error}</div>}
            <button className="login-button" onClick={handleLogin}>
              Login with Entra ID
            </button>
          </div>
        </div>
      </div>
    )
  }

  return (
    <div className="app-container">
      {/* Header */}
      <div className="app-header">
        <div className="header-left">
          <h1>Dashboard</h1>
        </div>
        <div className="header-right">
          <span className="user-name">Hello, {user.name}</span>
          <button className="logout-button" onClick={handleLogout}>
            Logout
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="app-content">
        {/* Left Sidebar - Menu */}
        <aside className="sidebar">
          <nav className="menu">
            <div className="menu-title">Navigation</div>
            {filteredMenu.length > 0 ? (
              <ul className="menu-list">
                {filteredMenu.map(item => (
                  <li key={item.id}>
                    <button
                      className={`menu-item ${selectedMenu === item.id ? 'active' : ''}`}
                      onClick={() => setSelectedMenu(item.id)}
                    >
                      {item.label}
                    </button>
                  </li>
                ))}
              </ul>
            ) : (
              <div className="menu-empty">No items available for your roles</div>
            )}
          </nav>
        </aside>

        {/* Right Content Area */}
        <main className="content">
          {error && <div className="error">{error}</div>}

          {selectedMenu === 'metrics' && (
            <Suspense fallback={<div className="loading-spinner">Loading Metrics...</div>}>
              <MetricsApp />
            </Suspense>
          )}

          {selectedMenu === 'analytics' && (
            <Suspense fallback={<div className="loading-spinner">Loading Analytics...</div>}>
              <AnalyticsApp />
            </Suspense>
          )}

          {!selectedMenu && filteredMenu.length > 0 && (
            <div className="welcome-message">
              <p>Select an item from the menu to begin</p>
            </div>
          )}
        </main>
      </div>
    </div>
  )
}

export default App
