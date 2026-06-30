import apiClient from './apiClient';
import { Diagram, Scenario, SymbolRegistryItem } from '../types';

export const diagramService = {
  getDiagram: async (id: string) => {
    const response = await apiClient.get<any>(`/api/v1/diagrams/${id}`);
    return response.data.data as Diagram;
  },

  updateDiagram: async (id: string, graphDelta: any) => {
    const response = await apiClient.patch<any>(`/api/v1/diagrams/${id}`, graphDelta);
    return response.data.data as Diagram;
  },

  getHistory: async (id: string) => {
    const response = await apiClient.get<any>(`/api/v1/diagrams/${id}/history`);
    return response.data.data as Diagram[];
  },

  restoreVersion: async (id: string, version: number) => {
    const response = await apiClient.post<any>(`/api/v1/diagrams/${id}/history/restore`, null, {
      params: { version },
    });
    return response.data.data as Diagram;
  },

  getScenarios: async (diagramId: string) => {
    const response = await apiClient.get<any>(`/api/v1/diagrams/${diagramId}/scenarios`);
    return response.data.data as Scenario[];
  },

  createScenario: async (diagramId: string, scenario: { name: string; stopperStates: Record<string, string>; isDefault?: boolean }) => {
    const response = await apiClient.post<any>(`/api/v1/diagrams/${diagramId}/scenarios`, scenario);
    return response.data.data as Scenario;
  },

  updateScenario: async (id: string, scenario: { name: string; stopperStates: Record<string, string>; isDefault?: boolean }) => {
    const response = await apiClient.patch<any>(`/api/v1/scenarios/${id}`, scenario);
    return response.data.data as Scenario;
  },

  deleteScenario: async (id: string) => {
    await apiClient.delete(`/api/v1/scenarios/${id}`);
  },

  getSymbols: async (domain: string) => {
    const response = await apiClient.get<any>('/api/v1/symbols', {
      params: { domain },
    });
    return response.data.data as SymbolRegistryItem[];
  },
};
