import { Component, ErrorInfo, ReactNode } from 'react';
import { AlertOctagon } from 'lucide-react';

interface Props {
  children?: ReactNode;
}

interface State {
  hasError: boolean;
  error: Error | null;
}

export class ErrorBoundary extends Component<Props, State> {
  public state: State = {
    hasError: false,
    error: null,
  };

  public static getDerivedStateFromError(error: Error): State {
    return { hasError: true, error };
  }

  public componentDidCatch(error: Error, errorInfo: ErrorInfo) {
    console.error('ErrorBoundary caught an error:', error, errorInfo);
  }

  public render() {
    if (this.state.hasError) {
      return (
        <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 flex flex-col items-center justify-center p-6 text-center">
          <AlertOctagon size={48} className="text-rose-500 mb-3" />
          <h2 className="text-xl font-bold tracking-tight">Application Error</h2>
          <p className="text-xs opacity-60 mt-1 max-w-sm truncate">
            {this.state.error?.message || 'An unexpected runtime error occurred.'}
          </p>
          <button
            onClick={() => window.location.reload()}
            className="mt-6 bg-brand-600 hover:bg-brand-700 text-white font-semibold px-4 py-2 rounded-lg text-sm"
          >
            Reload Application
          </button>
        </div>
      );
    }

    return this.props.children;
  }
}
