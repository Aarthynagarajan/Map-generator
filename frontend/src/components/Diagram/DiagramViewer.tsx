import { useMemo } from 'react';
import { ReactFlow, MiniMap, Controls, Background } from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import { useDiagramStore } from '../../stores/diagramStore';
import { useSimulationStore } from '../../stores/simulationStore';
import { ProcessNode } from './ProcessNode';
import { ProcessEdge } from './ProcessEdge';

const nodeTypes = {
  processNode: ProcessNode as any,
};

const edgeTypes = {
  processEdge: ProcessEdge as any,
};

export const DiagramViewer = () => {
  const { nodes, edges } = useDiagramStore();
  const { activePaths } = useSimulationStore();

  const flowNodes = useMemo(() => {
    return Object.values(nodes).map((n) => ({
      id: n.id,
      type: 'processNode',
      position: { x: n.x, y: n.y },
      data: n as any,
    }));
  }, [nodes]);

  const flowEdges = useMemo(() => {
    return Object.values(edges).map((e) => ({
      id: e.id,
      source: e.from,
      target: e.to,
      type: 'processEdge',
      data: {
        ...e,
        isActive: activePaths.has(e.id),
      } as any,
    }));
  }, [edges, activePaths]);

  return (
    <div className="w-full flex-1 min-h-[500px] border border-slate-200 dark:border-slate-800 rounded-lg overflow-hidden flex flex-col bg-slate-50 dark:bg-slate-900">
      <div className="flex-1 h-full relative flex flex-col">
        <ReactFlow
          nodes={flowNodes}
          edges={flowEdges}
          nodeTypes={nodeTypes}
          edgeTypes={edgeTypes}
          fitView
          nodesDraggable={false}
          nodesConnectable={false}
          elementsSelectable={false}
        >
          <Controls />
          <MiniMap />
          <Background color="#cbd5e1" gap={16} />
        </ReactFlow>
      </div>
    </div>
  );
};
