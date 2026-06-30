import { useNavigate } from 'react-router-dom';
import { HelpCircle } from 'lucide-react';

export const NotFound = () => {
  const navigate = useNavigate();

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 flex flex-col items-center justify-center p-6 text-center">
      <HelpCircle size={48} className="text-amber-500 opacity-80 mb-3 animate-bounce" />
      <h2 className="text-2xl font-bold tracking-tight">404 — Page Not Found</h2>
      <p className="text-sm opacity-60 mt-1 max-w-sm">
        The workspace path or project you are looking for does not exist.
      </p>
      <button
        onClick={() => navigate('/dashboard')}
        className="mt-6 bg-brand-600 hover:bg-brand-700 text-white font-semibold px-4 py-2 rounded-lg text-sm shadow-md"
      >
        Go to Dashboard
      </button>
    </div>
  );
};
