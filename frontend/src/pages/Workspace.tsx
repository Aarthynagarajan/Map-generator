import { useState, useEffect, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { useProject } from '../hooks/useProjects';
import { useHistory } from '../hooks/useDiagram';
import { useDiagramStore } from '../stores/diagramStore';
import { useGenerationStore, GenStage } from '../stores/generationStore';
import { useSimulationStore } from '../stores/simulationStore';
import { useUiStore } from '../stores/uiStore';
import { useAuthStore } from '../stores/authStore';
import { DiagramViewer } from '../components/Diagram/DiagramViewer';
import { DiagramEditor } from '../components/Diagram/DiagramEditor';
import { diagramService } from '../services/diagramService';
import {
  ArrowLeft,
  Sparkles,
  Play,
  Pause,
  RotateCcw,
  Download,
  History,
  Activity,
  Edit3,
  Loader2,
  FileCode,
  Save,
} from 'lucide-react';

export const Workspace = () => {
  const { id: projectId } = useParams<{ id: string }>();
  const navigate = useNavigate();

  const { data: project } = useProject(projectId);
  const { data: historyList, refetch: refetchHistory } = useHistory(projectId);

  // Zustand states
  const { currentDiagram, setDiagram, nodes, edges } = useDiagramStore();
  const { stage, percentage, startGen, updateProgress, setComplete, setError } = useGenerationStore();
  const { isPlaying, setPlaying, speed, setSpeed, setActivePaths, resetSimulation } = useSimulationStore();
  const { showToast } = useUiStore();
  const { accessToken } = useAuthStore();

  const abortControllerRef = useRef<AbortController | null>(null);

  const [activeTab, setActiveTab] = useState<'editor' | 'simulation' | 'history'>('editor');
  const [prompt, setPrompt] = useState('');
  const [domain, setDomain] = useState('industrial');

  // Load latest diagram from history list
  useEffect(() => {
    if (historyList && historyList.length > 0) {
      // Find latest version
      const latest = [...historyList].sort((a, b) => b.version - a.version)[0];
      setDiagram(latest);
    } else {
      setDiagram(null);
    }
  }, [historyList]);

  // SSE Generation Stream Parser
  const handleGenerate = async () => {
    if (!prompt.trim() || !projectId) return;

    if (abortControllerRef.current) {
      abortControllerRef.current.abort();
    }
    const controller = new AbortController();
    abortControllerRef.current = controller;

    startGen();
    showToast('Starting AI extraction...', 'info');

    let reader: ReadableStreamDefaultReader<Uint8Array> | undefined = undefined;

    try {
      const baseUrl = window.location.hostname === 'localhost' && window.location.port !== '8080'
        ? 'http://localhost:8080'
        : window.location.origin;
      const response = await fetch(`${baseUrl}/api/v1/generate`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          'Authorization': `Bearer ${accessToken}`,
        },
        signal: controller.signal,
        body: JSON.stringify({
          projectId,
          prompt: prompt.trim(),
          domain,
          constraints: {
            direction: domain === 'industrial' || domain === 'hydraulic' ? 'LR' : 'TB',
            symbolStandard: 'ISA',
            spacingDensity: 'medium',
          },
        }),
      });

      if (!response.ok) {
        throw new Error(`Server returned error status: ${response.status}`);
      }

      reader = response.body?.getReader();
      const decoder = new TextDecoder();

      if (!reader) {
        throw new Error('No readable response stream found.');
      }

      let buffer = '';
      while (true) {
        const { value, done } = await reader.read();
        if (done) break;

        buffer += decoder.decode(value, { stream: true });
        const events = buffer.split('\n\n');
        buffer = events.pop() || ''; // retain incomplete event

        for (const rawEvent of events) {
          if (!rawEvent.trim()) continue;

          const lines = rawEvent.split('\n');
          let eventName = '';
          let eventData = '';

          for (const line of lines) {
            if (line.startsWith('event:')) {
              eventName = line.substring(6).trim();
            } else if (line.startsWith('data:')) {
              eventData = line.substring(5).trim();
            }
          }

          if (eventName === 'progress') {
            const parsed = JSON.parse(eventData);
            updateProgress(parsed.stage as GenStage, parsed.pct);
          } else if (eventName === 'complete') {
            const parsed = JSON.parse(eventData);
            setComplete(parsed.diagramId);
            showToast('Diagram generated successfully!', 'success');
            refetchHistory();
            return;
          } else if (eventName === 'error') {
            const parsed = JSON.parse(eventData);
            setError(parsed.message);
            showToast(parsed.message, 'error');
            return;
          }
        }
      }
    } catch (err: any) {
      if (err.name === 'AbortError') {
        console.info('Generation request aborted successfully.');
        return;
      }
      console.error(err);
      setError(err.message || 'Failed to complete generation');
      showToast(err.message || 'Generation failed', 'error');
    } finally {
      if (reader) {
        try {
          reader.cancel();
          reader.releaseLock();
        } catch (e) {
          console.warn('Error closing stream reader:', e);
        }
      }
      controller.abort();
      if (abortControllerRef.current === controller) {
        abortControllerRef.current = null;
      }
    }
  };

  // Run Flow Path Simulation Locally
  useEffect(() => {
    if (!isPlaying) {
      setActivePaths(new Set());
      return;
    }

    const interval = setInterval(() => {
      // Find sources (e.g. RESERVOIR, GENERATOR, or no incoming edges)
      const sourceNodeIds = Object.values(nodes)
        .filter(n => n.entityClass?.includes('RESERVOIR') || n.entityClass?.includes('GENERATOR'))
        .map(n => n.id);

      if (sourceNodeIds.length === 0) {
        // Fallback to first node
        const first = Object.keys(nodes)[0];
        if (first) sourceNodeIds.push(first);
      }

      const activeEdges = new Set<string>();
      const visited = new Set<string>();
      const queue: string[] = [...sourceNodeIds];

      while (queue.length > 0) {
        const currId = queue.shift()!;
        if (visited.has(currId)) continue;
        visited.add(currId);

        // Find outgoing edges
        Object.values(edges).forEach(edge => {
          if (edge.from === currId) {
            const targetNode = nodes[edge.to];
            if (targetNode) {
              const state = targetNode.state || 'open';
              const isClosed = state === 'closed' || state === 'off';

              if (!isClosed) {
                activeEdges.add(edge.id);
                queue.push(edge.to);
              }
            }
          }
        });
      }

      setActivePaths(activeEdges);
    }, 1000 / speed);

    return () => clearInterval(interval);
  }, [isPlaying, nodes, edges, speed]);

  const handleRestore = async (version: number) => {
    if (!currentDiagram) return;
    try {
      await diagramService.restoreVersion(currentDiagram.id, version);
      showToast(`Restored version v${version}`, 'success');
      refetchHistory();
    } catch (error) {
      showToast('Failed to restore version', 'error');
    }
  };

  const handleExport = (format: 'json' | 'svg') => {
    if (!currentDiagram) return;

    if (format === 'json') {
      const liveSnapshot = {
        nodes: useDiagramStore.getState().nodes,
        edges: useDiagramStore.getState().edges,
        domain: currentDiagram.graphSnapshot.domain,
      };
      const dataStr = JSON.stringify(liveSnapshot, null, 2);
      const dataUri = 'data:application/json;charset=utf-8,'+ encodeURIComponent(dataStr);
      const exportFileDefaultName = `diagram-${currentDiagram.id}.json`;

      const linkElement = document.createElement('a');
      linkElement.setAttribute('href', dataUri);
      linkElement.setAttribute('download', exportFileDefaultName);
      linkElement.click();
      showToast('Exported JSON configuration file', 'success');
    } else {
      // Export live SVG representation
      const ns = useDiagramStore.getState().nodes;
      const es = useDiagramStore.getState().edges;

      const xCoords = Object.values(ns).map(n => n.x);
      const yCoords = Object.values(ns).map(n => n.y);
      const minX = Math.min(...xCoords, 0) - 100;
      const maxX = Math.max(...xCoords, 800) + 200;
      const minY = Math.min(...yCoords, 0) - 100;
      const maxY = Math.max(...yCoords, 600) + 200;
      const width = maxX - minX;
      const height = maxY - minY;

      let svgContent = `<svg xmlns="http://www.w3.org/2000/svg" viewBox="${minX} ${minY} ${width} ${height}" width="${width}" height="${height}">`;
      svgContent += `\n  <rect width="100%" height="100%" fill="#0f172a" />`;

      // Draw edges
      Object.values(es).forEach(edge => {
        const fromNode = ns[edge.from];
        const toNode = ns[edge.to];
        if (fromNode && toNode) {
          const x1 = fromNode.x + 50;
          const y1 = fromNode.y + 30;
          const x2 = toNode.x + 50;
          const y2 = toNode.y + 30;
          const color = edge.medium === 'electrical' ? '#fbbf24' : (edge.medium === 'liquid' ? '#38bdf8' : '#34d399');
          svgContent += `\n  <line x1="${x1}" y1="${y1}" x2="${x2}" y2="${y2}" stroke="${color}" stroke-width="3" marker-end="url(#arrow)" />`;
        }
      });

      // Draw nodes
      Object.values(ns).forEach(node => {
        const x = node.x;
        const y = node.y;
        const label = node.label || '';
        const tag = node.tag || '';
        const border = node.medium === 'electrical' ? '#fbbf24' : (node.medium === 'liquid' ? '#38bdf8' : '#34d399');
        svgContent += `\n  <rect x="${x}" y="${y}" width="100" height="60" rx="6" fill="#1e293b" stroke="${border}" stroke-width="2" />`;
        svgContent += `\n  <text x="${x + 50}" y="${y + 25}" fill="#f8fafc" font-size="10" font-family="sans-serif" font-weight="bold" text-anchor="middle">${tag}</text>`;
        svgContent += `\n  <text x="${x + 50}" y="${y + 45}" fill="#94a3b8" font-size="9" font-family="sans-serif" text-anchor="middle">${label}</text>`;
      });

      svgContent += `\n  <defs>\n    <marker id="arrow" viewBox="0 0 10 10" refX="6" refY="5" markerWidth="6" markerHeight="6" orient="auto-start-reverse">\n      <path d="M 0 0 L 10 5 L 0 10 z" fill="#94a3b8"/>\n    </marker>\n  </defs>\n</svg>`;

      const dataUri = 'data:image/svg+xml;charset=utf-8,' + encodeURIComponent(svgContent);
      const linkElement = document.createElement('a');
      linkElement.setAttribute('href', dataUri);
      linkElement.setAttribute('download', `diagram-${currentDiagram.id}.svg`);
      linkElement.click();
      showToast('Exported SVG image file', 'success');
    }
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 flex flex-col">
      {/* Header toolbar */}
      <header className="bg-white dark:bg-slate-950 border-b border-slate-200 dark:border-slate-800 px-6 py-3 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-3">
          <button
            onClick={() => navigate('/dashboard')}
            className="p-1.5 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-900 transition-colors"
          >
            <ArrowLeft size={18} />
          </button>
          <div>
            <h1 className="text-sm font-bold truncate max-w-[200px]">{project?.name || 'Loading project...'}</h1>
            <p className="text-[10px] opacity-60">ID: {projectId}</p>
          </div>
        </div>

        {/* View/Editor Tabs */}
        {currentDiagram && (
          <div className="flex items-center bg-slate-100 dark:bg-slate-900 p-0.5 rounded-lg">
            <button
              onClick={() => setActiveTab('editor')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
                activeTab === 'editor' ? 'bg-white dark:bg-slate-950 shadow-sm text-brand-600 dark:text-brand-400' : 'opacity-60 hover:opacity-100'
              }`}
            >
              <Edit3 size={14} />
              <span>Editor</span>
            </button>
            <button
              onClick={() => setActiveTab('simulation')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
                activeTab === 'simulation' ? 'bg-white dark:bg-slate-950 shadow-sm text-brand-600 dark:text-brand-400' : 'opacity-60 hover:opacity-100'
              }`}
            >
              <Activity size={14} />
              <span>Simulation</span>
            </button>
            <button
              onClick={() => setActiveTab('history')}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-md text-xs font-semibold transition-all ${
                activeTab === 'history' ? 'bg-white dark:bg-slate-950 shadow-sm text-brand-600 dark:text-brand-400' : 'opacity-60 hover:opacity-100'
              }`}
            >
              <History size={14} />
              <span>History</span>
            </button>
          </div>
        )}

        {/* Exports */}
        {currentDiagram && (
          <div className="flex items-center gap-2">
            <button
              onClick={() => handleExport('json')}
              className="flex items-center gap-1 text-xs border border-slate-300 dark:border-slate-700 px-3 py-1.5 rounded-lg hover:bg-slate-50"
            >
              <FileCode size={14} />
              <span>JSON</span>
            </button>
            <button
              onClick={() => handleExport('svg')}
              className="flex items-center gap-1 text-xs bg-brand-600 hover:bg-brand-700 text-white font-semibold px-3 py-1.5 rounded-lg"
            >
              <Download size={14} />
              <span>SVG</span>
            </button>
          </div>
        )}
      </header>

      {/* Workspace Area */}
      <div className="flex-1 flex overflow-hidden">
        {/* Left Generation Sidebar Panel */}
        <aside className="w-80 border-r border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 p-4 flex flex-col gap-4 overflow-y-auto">
          <div>
            <h3 className="text-xs font-bold uppercase tracking-wider opacity-60 mb-2">AI Generator</h3>
            <textarea
              value={prompt}
              onChange={(e) => setPrompt(e.target.value)}
              placeholder="e.g. Water flows from reservoir into centrifugal pump P-101, then through check valve CV-101 to storage tank T-102."
              className="w-full h-32 bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg p-3 text-xs focus:outline-none resize-none"
            />
          </div>

          <div className="flex items-center justify-between gap-4">
            <span className="text-xs font-bold opacity-60">Domain</span>
            <select
              value={domain}
              onChange={(e) => setDomain(e.target.value)}
              className="bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg px-2 py-1 text-xs focus:outline-none"
            >
              <option value="industrial">Industrial P&ID</option>
              <option value="electrical">Electrical Single-Line</option>
              <option value="hydraulic">Hydraulic Circuit</option>
            </select>
          </div>

          <button
            onClick={handleGenerate}
            disabled={stage !== 'idle' && stage !== 'complete' && stage !== 'error'}
            className="w-full bg-brand-600 hover:bg-brand-700 disabled:bg-slate-300 text-white font-semibold py-2 rounded-lg text-xs flex items-center justify-center gap-2"
          >
            {stage !== 'idle' && stage !== 'complete' && stage !== 'error' ? (
              <Loader2 className="animate-spin" size={14} />
            ) : (
              <Sparkles size={14} />
            )}
            <span>Generate Map</span>
          </button>

          {/* SSE Progress display */}
          {stage !== 'idle' && (
            <div className="p-4 rounded-xl border border-slate-100 dark:border-slate-900 bg-slate-50 dark:bg-slate-900/50 space-y-3">
              <div className="flex items-center justify-between text-xs">
                <span className="font-semibold capitalize">{stage.replace('_', ' ')}</span>
                <span className="opacity-60">{percentage}%</span>
              </div>
              <div className="w-full bg-slate-200 dark:bg-slate-800 rounded-full h-1.5">
                <div className="bg-brand-500 h-1.5 rounded-full transition-all" style={{ width: `${percentage}%` }}></div>
              </div>
            </div>
          )}
        </aside>

        {/* Center / Editor Canvas Area */}
        <section className="flex-1 h-full relative p-4 flex flex-col">
          {currentDiagram ? (
            activeTab === 'editor' ? (
              <DiagramEditor diagramId={currentDiagram.id} />
            ) : activeTab === 'simulation' ? (
              <div className="flex-1 flex flex-col gap-4 relative">
                {/* Simulation Control Overlay */}
                <div className="absolute top-4 left-4 z-10 bg-white/90 dark:bg-slate-950/90 border border-slate-200 dark:border-slate-800 rounded-xl p-3 flex items-center gap-3 shadow-md">
                  <button
                    onClick={() => setPlaying(!isPlaying)}
                    className="p-2 bg-brand-600 hover:bg-brand-700 text-white rounded-lg"
                  >
                    {isPlaying ? <Pause size={16} /> : <Play size={16} />}
                  </button>
                  <button
                    onClick={() => {
                      resetSimulation();
                      showToast('Simulation reset successfully', 'info');
                    }}
                    className="p-2 border border-slate-300 dark:border-slate-700 rounded-lg hover:bg-slate-50"
                  >
                    <RotateCcw size={16} />
                  </button>
                  <div className="flex items-center gap-2">
                    <span className="text-xs opacity-60">Speed</span>
                    <select
                      value={speed}
                      onChange={(e) => setSpeed(Number(e.target.value))}
                      className="bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded px-1 py-0.5 text-xs focus:outline-none"
                    >
                      <option value={1}>1x</option>
                      <option value={2}>2x</option>
                      <option value={5}>5x</option>
                    </select>
                  </div>
                </div>
                <DiagramViewer />
              </div>
            ) : (
              /* History revisions comparison view */
              <div className="flex-1 flex gap-6 h-full">
                <div className="flex-1 h-full">
                  <DiagramViewer />
                </div>
                <aside className="w-64 border-l border-slate-200 dark:border-slate-800 bg-white dark:bg-slate-950 p-4 rounded-xl flex flex-col gap-3 overflow-y-auto">
                  <h3 className="text-xs font-bold uppercase tracking-wider opacity-60">Revisions Timeline</h3>
                  <div className="space-y-2">
                    {historyList?.map((hist) => (
                      <div
                        key={hist.id}
                        onClick={() => setDiagram(hist)}
                        className={`p-3 rounded-lg border text-left cursor-pointer transition-colors ${
                          currentDiagram.id === hist.id
                            ? 'border-brand-500 bg-brand-50/50 dark:bg-brand-950/20'
                            : 'border-slate-200 dark:border-slate-800 hover:bg-slate-50'
                        }`}
                      >
                        <div className="flex items-center justify-between text-xs font-bold">
                          <span>Revision v{hist.version}</span>
                          <span className="opacity-55">{new Date(hist.createdAt).toLocaleTimeString()}</span>
                        </div>
                        <p className="text-[10px] opacity-60 truncate mt-1">{hist.prompt}</p>
                        <button
                          onClick={(e) => {
                            e.stopPropagation();
                            handleRestore(hist.version);
                          }}
                          className="mt-2 text-[10px] font-bold text-brand-600 hover:underline flex items-center gap-1"
                        >
                          <Save size={10} />
                          <span>Restore Version</span>
                        </button>
                      </div>
                    ))}
                  </div>
                </aside>
              </div>
            )
          ) : (
            <div className="flex-1 flex flex-col items-center justify-center border border-dashed border-slate-300 dark:border-slate-700 rounded-xl py-24">
              <Sparkles size={40} className="opacity-30 mb-2 animate-pulse text-brand-500" />
              <h3 className="text-base font-bold">Workspace Empty</h3>
              <p className="text-xs opacity-60 mt-1 max-w-xs text-center">
                Enter a system description in the sidebar and click Generate to start AI diagram extraction
              </p>
            </div>
          )}
        </section>
      </div>
    </div>
  );
};
