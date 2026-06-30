import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { projectService } from '../services/projectService';
import { useUiStore } from '../stores/uiStore';

export function useProjects(page = 0, size = 10, search = '', sortBy = 'updatedAt') {
  return useQuery({
    queryKey: ['projects', page, size, search, sortBy],
    queryFn: () => projectService.getProjects(page, size, search, sortBy),
    placeholderData: (prev) => prev,
  });
}

export function useProject(id: string | undefined) {
  return useQuery({
    queryKey: ['project', id],
    queryFn: () => projectService.getProject(id!),
    enabled: !!id,
  });
}

export function useCreateProject() {
  const queryClient = useQueryClient();
  const { showToast } = useUiStore();

  return useMutation({
    mutationFn: projectService.createProject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      showToast('Project created successfully', 'success');
    },
    onError: (err: any) => {
      showToast(err.response?.data?.message || 'Failed to create project', 'error');
    },
  });
}

export function useUpdateProject() {
  const queryClient = useQueryClient();
  const { showToast } = useUiStore();

  return useMutation({
    mutationFn: ({ id, details }: { id: string; details: any }) =>
      projectService.updateProject(id, details),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      queryClient.invalidateQueries({ queryKey: ['project', data.id] });
      showToast('Project updated successfully', 'success');
    },
    onError: (err: any) => {
      showToast(err.response?.data?.message || 'Failed to update project', 'error');
    },
  });
}

export function useDeleteProject() {
  const queryClient = useQueryClient();
  const { showToast } = useUiStore();

  return useMutation({
    mutationFn: projectService.deleteProject,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['projects'] });
      showToast('Project deleted successfully', 'success');
    },
    onError: (err: any) => {
      showToast(err.response?.data?.message || 'Failed to delete project', 'error');
    },
  });
}
