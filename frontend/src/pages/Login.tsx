import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { useAuthStore } from '../stores/authStore';
import { authService } from '../services/authService';
import { useUiStore } from '../stores/uiStore';
import { Mail, Lock, Loader2 } from 'lucide-react';

export const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { setTokens, setUser } = useAuthStore();
  const { showToast } = useUiStore();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError(null);

    try {
      const data = await authService.login({ email, password });
      setTokens(data.accessToken, data.refreshToken);
      setUser({ id: data.userId || 'mock-id', email, role: 'USER' });
      showToast('Logged in successfully', 'success');
      navigate('/dashboard');
    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.message || 'Invalid email or password');
      showToast('Login failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-900 px-4">
      <div className="max-w-md w-full bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-2xl p-8 shadow-xl">
        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold tracking-tight">Sign In</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Access your engineering map workspace
          </p>
        </div>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-rose-50 dark:bg-rose-950/20 border border-rose-200 dark:border-rose-900 text-rose-800 dark:text-rose-200 text-xs font-semibold">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-xs font-bold uppercase tracking-wider block mb-1 opacity-70">
              Email Address
            </label>
            <div className="relative">
              <Mail className="absolute left-3 top-2.5 opacity-40" size={18} />
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="you@domain.com"
                className="w-full bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg pl-10 pr-4 py-2 text-sm focus:outline-none focus:border-brand-500"
              />
            </div>
          </div>

          <div>
            <label className="text-xs font-bold uppercase tracking-wider block mb-1 opacity-70">
              Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-2.5 opacity-40" size={18} />
              <input
                type="password"
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••"
                className="w-full bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg pl-10 pr-4 py-2 text-sm focus:outline-none focus:border-brand-500"
              />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-brand-600 hover:bg-brand-700 text-white font-semibold py-2 rounded-lg transition-colors flex items-center justify-center gap-2 disabled:opacity-50"
          >
            {loading ? <Loader2 className="animate-spin" size={18} /> : 'Login'}
          </button>
        </form>

        <div className="mt-6 text-center text-xs">
          <span className="opacity-60">Don't have an account? </span>
          <Link to="/register" className="text-brand-600 font-bold hover:underline">
            Sign Up
          </Link>
        </div>
      </div>
    </div>
  );
};
