import { useEffect, useRef } from 'react';
import { useDiagramStore } from '../stores/diagramStore';
import { diagramService } from '../services/diagramService';
import { useUiStore } from '../stores/uiStore';

export function useAutoSave(diagramId: string | undefined) {
  const { nodes, edges, isDirty, clearDirty } = useDiagramStore();
  const { showToast } = useUiStore();
  const timerRef = useRef<NodeJS.Timeout | null>(null);

  useEffect(() => {
    if (!diagramId || !isDirty) return;

    if (timerRef.current) {
      clearTimeout(timerRef.current);
    }

    timerRef.current = setTimeout(async () => {
      try {
        const nodesPayload = Object.values(nodes).map(n => ({
          id: n.id,
          x: n.x,
          y: n.y,
          label: n.label,
          state: n.state
        }));

        await diagramService.updateDiagram(diagramId, { nodes: nodesPayload });
        clearDirty();
        showToast('Auto-saved changes', 'success');
      } catch (error) {
        console.error('Auto-save failed', error);
        showToast('Auto-save failed', 'error');
      }
    }, 1000); // 1-second debounce

    return () => {
      if (timerRef.current) {
        clearTimeout(timerRef.current);
      }
    };
  }, [nodes, edges, isDirty, diagramId]);
}
