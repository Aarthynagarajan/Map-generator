import apiClient from './apiClient';
import { Project } from '../types';

export interface ProjectListResponse {
  content: Project[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

export const projectService = {
  getProjects: async (page = 0, size = 10, search = '', sortBy = 'updatedAt') => {
    const response = await apiClient.get<any>('/api/v1/projects', {
      params: { page, size, search, sortBy },
    });
    return response.data.data as ProjectListResponse;
  },

  getProject: async (id: string) => {
    const response = await apiClient.get<any>(`/api/v1/projects/${id}`);
    return response.data.data as Project;
  },

  createProject: async (project: { name: string; description?: string }) => {
    const response = await apiClient.post<any>('/api/v1/projects', project);
    return response.data.data as Project;
  },

  updateProject: async (id: string, project: { name: string; description?: string }) => {
    const response = await apiClient.patch<any>(`/api/v1/projects/${id}`, project);
    return response.data.data as Project;
  },

  deleteProject: async (id: string) => {
    await apiClient.delete(`/api/v1/projects/${id}`);
  },
};
