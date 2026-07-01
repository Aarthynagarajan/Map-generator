import { Handle, Position, NodeProps } from "@xyflow/react";

export function ProcessNode({ data }: NodeProps<any>) {
  return (
    <div
      style={{
        width: 140,
        height: 70,
        background: "red",
        color: "white",
        border: "3px solid yellow",
        display: "flex",
        alignItems: "center",
        justifyContent: "center"
      }}
    >
      {String(data?.label)}

      <Handle id="left" type="target" position={Position.Left} />

      <Handle id="right" type="source" position={Position.Right} />
    </div>
  );
}
