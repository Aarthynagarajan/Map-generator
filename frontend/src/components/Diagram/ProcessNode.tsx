import { Handle, Position } from "@xyflow/react";

export const ProcessNode = ({ data }: any) => {
  return (
    <div
      style={{
        width: 120,
        height: 60,
        background: "red",
        color: "white",
        border: "2px solid white"
      }}
    >
      {data?.label}

      <Handle type="target" position={Position.Left} />

      <Handle type="source" position={Position.Right} />
    </div>
  );
};
