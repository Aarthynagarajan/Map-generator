import { useMemo, useEffect } from 'react';
import { ReactFlow, MiniMap, Controls, Background, useReactFlow, ReactFlowProvider } from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import { useDiagramStore } from '../../stores/diagramStore';


const DiagramViewerInner = () => {
  const { nodes, edges } = useDiagramStore();
  const { fitView } = useReactFlow();

  const flowNodes = useMemo(() => {
    return Object.values(nodes).map(n => ({
      id: n.id,
      position: {
        x: n.x,
        y: n.y
      },
      data: {
        label: n.label
      }
    }));
  }, [nodes]);

  const flowEdges = useMemo(() => {
    return Object.values(edges).map(e => ({
      id: e.id,
      source: e.from,
      target: e.to
    }));
  }, [edges]);

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
      <div className="flex-1 h-full relative flex flex-col">
        <ReactFlow
          nodes={flowNodes}
          edges={flowEdges}
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
