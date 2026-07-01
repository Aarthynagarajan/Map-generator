import { useMemo, useEffect, useLayoutEffect, useRef, useState } from 'react';
import { ReactFlow, MiniMap, Controls, Background, useReactFlow, ReactFlowProvider } from '@xyflow/react';
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

const DiagramViewerInner = () => {
  const { nodes, edges } = useDiagramStore();
  const { activePaths } = useSimulationStore();
  const { fitView } = useReactFlow();
  const containerRef = useRef<HTMLDivElement>(null);
  const [ready, setReady] = useState(false);

  useLayoutEffect(() => {
    const observer = new ResizeObserver(() => {
      const el = containerRef.current;
      if (el && el.clientWidth > 0 && el.clientHeight > 0) {
        setReady(true);
        observer.disconnect();
      }
    });

    if (containerRef.current) {
      observer.observe(containerRef.current);
    }

    return () => observer.disconnect();
  }, []);

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

  useEffect(() => {
    if (flowNodes.length > 0) {
      const timer = setTimeout(() => {
        fitView({ duration: 200 });
      }, 100);
      return () => clearTimeout(timer);
    }
  }, [flowNodes, fitView]);

  return (
    <div className="w-full flex-1 min-h-[500px] border border-slate-200 dark:border-slate-800 rounded-lg overflow-hidden flex flex-col bg-slate-50 dark:bg-slate-900">
      <div ref={containerRef} className="flex-1 h-full relative flex flex-col">
        {ready && (
          <ReactFlow
            nodes={flowNodes}
            edges={flowEdges}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            fitView
            nodesDraggable={false}
            nodesConnectable={false}
            elementsSelectable={false}
            onInit={(_instance) => {
                console.log("==== ReactFlow Mounted ====");
                console.log("Component:", "DiagramViewer");

                const rf = document.querySelectorAll(".react-flow");

                console.log("ReactFlow count:", rf.length);

                rf.forEach((el, index) => {
                    console.log({
                        index,
                        width: el.clientWidth,
                        height: el.clientHeight,
                        parentWidth: el.parentElement?.clientWidth,
                        parentHeight: el.parentElement?.clientHeight
                    });
                });
            }}
          >
            <Controls />
            <MiniMap />
            <Background color="#cbd5e1" gap={16} />
          </ReactFlow>
        )}
      </div>
    </div>
  );
};

export const DiagramViewer = () => {
  return (
    <ReactFlowProvider>
      <DiagramViewerInner />
    </ReactFlowProvider>
  );
};
