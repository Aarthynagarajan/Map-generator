import { create } from 'zustand';

export interface Toast {
  id: string;
  message: string;
  type: 'success' | 'error' | 'info' | 'warning';
  duration?: number;
}

export type Theme = 'light' | 'dark' | 'system';

const initialTheme = (localStorage.getItem('theme') as Theme) || 'system';
if (typeof window !== 'undefined') {
  const root = window.document.documentElement;
  root.classList.remove('light', 'dark');
  if (initialTheme === 'system') {
    const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
    root.classList.add(systemTheme);
  } else {
    root.classList.add(initialTheme);
  }
}

interface UiState {
  theme: Theme;
  toasts: Toast[];
  sidebarOpen: boolean;
  activeTab: string;
  showToast: (message: string, type: Toast['type'], duration?: number) => void;
  dismissToast: (id: string) => void;
  setTheme: (theme: Theme) => void;
  setSidebarOpen: (open: boolean) => void;
  setActiveTab: (tab: string) => void;
}

export const useUiStore = create<UiState>((set) => ({
  theme: initialTheme,
  toasts: [],
  sidebarOpen: true,
  activeTab: 'generator',

  showToast: (message, type, duration = 3000) => {
    const id = Math.random().toString(36).substring(2, 9);
    set((state) => ({
      toasts: [...state.toasts, { id, message, type, duration }],
    }));
    setTimeout(() => {
      set((state) => ({
        toasts: state.toasts.filter((t) => t.id !== id),
      }));
    }, duration);
  },

  dismissToast: (id) =>
    set((state) => ({
      toasts: state.toasts.filter((t) => t.id !== id),
    })),

  setTheme: (theme) => {
    localStorage.setItem('theme', theme);
    const root = window.document.documentElement;
    root.classList.remove('light', 'dark');

    if (theme === 'system') {
      const systemTheme = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light';
      root.classList.add(systemTheme);
    } else {
      root.classList.add(theme);
    }
    set({ theme });
  },

  setSidebarOpen: (sidebarOpen) => set({ sidebarOpen }),
  setActiveTab: (activeTab) => set({ activeTab }),
}));
