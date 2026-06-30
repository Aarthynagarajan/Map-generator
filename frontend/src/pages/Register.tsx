import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { authService } from '../services/authService';
import { useUiStore } from '../stores/uiStore';
import { Mail, Lock, Loader2 } from 'lucide-react';

export const Register = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const { showToast } = useUiStore();
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError(null);

    if (password !== confirmPassword) {
      setError('Passwords do not match');
      return;
    }

    setLoading(true);
    try {
      await authService.register({ email, password });
      showToast('Registration successful! Please login.', 'success');
      navigate('/login');
    } catch (err: any) {
      console.error(err);
      setError(err.response?.data?.message || 'Registration failed');
      showToast('Registration failed', 'error');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50 dark:bg-slate-900 px-4">
      <div className="max-w-md w-full bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-2xl p-8 shadow-xl">
        <div className="text-center mb-6">
          <h2 className="text-2xl font-bold tracking-tight">Create Account</h2>
          <p className="text-sm text-slate-500 dark:text-slate-400 mt-1">
            Start modeling technical system flow maps
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

          <div>
            <label className="text-xs font-bold uppercase tracking-wider block mb-1 opacity-70">
              Confirm Password
            </label>
            <div className="relative">
              <Lock className="absolute left-3 top-2.5 opacity-40" size={18} />
              <input
                type="password"
                required
                value={confirmPassword}
                onChange={(e) => setConfirmPassword(e.target.value)}
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
            {loading ? <Loader2 className="animate-spin" size={18} /> : 'Register'}
          </button>
        </form>

        <div className="mt-6 text-center text-xs">
          <span className="opacity-60">Already have an account? </span>
          <Link to="/login" className="text-brand-600 font-bold hover:underline">
            Sign In
          </Link>
        </div>
      </div>
    </div>
  );
};
