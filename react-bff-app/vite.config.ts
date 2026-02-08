import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import federation from '@originjs/vite-plugin-federation'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'react_bff_app',
      remotes: {
        react_metrics: 'http://localhost:5177/assets/remoteEntry.js',
        react_analytics: 'http://localhost:5178/assets/remoteEntry.js',
      },
      shared: ['react', 'react-dom'],
    }),
  ],
  server: {
    port: 5173,
  },
  build: {
    target: 'esnext',
  },
})
