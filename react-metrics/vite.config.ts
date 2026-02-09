import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import federation from '@originjs/vite-plugin-federation'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    react(),
    federation({
      name: 'react_metrics',
      filename: 'remoteEntry.js',
      exposes: {
        './App': './src/App.tsx',
      },
      shared: ['react', 'react-dom'],
    }),
  ],
  server: {
    port: 5178,
    strictPort: true,
    middlewareMode: false,
  },
  preview: {
    port: 5178,
    strictPort: true,
  },
  build: {
    target: 'esnext',
    minify: false,
  },
})
