import { create } from 'zustand';
import { User } from '../types';

const storedUser = localStorage.getItem('user');

let parsedUser: User | null = null;

try {
  if (storedUser && storedUser !== 'undefined') {
    parsedUser = JSON.parse(storedUser);
  }
} catch (error) {
  console.warn('Invalid user data found in localStorage. Clearing it.');
  localStorage.removeItem('user');
}

interface AuthState {
  accessToken: string | null;
  refreshToken: string | null;
  user: User | null;
  isAuthenticated: boolean;
  setTokens: (accessToken: string, refreshToken: string) => void;
  setUser: (user: User | null) => void;
  logout: () => void;
}

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: localStorage.getItem('accessToken'),
  refreshToken: localStorage.getItem('refreshToken'),
  user: parsedUser,
  isAuthenticated: !!localStorage.getItem('accessToken'),

  setTokens: (accessToken, refreshToken) => {
    localStorage.setItem('accessToken', accessToken);
    localStorage.setItem('refreshToken', refreshToken);
    set({ accessToken, refreshToken, isAuthenticated: true });
  },

  setUser: (user) => {
    if (user) {
      localStorage.setItem('user', JSON.stringify(user));
    } else {
      localStorage.removeItem('user');
    }
    set({ user });
  },

  logout: () => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('user');
    set({
      accessToken: null,
      refreshToken: null,
      user: null,
      isAuthenticated: false,
    });
  },
}));