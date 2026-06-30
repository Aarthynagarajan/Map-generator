import { create } from 'zustand';

export type GenStage = 'idle' | 'parsing' | 'symbol_mapping' | 'layout' | 'complete' | 'error';

interface GenerationState {
  stage: GenStage;
  percentage: number;
  errorMessage: string | null;
  diagramId: string | null;
  
  startGen: () => void;
  updateProgress: (stage: GenStage, percentage: number) => void;
  setComplete: (diagramId: string) => void;
  setError: (message: string) => void;
  resetGen: () => void;
}

export const useGenerationStore = create<GenerationState>((set) => ({
  stage: 'idle',
  percentage: 0,
  errorMessage: null,
  diagramId: null,

  startGen: () => set({ stage: 'parsing', percentage: 20, errorMessage: null, diagramId: null }),
  
  updateProgress: (stage, percentage) => set({ stage, percentage }),
  
  setComplete: (diagramId) => set({ stage: 'complete', percentage: 100, diagramId }),
  
  setError: (errorMessage) => set({ stage: 'error', errorMessage }),
  
  resetGen: () => set({ stage: 'idle', percentage: 0, errorMessage: null, diagramId: null }),
}));
