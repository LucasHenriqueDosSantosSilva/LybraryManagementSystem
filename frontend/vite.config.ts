import { defineConfig } from 'vitest/config';
import react from '@vitejs/plugin-react';
export default defineConfig({ plugins: [react()], server: { proxy: { '/api': process.env.LIBRARY_API_TARGET || 'http://127.0.0.1:8080' } }, test: { environment: 'jsdom', exclude: ['e2e/**', 'node_modules/**'], restoreMocks: true } });
