import { getBezierPath, EdgeProps, BaseEdge } from '@xyflow/react';

export const ProcessEdge = ({
  id: _id,
  sourceX,
  sourceY,
  targetX,
  targetY,
  sourcePosition,
  targetPosition,
  style = {},
  markerEnd,
  data,
}: EdgeProps<any>) => {
  const [edgePath, labelX, labelY] = getBezierPath({
    sourceX,
    sourceY,
    sourcePosition,
    targetPosition,
    targetX,
    targetY,
  });

  const isActive = data?.isActive || false;

  // Medium based color defaults
  let strokeColor = '#94a3b8'; // default slate-400
  if (isActive) {
    if (data?.medium === 'liquid') {
      strokeColor = '#0ea5e9'; // sky-500
    } else if (data?.medium === 'gas') {
      strokeColor = '#a855f7'; // purple-500
    } else if (data?.medium === 'electrical') {
      strokeColor = '#eab308'; // yellow-500
    } else if (data?.medium === 'hydraulic') {
      strokeColor = '#f97316'; // orange-500
    }
  }

  const customStyle = {
    ...style,
    stroke: strokeColor,
    strokeWidth: isActive ? 3 : 2,
    strokeDasharray: isActive ? '6,6' : undefined,
    animation: isActive ? 'flow-dash 0.8s linear infinite' : undefined,
  };

  return (
    <>
      <BaseEdge path={edgePath} markerEnd={markerEnd} style={customStyle} />
      {/* Edge label */}
      {(data?.label || data?.branchCondition) && (
        <foreignObject
          width={120}
          height={40}
          x={labelX - 60}
          y={labelY - 20}
          className="bg-white/90 dark:bg-slate-950/90 border border-slate-200 dark:border-slate-800 rounded px-1.5 py-0.5 shadow-sm text-center select-none overflow-hidden"
          requiredExtensions="http://www.w3.org/1999/xhtml"
        >
          <div className="text-[9px] font-semibold text-slate-700 dark:text-slate-300 truncate mt-0.5">
            {data.label}
          </div>
          {data.branchCondition && (
            <div className="text-[8px] text-brand-600 dark:text-brand-400 font-mono truncate">
              {data.branchCondition}
            </div>
          )}
        </foreignObject>
      )}
    </>
  );
};
