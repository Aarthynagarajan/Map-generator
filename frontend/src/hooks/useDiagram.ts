import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { diagramService } from '../services/diagramService';
import { useUiStore } from '../stores/uiStore';

export function useDiagram(id: string | undefined) {
  return useQuery({
    queryKey: ['diagram', id],
    queryFn: () => diagramService.getDiagram(id!),
    enabled: !!id,
  });
}

export function useHistory(id: string | undefined) {
  return useQuery({
    queryKey: ['history', id],
    queryFn: () => diagramService.getHistory(id!),
    enabled: !!id,
  });
}

export function useScenarios(diagramId: string | undefined) {
  return useQuery({
    queryKey: ['scenarios', diagramId],
    queryFn: () => diagramService.getScenarios(diagramId!),
    enabled: !!diagramId,
  });
}

export function useCreateScenario(diagramId: string | undefined) {
  const queryClient = useQueryClient();
  const { showToast } = useUiStore();

  return useMutation({
    mutationFn: (scenario: { name: string; stopperStates: Record<string, string>; isDefault?: boolean }) =>
      diagramService.createScenario(diagramId!, scenario),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['scenarios', diagramId] });
      showToast('Scenario created successfully', 'success');
    },
    onError: (err: any) => {
      showToast(err.response?.data?.message || 'Failed to create scenario', 'error');
    },
  });
}

export function useUpdateScenario(diagramId: string | undefined) {
  const queryClient = useQueryClient();
  const { showToast } = useUiStore();

  return useMutation({
    mutationFn: ({ id, details }: { id: string; details: any }) =>
      diagramService.updateScenario(id, details),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['scenarios', diagramId] });
      showToast('Scenario updated successfully', 'success');
    },
    onError: (err: any) => {
      showToast(err.response?.data?.message || 'Failed to update scenario', 'error');
    },
  });
}

export function useDeleteScenario(diagramId: string | undefined) {
  const queryClient = useQueryClient();
  const { showToast } = useUiStore();

  return useMutation({
    mutationFn: diagramService.deleteScenario,
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['scenarios', diagramId] });
      showToast('Scenario deleted successfully', 'success');
    },
    onError: (err: any) => {
      showToast(err.response?.data?.message || 'Failed to delete scenario', 'error');
    },
  });
}

export function useSymbols(domain: string | undefined) {
  return useQuery({
    queryKey: ['symbols', domain],
    queryFn: () => diagramService.getSymbols(domain!),
    enabled: !!domain,
  });
}
