export interface User {
  id: string;
  email: string;
  role: 'USER' | 'ADMIN';
}

export interface Project {
  id: string;
  name: string;
  domain: 'INDUSTRIAL' | 'ELECTRICAL' | 'HYDRAULIC';
  description?: string;
  createdAt: string;
  updatedAt: string;
  ownerId: string;
}

export interface Point {
  x: number;
  y: number;
}

export interface TypedNode {
  id: string;
  label: string;
  entityClass: string;
  symbolId?: string;
  x: number;
  y: number;
  width: number;
  height: number;
  orientation?: string;
  locked: boolean;
  state: string;
  confidence: number;
  medium: string;
  tag: string;
  aliases: string[];
  userConfirmRequired: boolean;
}

export interface TypedEdge {
  id: string;
  from: string;
  to: string;
  medium: string;
  direction: 'forward' | 'reverse' | 'bidirectional';
  label?: string;
  branchCondition?: string;
  routePoints: Point[];
  labelPosition?: Point;
  animationClass?: string;
}

export interface DiagramGraph {
  nodes: Record<string, TypedNode>;
  edges: Record<string, TypedEdge>;
  adjacency: Record<string, string[]>;
  domain: string;
}

export interface Diagram {
  id: string;
  projectId: string;
  prompt: string;
  graphSnapshot: DiagramGraph;
  version: number;
  createdAt: string;
}

export interface Scenario {
  id: string;
  diagramId: string;
  name: string;
  stopperStates: Record<string, string>;
  isDefault: boolean;
}

export interface SymbolRegistryItem {
  symbolId: string;
  name: string;
  domain: string;
  entityClass: string;
  svgPath: string;
  defaultTagPrefix?: string;
}
