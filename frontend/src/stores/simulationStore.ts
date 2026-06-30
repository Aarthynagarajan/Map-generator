import { create } from 'zustand';

interface SimulationState {
  isPlaying: boolean;
  speed: number; // multiplier e.g. 1, 2, 5
  scenarioId: string | null;
  activePaths: Set<string>;
  
  setPlaying: (isPlaying: boolean) => void;
  setSpeed: (speed: number) => void;
  setScenarioId: (scenarioId: string | null) => void;
  setActivePaths: (paths: Set<string>) => void;
  resetSimulation: () => void;
}

export const useSimulationStore = create<SimulationState>((set) => ({
  isPlaying: false,
  speed: 1,
  scenarioId: null,
  activePaths: new Set(),

  setPlaying: (isPlaying) => set({ isPlaying }),
  setSpeed: (speed) => set({ speed }),
  setScenarioId: (scenarioId) => set({ scenarioId }),
  setActivePaths: (activePaths) => set({ activePaths }),
  resetSimulation: () => set({ isPlaying: false, speed: 1, scenarioId: null, activePaths: new Set() }),
}));
