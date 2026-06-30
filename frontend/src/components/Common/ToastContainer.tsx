import { useUiStore } from '../../stores/uiStore';
import { X, CheckCircle, AlertTriangle, Info, AlertOctagon } from 'lucide-react';

export const ToastContainer = () => {
  const { toasts, dismissToast } = useUiStore();

  if (toasts.length === 0) return null;

  return (
    <div className="fixed bottom-4 right-4 z-50 flex flex-col gap-2 max-w-sm w-full">
      {toasts.map((toast) => {
        let icon = <Info size={18} />;
        let bgColor = 'bg-blue-50 border-blue-200 dark:bg-blue-950/20 dark:border-blue-900 text-blue-800 dark:text-blue-200';

        if (toast.type === 'success') {
          icon = <CheckCircle size={18} />;
          bgColor = 'bg-emerald-50 border-emerald-200 dark:bg-emerald-950/20 dark:border-emerald-900 text-emerald-800 dark:text-emerald-200';
        } else if (toast.type === 'error') {
          icon = <AlertOctagon size={18} />;
          bgColor = 'bg-rose-50 border-rose-200 dark:bg-rose-950/20 dark:border-rose-900 text-rose-800 dark:text-rose-200';
        } else if (toast.type === 'warning') {
          icon = <AlertTriangle size={18} />;
          bgColor = 'bg-amber-50 border-amber-200 dark:bg-amber-950/20 dark:border-amber-900 text-amber-800 dark:text-amber-200';
        }

        return (
          <div
            key={toast.id}
            className={`flex items-center justify-between gap-3 px-4 py-3 rounded-lg border shadow-lg animate-slide-in ${bgColor}`}
          >
            <div className="flex items-center gap-2 text-sm font-medium">
              {icon}
              <span>{toast.message}</span>
            </div>
            <button
              onClick={() => dismissToast(toast.id)}
              className="opacity-60 hover:opacity-100 transition-opacity"
            >
              <X size={14} />
            </button>
          </div>
        );
      })}
    </div>
  );
};
