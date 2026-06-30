import { create } from 'zustand';
import { Project } from '../types';

interface ProjectState {
  currentProject: Project | null;
  searchQuery: string;
  currentPage: number;
  sortBy: string;
  setCurrentProject: (project: Project | null) => void;
  setSearchQuery: (query: string) => void;
  setCurrentPage: (page: number) => void;
  setSortBy: (sortBy: string) => void;
}

export const useProjectStore = create<ProjectState>((set) => ({
  currentProject: null,
  searchQuery: '',
  currentPage: 0,
  sortBy: 'updatedAt',

  setCurrentProject: (currentProject) => set({ currentProject }),
  setSearchQuery: (searchQuery) => set({ searchQuery, currentPage: 0 }),
  setCurrentPage: (currentPage) => set({ currentPage }),
  setSortBy: (sortBy) => set({ sortBy }),
}));
