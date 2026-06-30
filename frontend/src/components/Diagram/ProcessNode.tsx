import { Handle, Position, NodeProps } from '@xyflow/react';
import { AlertCircle, ToggleLeft, ToggleRight } from 'lucide-react';
import { useDiagramStore } from '../../stores/diagramStore';

export const ProcessNode = ({ id, data }: NodeProps<any>) => {
  const { toggleNodeState } = useDiagramStore();

  const isStopper = [
    'GATE_VALVE', 'BALL_VALVE', 'BUTTERFLY_VALVE', 'CHECK_VALVE',
    'CIRCUIT_BREAKER', 'SWITCH', 'RELAY', 'LIMIT_SWITCH', 'PUSH_BUTTON',
    'HYDRAULIC_VALVE', 'CHECK_VALVE_HYD', 'FLOW_CONTROL_VALVE', 'DIRECTIONAL_CONTROL_VALVE'
  ].includes(data.entityClass?.toUpperCase());

  const state = data.state || 'open';
  const isOpen = state === 'open' || state === 'on';

  // Basic styling configurations based on entityClass and active state
  let nodeColor = 'border-slate-300 dark:border-slate-700 bg-slate-50 dark:bg-slate-900';
  if (isStopper) {
    nodeColor = isOpen 
      ? 'border-emerald-500 bg-emerald-50/50 dark:bg-emerald-950/20 text-emerald-700 dark:text-emerald-300'
      : 'border-rose-500 bg-rose-50/50 dark:bg-rose-950/20 text-rose-700 dark:text-rose-300';
  } else if (data.entityClass?.includes('PUMP')) {
    nodeColor = 'border-sky-500 bg-sky-50/50 dark:bg-sky-950/20 text-sky-700 dark:text-sky-300';
  } else if (data.entityClass?.includes('TANK') || data.entityClass?.includes('RESERVOIR')) {
    nodeColor = 'border-amber-500 bg-amber-50/50 dark:bg-amber-950/20 text-amber-700 dark:text-amber-300';
  }

  return (
    <div className={`px-4 py-3 rounded-lg border-2 shadow-md transition-all relative ${nodeColor} min-w-[120px] text-center`}>
      {/* Handles on 4 sides */}
      <Handle type="target" position={Position.Left} className="w-2.5 h-2.5 bg-brand-500 border border-white" />
      <Handle type="source" position={Position.Right} className="w-2.5 h-2.5 bg-brand-500 border border-white" />
      <Handle type="target" position={Position.Top} className="w-2.5 h-2.5 bg-brand-500 border border-white" />
      <Handle type="source" position={Position.Bottom} className="w-2.5 h-2.5 bg-brand-500 border border-white" />

      {/* Warning indicator for userConfirmRequired */}
      {data.userConfirmRequired && (
        <div className="absolute -top-2.5 -right-2.5 bg-amber-500 text-white rounded-full p-0.5 animate-pulse" title="Needs manual validation">
          <AlertCircle size={14} />
        </div>
      )}

      {/* Node Content */}
      <div className="text-[10px] uppercase font-bold tracking-wider opacity-60">
        {data.tag || data.entityClass}
      </div>
      <div className="text-xs font-semibold mt-0.5">{data.label}</div>

      {/* Stopper States Control */}
      {isStopper && (
        <button
          onClick={(e) => {
            e.stopPropagation();
            toggleNodeState(id);
          }}
          className="mt-2 flex items-center justify-center gap-1 mx-auto text-[10px] font-medium border border-current rounded px-1.5 py-0.5 hover:bg-black/5 dark:hover:bg-white/5 transition-colors"
        >
          {isOpen ? <ToggleRight size={14} className="text-emerald-500" /> : <ToggleLeft size={14} className="text-rose-500" />}
          <span className="uppercase">{state}</span>
        </button>
      )}

      {/* Confidence metric indicator */}
      {data.confidence < 1.0 && (
        <div className="text-[9px] mt-1 opacity-50">
          Confidence: {Math.round(data.confidence * 100)}%
        </div>
      )}
    </div>
  );
};
