import { useMemo, useState, useEffect, useLayoutEffect, useRef } from 'react';
import { ReactFlow, MiniMap, Controls, Background, useReactFlow, ReactFlowProvider } from '@xyflow/react';
import '@xyflow/react/dist/style.css';

import { useDiagramStore } from '../../stores/diagramStore';
import { useAutoSave } from '../../hooks/useAutoSave';
import { ProcessNode } from './ProcessNode';
import { ProcessEdge } from './ProcessEdge';
import { Undo2, Redo2, Trash2, Edit2, Check, X } from 'lucide-react';

const nodeTypes = {
  processNode: ProcessNode as any,
};

const edgeTypes = {
  processEdge: ProcessEdge as any,
};



interface DiagramEditorProps {
  diagramId: string;
}

const DiagramEditorInner = ({ diagramId }: DiagramEditorProps) => {
  const { nodes, edges, updateNodePosition, renameNode, deleteNode, undo, redo, undoStack, redoStack } = useDiagramStore();
  const [selectedNodeId, setSelectedNodeId] = useState<string | null>(null);
  const [isEditing, setIsEditing] = useState(false);
  const [editLabel, setEditLabel] = useState('');

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

  // Setup auto-save hook
  useAutoSave(diagramId);

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
        isActive: false, // Editor doesn't showcase active paths simulation
      } as any,
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

  const onNodeDragStop = (_event: any, node: any) => {
    updateNodePosition(node.id, node.position.x, node.position.y);
  };

  const onNodeClick = (_event: any, node: any) => {
    setSelectedNodeId(node.id);
  };

  const onPaneClick = () => {
    setSelectedNodeId(null);
    setIsEditing(false);
  };

  const handleStartRename = () => {
    if (!selectedNodeId || !nodes[selectedNodeId]) return;
    setEditLabel(nodes[selectedNodeId].label);
    setIsEditing(true);
  };

  const handleSaveRename = () => {
    if (selectedNodeId && editLabel.trim()) {
      renameNode(selectedNodeId, editLabel.trim());
    }
    setIsEditing(false);
  };

  const handleDelete = () => {
    if (selectedNodeId) {
      deleteNode(selectedNodeId);
      setSelectedNodeId(null);
    }
  };
console.log("Nodes in store:", nodes);
console.log("Edges in store:", edges);
console.log("Flow nodes:", flowNodes.length);
console.log("Flow edges:", flowEdges.length);
  return (
    <div className="w-full h-full border border-slate-200 dark:border-slate-800 rounded-lg overflow-hidden flex flex-col bg-slate-50 dark:bg-slate-900">
      {/* Editor control toolbar */}
      <div className="p-3 border-b border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 flex items-center justify-between gap-4">
        <div className="flex items-center gap-2">
          <button
            onClick={undo}
            disabled={undoStack.length === 0}
            className="p-1.5 rounded hover:bg-slate-100 dark:hover:bg-slate-900 disabled:opacity-40"
            title="Undo"
          >
            <Undo2 size={16} />
          </button>
          <button
            onClick={redo}
            disabled={redoStack.length === 0}
            className="p-1.5 rounded hover:bg-slate-100 dark:hover:bg-slate-900 disabled:opacity-40"
            title="Redo"
          >
            <Redo2 size={16} />
          </button>
        </div>

        {selectedNodeId && (
          <div className="flex items-center gap-2 bg-slate-100 dark:bg-slate-900 px-3 py-1 rounded-lg">
            {isEditing ? (
              <div className="flex items-center gap-1">
                <input
                  type="text"
                  value={editLabel}
                  onChange={(e) => setEditLabel(e.target.value)}
                  className="bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-700 px-2 py-0.5 rounded text-xs focus:outline-none"
                  autoFocus
                  onKeyDown={(e) => {
                    if (e.key === 'Enter') handleSaveRename();
                    if (e.key === 'Escape') setIsEditing(false);
                  }}
                />
                <button onClick={handleSaveRename} className="p-1 text-emerald-600">
                  <Check size={14} />
                </button>
                <button onClick={() => setIsEditing(false)} className="p-1 text-rose-600">
                  <X size={14} />
                </button>
              </div>
            ) : (
              <>
                <span className="text-xs font-semibold max-w-[120px] truncate">
                  {nodes[selectedNodeId]?.label}
                </span>
                <button onClick={handleStartRename} className="p-1 text-brand-600" title="Rename">
                  <Edit2 size={14} />
                </button>
                <button onClick={handleDelete} className="p-1 text-rose-600" title="Delete">
                  <Trash2 size={14} />
                </button>
              </>
            )}
          </div>
        )}
      </div>

      <div ref={containerRef} className="flex-1 relative flex flex-col">
        {ready && (
          <ReactFlow
            nodes={flowNodes}
            edges={flowEdges}
            nodeTypes={nodeTypes}
            edgeTypes={edgeTypes}
            onNodeDragStop={onNodeDragStop}
            onNodeClick={onNodeClick}
            onPaneClick={onPaneClick}
            fitView
            onInit={(_instance) => {
                console.log("==== ReactFlow Mounted ====");
                console.log("Component:", "DiagramEditor");

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

export const DiagramEditor = (props: DiagramEditorProps) => {
  return (
    <ReactFlowProvider>
      <DiagramEditorInner {...props} />
    </ReactFlowProvider>
  );
};
