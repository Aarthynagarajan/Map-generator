import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useProjects, useCreateProject, useUpdateProject, useDeleteProject } from '../hooks/useProjects';
import { useProjectStore } from '../stores/projectStore';
import { useAuthStore } from '../stores/authStore';
import { useUiStore } from '../stores/uiStore';
import { Search, Plus, Trash2, Edit2, LogOut, Sun, Moon, FolderOpen, ArrowUpDown } from 'lucide-react';

export const Dashboard = () => {
  const navigate = useNavigate();
  const { searchQuery, setSearchQuery, currentPage, setCurrentPage, sortBy, setSortBy } = useProjectStore();
  const { user, logout } = useAuthStore();
  const { theme, setTheme, showToast } = useUiStore();

  const [newProjectName, setNewProjectName] = useState('');
  const [newProjectDesc, setNewProjectDesc] = useState('');
  const [showCreateModal, setShowCreateModal] = useState(false);

  const [renameId, setRenameId] = useState<string | null>(null);
  const [renameName, setRenameName] = useState('');
  const [showRenameModal, setShowRenameModal] = useState(false);

  // Queries and mutations
  const { data, isLoading, isError } = useProjects(currentPage, 6, searchQuery, sortBy);
  const createMutation = useCreateProject();
  const updateMutation = useUpdateProject();
  const deleteMutation = useDeleteProject();

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!newProjectName.trim()) return;
    createMutation.mutate(
      { name: newProjectName.trim(), description: newProjectDesc.trim() },
      {
        onSuccess: () => {
          setNewProjectName('');
          setNewProjectDesc('');
          setShowCreateModal(false);
        },
      }
    );
  };

  const handleRenameSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    if (!renameId || !renameName.trim()) return;
    updateMutation.mutate(
      { id: renameId, details: { name: renameName.trim() } },
      {
        onSuccess: () => {
          setRenameId(null);
          setRenameName('');
          setShowRenameModal(false);
        },
      }
    );
  };

  const handleDelete = (id: string) => {
    if (window.confirm('Are you sure you want to delete this project? This will delete all diagram histories.')) {
      deleteMutation.mutate(id);
    }
  };

  const toggleTheme = () => {
    setTheme(theme === 'dark' ? 'light' : 'dark');
  };

  return (
    <div className="min-h-screen bg-slate-50 dark:bg-slate-900 text-slate-900 dark:text-slate-100 flex flex-col">
      {/* Header */}
      <header className="bg-white dark:bg-slate-950 border-b border-slate-200 dark:border-slate-800 px-6 py-4 flex items-center justify-between shadow-sm">
        <div className="flex items-center gap-3">
          <div className="bg-brand-600 text-white p-2 rounded-lg">
            <FolderOpen size={22} />
          </div>
          <div>
            <h1 className="text-lg font-bold">ProcessPro Dashboard</h1>
            <p className="text-xs opacity-60">Welcome, {user?.email}</p>
          </div>
        </div>

        <div className="flex items-center gap-3">
          <button
            onClick={toggleTheme}
            className="p-2 rounded-lg hover:bg-slate-100 dark:hover:bg-slate-900 transition-colors"
            title="Toggle Theme"
          >
            {theme === 'dark' ? <Sun size={18} /> : <Moon size={18} />}
          </button>
          <button
            onClick={() => {
              logout();
              navigate('/login');
              showToast('Logged out successfully', 'success');
            }}
            className="p-2 rounded-lg hover:bg-rose-50 hover:text-rose-600 dark:hover:bg-rose-950/20 transition-colors"
            title="Logout"
          >
            <LogOut size={18} />
          </button>
        </div>
      </header>

      {/* Main Workspace */}
      <main className="flex-1 max-w-7xl w-full mx-auto p-6 flex flex-col gap-6">
        {/* Toolbar */}
        <div className="flex flex-col sm:flex-row items-center justify-between gap-4">
          <div className="relative w-full sm:max-w-md">
            <Search className="absolute left-3 top-2.5 opacity-40" size={18} />
            <input
              type="text"
              placeholder="Search projects..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="w-full bg-white dark:bg-slate-950 border border-slate-300 dark:border-slate-700 rounded-lg pl-10 pr-4 py-2 text-sm focus:outline-none focus:border-brand-500"
            />
          </div>

          <div className="flex items-center gap-3 w-full sm:w-auto justify-end">
            <button
              onClick={() => {
                setSortBy(sortBy === 'name' ? 'updatedAt' : 'name');
              }}
              className="flex items-center gap-1 text-xs border border-slate-300 dark:border-slate-700 rounded-lg px-3 py-2 bg-white dark:bg-slate-950 hover:bg-slate-50"
            >
              <ArrowUpDown size={14} />
              <span>Sort: {sortBy === 'name' ? 'Alphabetical' : 'Date'}</span>
            </button>
            <button
              onClick={() => setShowCreateModal(true)}
              className="bg-brand-600 hover:bg-brand-700 text-white font-semibold px-4 py-2 rounded-lg text-sm flex items-center gap-1"
            >
              <Plus size={16} />
              <span>New Project</span>
            </button>
          </div>
        </div>

        {/* Content States */}
        {isLoading ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((i) => (
              <div key={i} className="bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl p-6 space-y-4 animate-pulse">
                <div className="h-6 w-3/4 bg-slate-200 dark:bg-slate-800 rounded"></div>
                <div className="h-4 w-1/2 bg-slate-200 dark:bg-slate-800 rounded"></div>
                <div className="h-4 w-full bg-slate-200 dark:bg-slate-800 rounded"></div>
              </div>
            ))}
          </div>
        ) : isError ? (
          <div className="text-center py-12 text-rose-500 font-semibold">
            Failed to load projects. Please try refreshing.
          </div>
        ) : data?.content.length === 0 ? (
          <div className="text-center py-16 bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-2xl">
            <FolderOpen size={48} className="mx-auto opacity-35 mb-2" />
            <h3 className="text-lg font-bold">No Projects Found</h3>
            <p className="text-sm opacity-60 mt-1">Create your first project to begin modeling diagrams</p>
          </div>
        ) : (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {data?.content.map((proj) => (
              <div
                key={proj.id}
                onClick={() => navigate(`/project/${proj.id}`)}
                className="bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl p-6 shadow-sm hover:shadow-md transition-shadow cursor-pointer flex flex-col justify-between group relative"
              >
                <div>
                  <h3 className="text-base font-bold truncate group-hover:text-brand-600 dark:group-hover:text-brand-400">
                    {proj.name}
                  </h3>
                  <p className="text-xs opacity-50 mt-1">
                    Updated {new Date(proj.updatedAt).toLocaleDateString()}
                  </p>
                  <p className="text-xs opacity-75 mt-3 line-clamp-2">
                    {proj.description || 'No description provided.'}
                  </p>
                </div>

                <div className="flex items-center justify-end gap-2 mt-4 pt-3 border-t border-slate-100 dark:border-slate-900">
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      setRenameId(proj.id);
                      setRenameName(proj.name);
                      setShowRenameModal(true);
                    }}
                    className="p-1.5 rounded hover:bg-slate-100 dark:hover:bg-slate-900 text-slate-500"
                    title="Rename"
                  >
                    <Edit2 size={14} />
                  </button>
                  <button
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDelete(proj.id);
                    }}
                    className="p-1.5 rounded hover:bg-rose-50 dark:hover:bg-rose-950/20 text-rose-600"
                    title="Delete"
                  >
                    <Trash2 size={14} />
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Pagination controls */}
        {data && data.totalPages > 1 && (
          <div className="flex items-center justify-center gap-2 mt-6">
            <button
              onClick={() => setCurrentPage(Math.max(0, currentPage - 1))}
              disabled={currentPage === 0}
              className="px-3 py-1.5 border border-slate-300 dark:border-slate-700 rounded-lg text-xs hover:bg-slate-50 disabled:opacity-40"
            >
              Previous
            </button>
            <span className="text-xs opacity-60">
              Page {currentPage + 1} of {data.totalPages}
            </span>
            <button
              onClick={() => setCurrentPage(Math.min(data.totalPages - 1, currentPage + 1))}
              disabled={currentPage === data.totalPages - 1}
              className="px-3 py-1.5 border border-slate-300 dark:border-slate-700 rounded-lg text-xs hover:bg-slate-50 disabled:opacity-40"
            >
              Next
            </button>
          </div>
        )}
      </main>

      {/* Create Project Modal */}
      {showCreateModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl p-6 max-w-md w-full shadow-2xl">
            <h3 className="text-lg font-bold mb-4">Create New Project</h3>
            <form onSubmit={handleCreate} className="space-y-4">
              <div>
                <label className="text-xs font-bold block mb-1">Project Name</label>
                <input
                  type="text"
                  required
                  value={newProjectName}
                  onChange={(e) => setNewProjectName(e.target.value)}
                  placeholder="e.g. Steam Cycle Loop"
                  className="w-full bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg px-3 py-2 text-sm focus:outline-none"
                />
              </div>
              <div>
                <label className="text-xs font-bold block mb-1">Description (Optional)</label>
                <textarea
                  value={newProjectDesc}
                  onChange={(e) => setNewProjectDesc(e.target.value)}
                  placeholder="Describe your process pipeline..."
                  className="w-full bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg px-3 py-2 text-sm focus:outline-none h-20 resize-none"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2 border border-slate-300 dark:border-slate-700 rounded-lg text-sm hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="bg-brand-600 hover:bg-brand-700 text-white font-semibold px-4 py-2 rounded-lg text-sm"
                >
                  Create
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {/* Rename Modal */}
      {showRenameModal && (
        <div className="fixed inset-0 z-50 bg-black/50 flex items-center justify-center p-4">
          <div className="bg-white dark:bg-slate-950 border border-slate-200 dark:border-slate-800 rounded-xl p-6 max-w-md w-full shadow-2xl">
            <h3 className="text-lg font-bold mb-4">Rename Project</h3>
            <form onSubmit={handleRenameSubmit} className="space-y-4">
              <div>
                <label className="text-xs font-bold block mb-1">New Name</label>
                <input
                  type="text"
                  required
                  value={renameName}
                  onChange={(e) => setRenameName(e.target.value)}
                  className="w-full bg-slate-50 dark:bg-slate-900 border border-slate-300 dark:border-slate-700 rounded-lg px-3 py-2 text-sm focus:outline-none"
                />
              </div>
              <div className="flex justify-end gap-2 pt-2">
                <button
                  type="button"
                  onClick={() => setShowRenameModal(false)}
                  className="px-4 py-2 border border-slate-300 dark:border-slate-700 rounded-lg text-sm hover:bg-slate-50"
                >
                  Cancel
                </button>
                <button
                  type="submit"
                  className="bg-brand-600 hover:bg-brand-700 text-white font-semibold px-4 py-2 rounded-lg text-sm"
                >
                  Rename
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
