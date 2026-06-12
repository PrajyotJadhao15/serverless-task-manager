import React, { useState, useEffect } from 'react';
import axios from 'axios';

const API = 'https://7flo8fwrl7.execute-api.us-east-1.amazonaws.com/prod';

const PRIORITY_COLORS = {
  HIGH: { bg: '#fee2e2', border: '#ef4444', badge: '#ef4444' },
  MEDIUM: { bg: '#fef9c3', border: '#eab308', badge: '#eab308' },
  LOW: { bg: '#dcfce7', border: '#22c55e', badge: '#22c55e' },
};

export default function App() {
  const [tasks, setTasks] = useState([]);
  const [stats, setStats] = useState(null);
  const [loading, setLoading] = useState(false);
  const [filter, setFilter] = useState('ALL');
  const [search, setSearch] = useState('');
  const [form, setForm] = useState({
    title: '', description: '', priority: 'MEDIUM', dueDate: ''
  });
  const [message, setMessage] = useState('');

  useEffect(() => { fetchTasks(); fetchStats(); }, []);

  const fetchTasks = async () => {
    setLoading(true);
    try {
      const res = await axios.get(`${API}/tasks`);
      const data = typeof res.data.body === 'string'
        ? JSON.parse(res.data.body) : res.data.body || res.data;
      setTasks(Array.isArray(data) ? data : []);
    } catch (e) { setMessage('Error loading tasks'); }
    setLoading(false);
  };

  const fetchStats = async () => {
    try {
      const res = await axios.get(`${API}/tasks/stats`);
      const data = typeof res.data.body === 'string'
        ? JSON.parse(res.data.body) : res.data.body || res.data;
      setStats(data);
    } catch (e) {}
  };

  const createTask = async () => {
    if (!form.title.trim()) { setMessage('Title is required'); return; }
    try {
      await axios.post(`${API}/tasks`, form);
      setForm({ title: '', description: '', priority: 'MEDIUM', dueDate: '' });
      setMessage('Task created successfully!');
      fetchTasks(); fetchStats();
    } catch (e) { setMessage('Error creating task'); }
  };

  const toggleStatus = async (task) => {
    try {
      await axios.put(`${API}/tasks/${task.taskId}`, {
        ...task,
        status: task.status === 'COMPLETED' ? 'PENDING' : 'COMPLETED'
      });
      fetchTasks(); fetchStats();
    } catch (e) { setMessage('Error updating task'); }
  };

  const deleteTask = async (taskId) => {
    try {
      await axios.delete(`${API}/tasks/${taskId}`);
      setMessage('Task deleted');
      fetchTasks(); fetchStats();
    } catch (e) { setMessage('Error deleting task'); }
  };

  const filtered = tasks.filter(t => {
    const matchFilter = filter === 'ALL' || t.status === filter;
    const matchSearch = t.title.toLowerCase().includes(search.toLowerCase());
    return matchFilter && matchSearch;
  });

  return (
    <div style={{ fontFamily: 'Segoe UI, sans-serif', background: '#f1f5f9', minHeight: '100vh' }}>

      {/* Header */}
      <div style={{ background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', padding: '24px', color: 'white' }}>
        <h1 style={{ margin: 0, fontSize: '28px' }}>📋 Task Manager</h1>
        <p style={{ margin: '4px 0 0', opacity: 0.8 }}>AWS Lambda + DynamoDB + React</p>
      </div>

      <div style={{ maxWidth: '900px', margin: '0 auto', padding: '24px' }}>

        {/* Stats */}
        {stats && (
          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(4,1fr)', gap: '16px', marginBottom: '24px' }}>
            {[
              { label: 'Total Tasks', value: stats.total, color: '#6366f1' },
              { label: 'Completed', value: stats.completed, color: '#22c55e' },
              { label: 'Pending', value: stats.pending, color: '#f59e0b' },
              { label: 'Completion Rate', value: `${stats.completionRate}%`, color: '#8b5cf6' },
            ].map(s => (
              <div key={s.label} style={{ background: 'white', borderRadius: '12px', padding: '16px', textAlign: 'center', boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
                <div style={{ fontSize: '28px', fontWeight: 'bold', color: s.color }}>{s.value}</div>
                <div style={{ fontSize: '13px', color: '#64748b', marginTop: '4px' }}>{s.label}</div>
              </div>
            ))}
          </div>
        )}

        {/* Progress Bar */}
        {stats && (
          <div style={{ background: 'white', borderRadius: '12px', padding: '16px', marginBottom: '24px', boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '8px' }}>
              <span style={{ fontWeight: '600', color: '#374151' }}>Overall Progress</span>
              <span style={{ color: '#6366f1', fontWeight: '600' }}>{stats.completionRate}%</span>
            </div>
            <div style={{ background: '#e2e8f0', borderRadius: '99px', height: '10px' }}>
              <div style={{ background: 'linear-gradient(90deg, #6366f1, #8b5cf6)', width: `${stats.completionRate}%`, height: '10px', borderRadius: '99px', transition: 'width 0.5s' }} />
            </div>
          </div>
        )}

        {/* Message */}
        {message && (
          <div style={{ background: '#ede9fe', border: '1px solid #8b5cf6', color: '#6d28d9', padding: '12px 16px', borderRadius: '8px', marginBottom: '16px' }}>
            {message}
            <span onClick={() => setMessage('')} style={{ float: 'right', cursor: 'pointer' }}>✕</span>
          </div>
        )}

        {/* Create Task Form */}
        <div style={{ background: 'white', borderRadius: '12px', padding: '20px', marginBottom: '24px', boxShadow: '0 1px 4px rgba(0,0,0,0.1)' }}>
          <h2 style={{ margin: '0 0 16px', color: '#1e293b', fontSize: '18px' }}>➕ Create New Task</h2>
          <input
            placeholder="Task title *"
            value={form.title}
            onChange={e => setForm({ ...form, title: e.target.value })}
            style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #e2e8f0', marginBottom: '10px', boxSizing: 'border-box', fontSize: '14px' }}
          />
          <textarea
            placeholder="Description (optional)"
            value={form.description}
            onChange={e => setForm({ ...form, description: e.target.value })}
            rows={2}
            style={{ width: '100%', padding: '10px', borderRadius: '8px', border: '1px solid #e2e8f0', marginBottom: '10px', boxSizing: 'border-box', fontSize: '14px', resize: 'vertical' }}
          />
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px', marginBottom: '12px' }}>
            <select
              value={form.priority}
              onChange={e => setForm({ ...form, priority: e.target.value })}
              style={{ padding: '10px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '14px' }}
            >
              <option value="HIGH">🔴 High Priority</option>
              <option value="MEDIUM">🟡 Medium Priority</option>
              <option value="LOW">🟢 Low Priority</option>
            </select>
            <input
              type="date"
              value={form.dueDate}
              onChange={e => setForm({ ...form, dueDate: e.target.value })}
              style={{ padding: '10px', borderRadius: '8px', border: '1px solid #e2e8f0', fontSize: '14px' }}
            />
          </div>
          <button
            onClick={createTask}
            style={{ background: 'linear-gradient(135deg, #6366f1, #8b5cf6)', color: 'white', border: 'none', padding: '12px 24px', borderRadius: '8px', cursor: 'pointer', fontSize: '15px', fontWeight: '600', width: '100%' }}
          >
            Create Task
          </button>
        </div>

        {/* Filter + Search */}
        <div style={{ display: 'flex', gap: '12px', marginBottom: '16px', flexWrap: 'wrap' }}>
          {['ALL', 'PENDING', 'COMPLETED'].map(f => (
            <button key={f} onClick={() => setFilter(f)}
              style={{ padding: '8px 20px', borderRadius: '99px', border: 'none', cursor: 'pointer', fontWeight: '600', fontSize: '13px', background: filter === f ? '#6366f1' : '#e2e8f0', color: filter === f ? 'white' : '#64748b' }}>
              {f}
            </button>
          ))}
          <input
            placeholder="🔍 Search tasks..."
            value={search}
            onChange={e => setSearch(e.target.value)}
            style={{ flex: 1, padding: '8px 14px', borderRadius: '99px', border: '1px solid #e2e8f0', fontSize: '14px', minWidth: '180px' }}
          />
        </div>

        {/* Task List */}
        {loading ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#6366f1', fontSize: '18px' }}>Loading tasks...</div>
        ) : filtered.length === 0 ? (
          <div style={{ textAlign: 'center', padding: '40px', color: '#94a3b8', fontSize: '16px' }}>No tasks found. Create one above!</div>
        ) : (
          filtered.map(task => {
            const pc = PRIORITY_COLORS[task.priority] || PRIORITY_COLORS.MEDIUM;
            const isOverdue = task.dueDate && new Date(task.dueDate) < new Date() && task.status !== 'COMPLETED';
            return (
              <div key={task.taskId} style={{ background: pc.bg, border: `1.5px solid ${pc.border}`, borderRadius: '12px', padding: '16px', marginBottom: '12px', boxShadow: '0 1px 4px rgba(0,0,0,0.08)' }}>
                <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start' }}>
                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                      <span style={{ fontWeight: '700', fontSize: '16px', color: '#1e293b', textDecoration: task.status === 'COMPLETED' ? 'line-through' : 'none' }}>
                        {task.title}
                      </span>
                      <span style={{ background: pc.badge, color: 'white', padding: '2px 10px', borderRadius: '99px', fontSize: '11px', fontWeight: '700' }}>
                        {task.priority}
                      </span>
                      {task.status === 'COMPLETED' && (
                        <span style={{ background: '#22c55e', color: 'white', padding: '2px 10px', borderRadius: '99px', fontSize: '11px', fontWeight: '700' }}>✓ DONE</span>
                      )}
                      {isOverdue && (
                        <span style={{ background: '#ef4444', color: 'white', padding: '2px 10px', borderRadius: '99px', fontSize: '11px', fontWeight: '700' }}>⚠ OVERDUE</span>
                      )}
                    </div>
                    {task.description && (
                      <p style={{ margin: '6px 0 0', color: '#475569', fontSize: '14px' }}>{task.description}</p>
                    )}
                    {task.dueDate && (
                      <p style={{ margin: '4px 0 0', color: '#64748b', fontSize: '12px' }}>📅 Due: {task.dueDate}</p>
                    )}
                  </div>
                  <div style={{ display: 'flex', gap: '8px', marginLeft: '12px' }}>
                    <button onClick={() => toggleStatus(task)}
                      style={{ background: task.status === 'COMPLETED' ? '#f59e0b' : '#22c55e', color: 'white', border: 'none', padding: '8px 14px', borderRadius: '8px', cursor: 'pointer', fontSize: '13px', fontWeight: '600' }}>
                      {task.status === 'COMPLETED' ? '↩ Undo' : '✓ Done'}
                    </button>
                    <button onClick={() => deleteTask(task.taskId)}
                      style={{ background: '#ef4444', color: 'white', border: 'none', padding: '8px 14px', borderRadius: '8px', cursor: 'pointer', fontSize: '13px', fontWeight: '600' }}>
                      🗑
                    </button>
                  </div>
                </div>
              </div>
            );
          })
        )}
      </div>
    </div>
  );
}