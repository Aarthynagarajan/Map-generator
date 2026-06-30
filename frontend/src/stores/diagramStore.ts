import { create } from 'zustand';
import { Diagram, TypedNode, TypedEdge } from '../types';

interface HistoryEntry {
  nodes: Record<string, TypedNode>;
  edges: Record<string, TypedEdge>;
}

interface DiagramState {
  currentDiagram: Diagram | null;
  nodes: Record<string, TypedNode>;
  edges: Record<string, TypedEdge>;
  undoStack: HistoryEntry[];
  redoStack: HistoryEntry[];
  isDirty: boolean;
  
  setDiagram: (diagram: Diagram | null) => void;
  updateNodePosition: (id: string, x: number, y: number) => void;
  renameNode: (id: string, newLabel: string) => void;
  deleteNode: (id: string) => void;
  toggleNodeState: (id: string) => void;
  addHistoryEntry: () => void;
  undo: () => void;
  redo: () => void;
  clearDirty: () => void;
}

export const useDiagramStore = create<DiagramState>((set, get) => ({
  currentDiagram: null,
  nodes: {},
  edges: {},
  undoStack: [],
  redoStack: [],
  isDirty: false,

  setDiagram: (diagram) => {
    if (!diagram) {
      set({ currentDiagram: null, nodes: {}, edges: {}, undoStack: [], redoStack: [], isDirty: false });
      return;
    }
    set({
      currentDiagram: diagram,
      nodes: { ...diagram.graphSnapshot.nodes },
      edges: { ...diagram.graphSnapshot.edges },
      undoStack: [],
      redoStack: [],
      isDirty: false,
    });
  },

  updateNodePosition: (id, x, y) => {
    const { nodes } = get();
    if (!nodes[id]) return;

    get().addHistoryEntry();
    const updatedNodes = {
      ...nodes,
      [id]: {
        ...nodes[id],
        x,
        y,
      },
    };

    set({ nodes: updatedNodes, isDirty: true });
  },

  renameNode: (id, newLabel) => {
    const { nodes } = get();
    if (!nodes[id]) return;

    get().addHistoryEntry();
    const updatedNodes = {
      ...nodes,
      [id]: {
        ...nodes[id],
        label: newLabel,
      },
    };

    set({ nodes: updatedNodes, isDirty: true });
  },

  deleteNode: (id) => {
    const { nodes, edges } = get();
    if (!nodes[id]) return;

    get().addHistoryEntry();
    const updatedNodes = { ...nodes };
    delete updatedNodes[id];

    // Remove connected edges
    const updatedEdges = { ...edges };
    Object.keys(updatedEdges).forEach((edgeId) => {
      if (updatedEdges[edgeId].from === id || updatedEdges[edgeId].to === id) {
        delete updatedEdges[edgeId];
      }
    });

    set({ nodes: updatedNodes, edges: updatedEdges, isDirty: true });
  },

  toggleNodeState: (id) => {
    const { nodes } = get();
    if (!nodes[id]) return;

    get().addHistoryEntry();
    const currentState = nodes[id].state;
    const entityClass = nodes[id].entityClass || '';
    const newState = currentState === 'open' || currentState === 'on' 
        ? (entityClass.includes('BREAKER') || entityClass.includes('SWITCH') ? 'off' : 'closed')
        : (entityClass.includes('BREAKER') || entityClass.includes('SWITCH') ? 'on' : 'open');

    const updatedNodes = {
      ...nodes,
      [id]: {
        ...nodes[id],
        state: newState,
      },
    };

    set({ nodes: updatedNodes, isDirty: true });
  },

  addHistoryEntry: () => {
    const { nodes, edges, undoStack } = get();
    // Keep max 20 entries
    const newUndo = [...undoStack, { nodes: JSON.parse(JSON.stringify(nodes)), edges: JSON.parse(JSON.stringify(edges)) }];
    if (newUndo.length > 20) newUndo.shift();
    set({ undoStack: newUndo, redoStack: [] });
  },

  undo: () => {
    const { undoStack, redoStack, nodes, edges } = get();
    if (undoStack.length === 0) return;

    const previous = undoStack[undoStack.length - 1];
    const newUndo = undoStack.slice(0, -1);
    const newRedo = [...redoStack, { nodes: JSON.parse(JSON.stringify(nodes)), edges: JSON.parse(JSON.stringify(edges)) }];

    set({
      nodes: previous.nodes,
      edges: previous.edges,
      undoStack: newUndo,
      redoStack: newRedo,
      isDirty: true,
    });
  },

  redo: () => {
    const { undoStack, redoStack, nodes, edges } = get();
    if (redoStack.length === 0) return;

    const next = redoStack[redoStack.length - 1];
    const newRedo = redoStack.slice(0, -1);
    const newUndo = [...undoStack, { nodes: JSON.parse(JSON.stringify(nodes)), edges: JSON.parse(JSON.stringify(edges)) }];

    set({
      nodes: next.nodes,
      edges: next.edges,
      undoStack: newUndo,
      redoStack: newRedo,
      isDirty: true,
    });
  },

  clearDirty: () => set({ isDirty: false }),
}));
