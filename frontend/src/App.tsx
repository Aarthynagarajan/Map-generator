import { Suspense, lazy } from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { ErrorBoundary } from './components/Common/ErrorBoundary';
import { ToastContainer } from './components/Common/ToastContainer';
import { ProtectedRoute } from './components/Common/ProtectedRoute';
import { Loader2 } from 'lucide-react';

// Lazy loading pages
const Login = lazy(() => import('./pages/Login').then(module => ({ default: module.Login })));
const Register = lazy(() => import('./pages/Register').then(module => ({ default: module.Register })));
const Dashboard = lazy(() => import('./pages/Dashboard').then(module => ({ default: module.Dashboard })));
const Workspace = lazy(() => import('./pages/Workspace').then(module => ({ default: module.Workspace })));
const NotFound = lazy(() => import('./pages/NotFound').then(module => ({ default: module.NotFound })));

// Loading skeleton fallback
const LoadingPage = () => (
  <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 flex items-center justify-center">
    <div className="flex flex-col items-center gap-2">
      <Loader2 className="animate-spin text-brand-600" size={32} />
      <span className="text-sm font-semibold opacity-70">Loading workspace...</span>
    </div>
  </div>
);

export const App = () => {
  return (
    <ErrorBoundary>
      <BrowserRouter>
        <Suspense fallback={<LoadingPage />}>
          <Routes>
            <Route path="/" element={<Navigate to="/dashboard" replace />} />
            <Route path="/login" element={<Login />} />
            <Route path="/register" element={<Register />} />
            
            <Route
              path="/dashboard"
              element={
                <ProtectedRoute>
                  <Dashboard />
                </ProtectedRoute>
              }
            />
            <Route
              path="/project/:id"
              element={
                <ProtectedRoute>
                  <Workspace />
                </ProtectedRoute>
              }
            />

            <Route path="*" element={<NotFound />} />
          </Routes>
        </Suspense>
      </BrowserRouter>
      <ToastContainer />
    </ErrorBoundary>
  );
};

export default App;
