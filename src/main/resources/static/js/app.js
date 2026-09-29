/**
 * TaskNest - Frontend Single Page Application Logic
 */

const API_BASE = '';

// Application State
const state = {
  currentUser: null,
  users: [],
  taskLists: [],
  currentView: 'all', // 'all', 'today', 'overdue', or 'list'
  selectedListId: null,
  tasks: [],
  dashboard: {},
  filters: {
    search: '',
    priority: '',
    status: '',
    dueDate: ''
  }
};

// ===================================================================
// API Helper
// ===================================================================
async function apiRequest(endpoint, method = 'GET', data = null) {
  const options = {
    method,
    headers: {
      'Accept': 'application/json'
    }
  };

  if (data && (method === 'POST' || method === 'PUT')) {
    options.headers['Content-Type'] = 'application/json';
    options.body = JSON.stringify(data);
  }

  try {
    const res = await fetch(`${API_BASE}${endpoint}`, options);

    if (res.status === 204) {
      return null;
    }

    const json = await res.json().catch(() => null);

    if (!res.ok) {
      const errorMsg = json?.message || `Request failed with status ${res.status}`;
      throw new Error(errorMsg);
    }

    return json;
  } catch (err) {
    console.error(`API Error on [${method} ${endpoint}]:`, err);
    throw err;
  }
}

// ===================================================================
// Toast Notifications
// ===================================================================
function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;

  const icon = type === 'success' ? '✅' : type === 'error' ? '⚠️' : 'ℹ️';
  toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;

  container.appendChild(toast);

  // Trigger animation
  requestAnimationFrame(() => {
    toast.classList.add('show');
  });

  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 250);
  }, 3500);
}

// ===================================================================
// Initialization
// ===================================================================
document.addEventListener('DOMContentLoaded', async () => {
  initEventListeners();
  initModalListeners();
  
  // Set default due date in task creation modal to today
  const todayStr = new Date().toISOString().split('T')[0];
  document.getElementById('task-duedate').value = todayStr;

  await loadUsers();
});

// ===================================================================
// User Management & Switching
// ===================================================================
async function loadUsers() {
  try {
    const users = await apiRequest('/api/users');
    state.users = users || [];

    const selectEl = document.getElementById('user-select');
    selectEl.innerHTML = '';

    if (state.users.length === 0) {
      selectEl.innerHTML = '<option value="">No Users</option>';
      return;
    }

    state.users.forEach(user => {
      const opt = document.createElement('option');
      opt.value = user.id;
      opt.textContent = `${user.name} (${user.email})`;
      selectEl.appendChild(opt);
    });

    // Check stored user or redirect to login
    const savedUserId = localStorage.getItem('tasknest_active_user_id');
    const matched = state.users.find(u => u.id == savedUserId);

    if (!savedUserId || !matched) {
      window.location.href = 'login.html';
      return;
    }

    selectEl.value = matched.id;
    await switchUser(matched.id);
  } catch (err) {
    showToast(`Failed to load users: ${err.message}`, 'error');
  }
}

async function switchUser(userId) {
  const user = state.users.find(u => u.id == userId);
  if (!user) return;

  state.currentUser = user;
  localStorage.setItem('tasknest_active_user_id', user.id);
  localStorage.setItem('tasknest_active_user', JSON.stringify(user));

  // Update Avatar initials
  const initials = user.name ? user.name.charAt(0).toUpperCase() : 'U';
  document.getElementById('current-user-avatar').textContent = initials;

  // Default view to 'all'
  state.currentView = 'all';
  state.selectedListId = null;

  // Reset filters
  resetFiltersState();

  await Promise.all([
    loadDashboard(),
    loadTaskLists()
  ]);

  await loadTasks();
}

// ===================================================================
// Dashboard Metrics
// ===================================================================
async function loadDashboard() {
  if (!state.currentUser) return;
  try {
    const data = await apiRequest(`/api/users/${state.currentUser.id}/dashboard`);
    state.dashboard = data || {};

    document.getElementById('stat-total-lists').textContent = data.totalTaskLists ?? 0;
    document.getElementById('stat-total-tasks').textContent = data.totalTasks ?? 0;
    document.getElementById('stat-completed-tasks').textContent = data.completedTasks ?? 0;
    document.getElementById('stat-incomplete-tasks').textContent = data.incompleteTasks ?? 0;
    document.getElementById('stat-overdue-tasks').textContent = data.overdueTasks ?? 0;
    document.getElementById('stat-today-tasks').textContent = data.tasksDueToday ?? 0;

    // Sidebar counts
    document.getElementById('sidebar-count-all').textContent = data.totalTasks ?? 0;
    document.getElementById('sidebar-count-today').textContent = data.tasksDueToday ?? 0;
    document.getElementById('sidebar-count-overdue').textContent = data.overdueTasks ?? 0;
  } catch (err) {
    console.error('Error fetching dashboard stats:', err);
  }
}

// ===================================================================
// Task Lists
// ===================================================================
async function loadTaskLists() {
  if (!state.currentUser) return;
  try {
    const lists = await apiRequest(`/api/users/${state.currentUser.id}/lists`);
    state.taskLists = lists || [];

    renderSidebarLists();
    populateTaskModalListOptions();
  } catch (err) {
    showToast(`Failed to load task lists: ${err.message}`, 'error');
  }
}

function renderSidebarLists() {
  const container = document.getElementById('custom-task-lists');
  container.innerHTML = '';

  if (state.taskLists.length === 0) {
    container.innerHTML = `
      <li style="padding: 0.6rem 0.85rem; font-size: 0.8rem; color: var(--text-faint);">
        No task lists created yet.
      </li>
    `;
    return;
  }

  state.taskLists.forEach(list => {
    const li = document.createElement('li');
    const isActive = state.currentView === 'list' && state.selectedListId === list.id;
    li.className = `menu-item ${isActive ? 'active' : ''}`;
    li.dataset.listId = list.id;

    li.innerHTML = `
      <div class="menu-item-left">
        <span class="menu-item-icon">📁</span>
        <span title="${escapeHtml(list.name)}">${escapeHtml(list.name)}</span>
      </div>
      <div style="display: flex; align-items: center; gap: 0.4rem;">
        <span class="badge-count">${list.taskCount ?? 0}</span>
        <div class="custom-list-actions">
          <button class="btn-icon-tiny btn-edit-list" data-id="${list.id}" title="Edit list">✏️</button>
          <button class="btn-icon-tiny btn-delete-list" data-id="${list.id}" title="Delete list">🗑️</button>
        </div>
      </div>
    `;

    li.addEventListener('click', (e) => {
      if (e.target.closest('.custom-list-actions')) return;
      selectListView(list.id);
    });

    const editBtn = li.querySelector('.btn-edit-list');
    editBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      openEditListModal(list);
    });

    const delBtn = li.querySelector('.btn-delete-list');
    delBtn.addEventListener('click', (e) => {
      e.stopPropagation();
      confirmDeleteList(list);
    });

    container.appendChild(li);
  });
}

function populateTaskModalListOptions() {
  const createSelect = document.getElementById('task-target-list');
  const moveSelect = document.getElementById('move-target-list');
  const manualSelect = document.getElementById('manual-task-list');

  createSelect.innerHTML = '';
  moveSelect.innerHTML = '';
  if (manualSelect) manualSelect.innerHTML = '';

  if (state.taskLists.length === 0) {
    createSelect.innerHTML = '<option value="">No lists available (Create one first)</option>';
    moveSelect.innerHTML = '<option value="">No lists available</option>';
    if (manualSelect) manualSelect.innerHTML = '<option value="">(No lists yet - use sidebar to add)</option>';
    return;
  }

  state.taskLists.forEach(list => {
    const opt1 = document.createElement('option');
    opt1.value = list.id;
    opt1.textContent = list.name;
    createSelect.appendChild(opt1);

    const opt2 = document.createElement('option');
    opt2.value = list.id;
    opt2.textContent = list.name;
    moveSelect.appendChild(opt2);

    if (manualSelect) {
      const opt3 = document.createElement('option');
      opt3.value = list.id;
      opt3.textContent = `📁 ${list.name}`;
      manualSelect.appendChild(opt3);
    }
  });

  if (state.selectedListId) {
    createSelect.value = state.selectedListId;
    if (manualSelect) manualSelect.value = state.selectedListId;
  }
}

// ===================================================================
// Views Navigation
// ===================================================================
function selectView(viewName) {
  state.currentView = viewName;
  state.selectedListId = null;

  // Update UI sidebar items
  document.querySelectorAll('.sidebar-menu .menu-item').forEach(el => el.classList.remove('active'));
  const activeEl = document.querySelector(`.sidebar-menu [data-view="${viewName}"]`);
  if (activeEl) activeEl.classList.add('active');

  const titleEl = document.getElementById('view-title');
  const descEl = document.getElementById('view-description');
  const headerActions = document.getElementById('view-header-actions');
  const overdueBanner = document.getElementById('overdue-banner');

  headerActions.innerHTML = '';

  if (viewName === 'all') {
    titleEl.textContent = 'All Tasks';
    descEl.textContent = 'Showing all tasks across your organized task lists.';
    overdueBanner.style.display = 'none';
  } else if (viewName === 'today') {
    titleEl.textContent = "Today's Tasks";
    descEl.textContent = 'Tasks scheduled for completion today.';
    overdueBanner.style.display = 'none';
  } else if (viewName === 'overdue') {
    titleEl.textContent = '⚠️ Overdue Tasks';
    descEl.textContent = 'Incomplete tasks past their scheduled due date.';
    overdueBanner.style.display = 'flex';
  }

  loadTasks();
}

function selectListView(listId) {
  state.currentView = 'list';
  state.selectedListId = listId;

  document.querySelectorAll('.sidebar-menu .menu-item').forEach(el => el.classList.remove('active'));
  const activeEl = document.querySelector(`.sidebar-menu [data-list-id="${listId}"]`);
  if (activeEl) activeEl.classList.add('active');

  const list = state.taskLists.find(l => l.id === listId);
  const titleEl = document.getElementById('view-title');
  const descEl = document.getElementById('view-description');
  const headerActions = document.getElementById('view-header-actions');
  const overdueBanner = document.getElementById('overdue-banner');

  overdueBanner.style.display = 'none';

  if (list) {
    titleEl.textContent = `📁 ${list.name}`;
    descEl.textContent = list.description || 'Personal task list.';

    headerActions.innerHTML = `
      <div style="display: flex; gap: 0.5rem;">
        <button class="btn btn-secondary btn-sm" id="btn-view-edit-list">Edit List</button>
        <button class="btn btn-danger btn-sm" id="btn-view-delete-list">Delete List</button>
      </div>
    `;

    document.getElementById('btn-view-edit-list').addEventListener('click', () => openEditListModal(list));
    document.getElementById('btn-view-delete-list').addEventListener('click', () => confirmDeleteList(list));
  }

  const manualListSelect = document.getElementById('manual-task-list');
  if (manualListSelect && listId) {
    manualListSelect.value = listId;
  }

  loadTasks();
}

// ===================================================================
// Tasks Management & Rendering
// ===================================================================
async function loadTasks() {
  if (!state.currentUser) return;
  const container = document.getElementById('tasks-list');
  container.innerHTML = `
    <div style="text-align: center; padding: 3rem; color: var(--text-muted);">
      Loading tasks...
    </div>
  `;

  try {
    let tasks = [];

    if (state.currentView === 'all') {
      const queryParams = new URLSearchParams();
      if (state.filters.priority) queryParams.append('priority', state.filters.priority);
      if (state.filters.status !== '') queryParams.append('completed', state.filters.status);
      if (state.filters.dueDate) queryParams.append('dueDate', state.filters.dueDate);

      const qs = queryParams.toString() ? `?${queryParams.toString()}` : '';
      tasks = await apiRequest(`/api/users/${state.currentUser.id}/tasks${qs}`);
    } else if (state.currentView === 'today') {
      tasks = await apiRequest(`/api/users/${state.currentUser.id}/tasks/today`);
      tasks = applyClientFilters(tasks);
    } else if (state.currentView === 'overdue') {
      tasks = await apiRequest(`/api/users/${state.currentUser.id}/tasks/overdue`);
      tasks = applyClientFilters(tasks);
    } else if (state.currentView === 'list' && state.selectedListId) {
      tasks = await apiRequest(`/api/lists/${state.selectedListId}/tasks`);
      tasks = applyClientFilters(tasks);
    }

    state.tasks = tasks || [];
    renderTasksList(state.tasks);
  } catch (err) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">❌</div>
        <div class="empty-title">Failed to load tasks</div>
        <div class="empty-subtitle">${escapeHtml(err.message)}</div>
      </div>
    `;
  }
}

function applyClientFilters(tasks) {
  return tasks.filter(task => {
    // Search keyword
    if (state.filters.search) {
      const q = state.filters.search.toLowerCase();
      const matchTitle = task.title && task.title.toLowerCase().includes(q);
      const matchDesc = task.description && task.description.toLowerCase().includes(q);
      if (!matchTitle && !matchDesc) return false;
    }
    // Priority
    if (state.filters.priority && task.priority !== state.filters.priority) {
      return false;
    }
    // Status
    if (state.filters.status !== '') {
      const wantCompleted = state.filters.status === 'true';
      if (task.completed !== wantCompleted) return false;
    }
    // Due Date
    if (state.filters.dueDate && task.dueDate !== state.filters.dueDate) {
      return false;
    }
    return true;
  });
}

function renderTasksList(tasks) {
  const container = document.getElementById('tasks-list');
  container.innerHTML = '';

  if (tasks.length === 0) {
    container.innerHTML = `
      <div class="empty-state">
        <div class="empty-icon">🪹</div>
        <div class="empty-title">No tasks found</div>
        <div class="empty-subtitle">
          ${state.currentView === 'overdue' 
            ? 'Great job! You have zero overdue tasks.' 
            : 'No tasks match your current view or filter criteria.'}
        </div>
        <button class="btn btn-primary" onclick="openCreateTaskModal()" style="margin-top: 0.5rem;">
          + Create a Task
        </button>
      </div>
    `;
    return;
  }

  const todayStr = new Date().toISOString().split('T')[0];

  tasks.forEach(task => {
    const card = document.createElement('div');
    const isOverdue = task.overdue;
    const isCompleted = task.completed;
    const isToday = task.dueDate === todayStr;

    card.className = `task-card ${isOverdue ? 'is-overdue' : ''} ${isCompleted ? 'is-completed' : ''}`;
    card.id = `task-card-${task.id}`;

    // Priority badge class
    const priorityClass = `badge-priority-${task.priority.toLowerCase()}`;

    // Format completed timestamp if present
    let completedBadge = '';
    if (isCompleted && task.completedAt) {
      const formattedDate = new Date(task.completedAt).toLocaleDateString(undefined, {
        month: 'short', day: 'numeric', hour: '2-digit', minute: '2-digit'
      });
      completedBadge = `<span class="badge badge-completed-at">✓ Finished ${formattedDate}</span>`;
    }

    card.innerHTML = `
      <div class="task-checkbox-wrapper" title="${isCompleted ? 'Mark as incomplete' : 'Mark as complete'}">
        <div class="custom-checkbox ${isCompleted ? 'checked' : ''}" data-task-id="${task.id}" data-completed="${isCompleted}">
          ${isCompleted ? '✓' : ''}
        </div>
      </div>

      <div class="task-details">
        <div class="task-title-row">
          <span class="task-title">${escapeHtml(task.title)}</span>
          <div class="task-actions">
            <button class="btn btn-secondary btn-sm btn-move-task" data-task-id="${task.id}" title="Move task to another list">
              ↔ Move
            </button>
            <button class="btn btn-secondary btn-sm btn-edit-task" data-task-id="${task.id}" title="Edit task">
              ✏️
            </button>
            <button class="btn btn-danger btn-sm btn-delete-task" data-task-id="${task.id}" title="Delete task">
              🗑️
            </button>
          </div>
        </div>

        ${task.description ? `<p class="task-description">${escapeHtml(task.description)}</p>` : ''}

        <div class="task-meta-row">
          <span class="badge ${priorityClass}">⚡ ${task.priority}</span>
          <span class="badge badge-due ${isToday ? 'today' : ''}">
            📅 ${task.dueDate} ${isToday ? '(Today)' : ''}
          </span>
          ${isOverdue ? '<span class="badge badge-overdue-tag">⚠️ OVERDUE</span>' : ''}
          ${task.taskListName ? `<span class="badge badge-list">📁 ${escapeHtml(task.taskListName)}</span>` : ''}
          ${completedBadge}
        </div>
      </div>
    `;

    // Checkbox toggle event
    const checkbox = card.querySelector('.custom-checkbox');
    checkbox.addEventListener('click', () => toggleTaskComplete(task.id, isCompleted));

    // Move task button
    const moveBtn = card.querySelector('.btn-move-task');
    moveBtn.addEventListener('click', () => openMoveTaskModal(task));

    // Edit task button
    const editBtn = card.querySelector('.btn-edit-task');
    editBtn.addEventListener('click', () => openEditTaskModal(task));

    // Delete task button
    const delBtn = card.querySelector('.btn-delete-task');
    delBtn.addEventListener('click', () => confirmDeleteTask(task));

    container.appendChild(card);
  });
}

// ===================================================================
// Task CRUD Handlers
// ===================================================================
async function toggleTaskComplete(taskId, currentlyCompleted) {
  try {
    const endpoint = currentlyCompleted 
      ? `/api/tasks/${taskId}/incomplete` 
      : `/api/tasks/${taskId}/complete`;

    const updatedTask = await apiRequest(endpoint, 'PATCH');
    showToast(
      currentlyCompleted ? 'Task marked as incomplete' : 'Task completed! Great job! 🎉', 
      'success'
    );

    await Promise.all([
      loadDashboard(),
      loadTaskLists(),
      loadTasks()
    ]);
  } catch (err) {
    showToast(`Error updating task status: ${err.message}`, 'error');
  }
}

async function handleCreateTask(e) {
  e.preventDefault();
  const listId = document.getElementById('task-target-list').value;
  const title = document.getElementById('task-title').value.trim();
  const description = document.getElementById('task-desc').value.trim();
  const dueDate = document.getElementById('task-duedate').value;
  const priorityRadio = document.querySelector('input[name="create-priority"]:checked');
  const priority = priorityRadio ? priorityRadio.value : 'MEDIUM';

  if (!listId) {
    showToast('Please select a valid task list', 'error');
    return;
  }

  const payload = {
    title,
    description: description || null,
    dueDate,
    priority,
    completed: false
  };

  try {
    await apiRequest(`/api/lists/${listId}/tasks`, 'POST', payload);
    showToast('Task created successfully!', 'success');
    closeModal('modal-create-task');
    document.getElementById('form-create-task').reset();

    await Promise.all([
      loadDashboard(),
      loadTaskLists(),
      loadTasks()
    ]);
  } catch (err) {
    showToast(`Error creating task: ${err.message}`, 'error');
  }
}

function openEditTaskModal(task) {
  document.getElementById('edit-task-id').value = task.id;
  document.getElementById('edit-task-title').value = task.title;
  document.getElementById('edit-task-desc').value = task.description || '';
  document.getElementById('edit-task-duedate').value = task.dueDate;

  const prioInput = document.querySelector(`input[name="edit-priority"][value="${task.priority}"]`);
  if (prioInput) prioInput.checked = true;

  openModal('modal-edit-task');
}

async function handleEditTask(e) {
  e.preventDefault();
  const taskId = document.getElementById('edit-task-id').value;
  const title = document.getElementById('edit-task-title').value.trim();
  const description = document.getElementById('edit-task-desc').value.trim();
  const dueDate = document.getElementById('edit-task-duedate').value;
  const priorityRadio = document.querySelector('input[name="edit-priority"]:checked');
  const priority = priorityRadio ? priorityRadio.value : 'MEDIUM';

  const payload = {
    title,
    description: description || null,
    dueDate,
    priority
  };

  try {
    await apiRequest(`/api/tasks/${taskId}`, 'PUT', payload);
    showToast('Task updated successfully!', 'success');
    closeModal('modal-edit-task');

    await Promise.all([
      loadDashboard(),
      loadTasks()
    ]);
  } catch (err) {
    showToast(`Error updating task: ${err.message}`, 'error');
  }
}

function openMoveTaskModal(task) {
  document.getElementById('move-task-id').value = task.id;
  document.getElementById('move-task-title-display').innerHTML = `
    Moving: <strong>${escapeHtml(task.title)}</strong><br>
    Current list: <span class="badge badge-list">${escapeHtml(task.taskListName || 'None')}</span>
  `;

  // Select target list excluding current if possible
  const select = document.getElementById('move-target-list');
  populateTaskModalListOptions();
  if (task.taskListId) {
    // default to another list if available
    const otherOption = Array.from(select.options).find(o => o.value != task.taskListId);
    if (otherOption) select.value = otherOption.value;
  }

  openModal('modal-move-task');
}

async function handleMoveTask(e) {
  e.preventDefault();
  const taskId = document.getElementById('move-task-id').value;
  const targetListId = document.getElementById('move-target-list').value;

  if (!targetListId) {
    showToast('Please select a destination list', 'error');
    return;
  }

  try {
    await apiRequest(`/api/tasks/${taskId}/move/${targetListId}`, 'PATCH');
    showToast('Task moved successfully!', 'success');
    closeModal('modal-move-task');

    await Promise.all([
      loadDashboard(),
      loadTaskLists(),
      loadTasks()
    ]);
  } catch (err) {
    showToast(`Failed to move task: ${err.message}`, 'error');
  }
}

async function confirmDeleteTask(task) {
  if (!confirm(`Are you sure you want to delete task "${task.title}"?`)) {
    return;
  }

  try {
    await apiRequest(`/api/tasks/${task.id}`, 'DELETE');
    showToast('Task deleted', 'info');

    await Promise.all([
      loadDashboard(),
      loadTaskLists(),
      loadTasks()
    ]);
  } catch (err) {
    showToast(`Failed to delete task: ${err.message}`, 'error');
  }
}

// ===================================================================
// Task List CRUD Handlers
// ===================================================================
async function handleCreateList(e) {
  e.preventDefault();
  if (!state.currentUser) return;

  const name = document.getElementById('list-name').value.trim();
  const description = document.getElementById('list-desc').value.trim();

  const payload = {
    name,
    description: description || null
  };

  try {
    const created = await apiRequest(`/api/users/${state.currentUser.id}/lists`, 'POST', payload);
    showToast(`Task list "${created.name}" created!`, 'success');
    closeModal('modal-create-list');
    document.getElementById('form-create-list').reset();

    await Promise.all([
      loadDashboard(),
      loadTaskLists()
    ]);

    // Automatically navigate to the new list
    selectListView(created.id);
  } catch (err) {
    showToast(`Error creating list: ${err.message}`, 'error');
  }
}

function openEditListModal(list) {
  document.getElementById('edit-list-id').value = list.id;
  document.getElementById('edit-list-name').value = list.name;
  document.getElementById('edit-list-desc').value = list.description || '';
  openModal('modal-edit-list');
}

async function handleEditList(e) {
  e.preventDefault();
  const listId = document.getElementById('edit-list-id').value;
  const name = document.getElementById('edit-list-name').value.trim();
  const description = document.getElementById('edit-list-desc').value.trim();

  const payload = {
    name,
    description: description || null
  };

  try {
    await apiRequest(`/api/lists/${listId}`, 'PUT', payload);
    showToast('Task list updated successfully!', 'success');
    closeModal('modal-edit-list');

    await loadTaskLists();
    if (state.currentView === 'list' && state.selectedListId == listId) {
      selectListView(Number(listId));
    }
  } catch (err) {
    showToast(`Error updating list: ${err.message}`, 'error');
  }
}

async function confirmDeleteList(list) {
  if (!confirm(`Are you sure you want to delete task list "${list.name}"?\nAll tasks within this list will also be permanently deleted!`)) {
    return;
  }

  try {
    await apiRequest(`/api/lists/${list.id}`, 'DELETE');
    showToast(`Task list "${list.name}" deleted`, 'info');

    // If currently viewing deleted list, reset to 'all'
    if (state.currentView === 'list' && state.selectedListId == list.id) {
      selectView('all');
    }

    await Promise.all([
      loadDashboard(),
      loadTaskLists()
    ]);
  } catch (err) {
    showToast(`Failed to delete list: ${err.message}`, 'error');
  }
}

// ===================================================================
// User Creation Handler
// ===================================================================
async function handleCreateUser(e) {
  e.preventDefault();
  const name = document.getElementById('user-name').value.trim();
  const email = document.getElementById('user-email').value.trim();
  const password = document.getElementById('user-password').value;

  const payload = { name, email, password };

  try {
    const newUser = await apiRequest('/api/users', 'POST', payload);
    showToast(`User ${newUser.name} created!`, 'success');
    closeModal('modal-create-user');
    document.getElementById('form-create-user').reset();

    // Reload users and switch to new user
    await loadUsers();
    document.getElementById('user-select').value = newUser.id;
    await switchUser(newUser.id);
  } catch (err) {
    showToast(`Error creating user: ${err.message}`, 'error');
  }
}

// ===================================================================
// Filter & Search Controls
// ===================================================================
function initEventListeners() {
  // Brand logo click
  document.getElementById('nav-brand-link').addEventListener('click', (e) => {
    e.preventDefault();
    selectView('all');
  });

  // User select change
  document.getElementById('user-select').addEventListener('change', (e) => {
    switchUser(e.target.value);
  });

  // Logout button
  const btnLogout = document.getElementById('btn-logout');
  if (btnLogout) {
    btnLogout.addEventListener('click', () => {
      localStorage.removeItem('tasknest_active_user');
      localStorage.removeItem('tasknest_active_user_id');
      window.location.href = 'login.html?logout=true';
    });
  }

  // Smart View Clicks
  document.getElementById('view-all-tasks').addEventListener('click', () => selectView('all'));
  document.getElementById('view-today-tasks').addEventListener('click', () => selectView('today'));
  document.getElementById('view-overdue-tasks').addEventListener('click', () => selectView('overdue'));

  // Metrics Card Clicks for quick navigation
  document.getElementById('metric-card-total').addEventListener('click', () => selectView('all'));
  document.getElementById('metric-card-today').addEventListener('click', () => selectView('today'));
  document.getElementById('metric-card-overdue').addEventListener('click', () => selectView('overdue'));

  // Search input debounced
  let searchTimeout = null;
  document.getElementById('filter-search').addEventListener('input', (e) => {
    clearTimeout(searchTimeout);
    searchTimeout = setTimeout(() => {
      state.filters.search = e.target.value.trim();
      loadTasks();
    }, 250);
  });

  // Priority filter
  document.getElementById('filter-priority').addEventListener('change', (e) => {
    state.filters.priority = e.target.value;
    loadTasks();
  });

  // Status filter
  document.getElementById('filter-status').addEventListener('change', (e) => {
    state.filters.status = e.target.value;
    loadTasks();
  });

  // Due Date filter
  document.getElementById('filter-duedate').addEventListener('change', (e) => {
    state.filters.dueDate = e.target.value;
    loadTasks();
  });

  // Reset Filters
  document.getElementById('btn-clear-filters').addEventListener('click', () => {
    resetFiltersState();
    loadTasks();
  });

  // Form Submissions
  document.getElementById('form-create-task').addEventListener('submit', handleCreateTask);
  document.getElementById('form-edit-task').addEventListener('submit', handleEditTask);
  document.getElementById('form-move-task').addEventListener('submit', handleMoveTask);
  document.getElementById('form-create-list').addEventListener('submit', handleCreateList);
  document.getElementById('form-edit-list').addEventListener('submit', handleEditList);
  document.getElementById('form-create-user').addEventListener('submit', handleCreateUser);

  // Initialize Manual Creation Tools
  initManualCreationStudio();
  initSidebarQuickList();
  initWorkspaceDataTools();
}

// ===================================================================
// Manual Creation Studio & Quick Add
// ===================================================================
state.manualPriority = 'MEDIUM';

function initManualCreationStudio() {
  const titleInput = document.getElementById('manual-task-title');
  const addBtn = document.getElementById('btn-manual-add-task');
  const dateInput = document.getElementById('manual-task-date');
  const toggleNotesBtn = document.getElementById('btn-toggle-manual-notes');
  const descInput = document.getElementById('manual-task-desc');

  if (!titleInput) return;

  // Set default date to today
  const todayStr = new Date().toISOString().split('T')[0];
  dateInput.value = todayStr;

  // Toggle notes
  if (toggleNotesBtn && descInput) {
    toggleNotesBtn.addEventListener('click', () => {
      descInput.classList.toggle('show');
      if (descInput.classList.contains('show')) {
        toggleNotesBtn.textContent = '– Hide Description';
        descInput.focus();
      } else {
        toggleNotesBtn.textContent = '+ Add Description';
      }
    });
  }

  // Date chips
  document.querySelectorAll('.manual-creation-card .date-chip').forEach(chip => {
    chip.addEventListener('click', () => {
      document.querySelectorAll('.manual-creation-card .date-chip').forEach(c => c.classList.remove('active'));
      chip.classList.add('active');
      const offset = parseInt(chip.getAttribute('data-offset') || '0', 10);
      const targetDate = new Date();
      targetDate.setDate(targetDate.getDate() + offset);
      dateInput.value = targetDate.toISOString().split('T')[0];
    });
  });

  // Date input change clears chip active
  dateInput.addEventListener('change', () => {
    document.querySelectorAll('.manual-creation-card .date-chip').forEach(c => c.classList.remove('active'));
  });

  // Priority buttons
  document.querySelectorAll('.manual-creation-card .priority-pill-btn').forEach(btn => {
    btn.addEventListener('click', () => {
      document.querySelectorAll('.manual-creation-card .priority-pill-btn').forEach(b => b.classList.remove('active'));
      btn.classList.add('active');
      state.manualPriority = btn.getAttribute('data-priority');
    });
  });

  // Add Task on button click
  addBtn.addEventListener('click', handleManualAddTask);

  // Add Task on Enter key inside title input
  titleInput.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      handleManualAddTask();
    }
  });
}

async function handleManualAddTask() {
  const titleInput = document.getElementById('manual-task-title');
  const title = titleInput.value.trim();
  if (!title) {
    showToast('Please enter a task title', 'error');
    titleInput.focus();
    return;
  }

  const listSelect = document.getElementById('manual-task-list');
  let listId = listSelect.value;
  if (!listId) {
    if (state.taskLists.length === 0) {
      showToast('Please create a task list first using the sidebar', 'error');
      document.getElementById('sidebar-quick-list-input').focus();
      return;
    }
    listId = state.taskLists[0].id;
  }

  const dateInput = document.getElementById('manual-task-date');
  const dueDate = dateInput.value || new Date().toISOString().split('T')[0];
  const descInput = document.getElementById('manual-task-desc');
  const description = descInput ? descInput.value.trim() : '';
  const priority = state.manualPriority || 'MEDIUM';

  const payload = {
    title,
    description: description || null,
    dueDate,
    priority,
    completed: false
  };

  try {
    const createdTask = await apiRequest(`/api/lists/${listId}/tasks`, 'POST', payload);
    showToast(`Task "${createdTask.title}" created successfully!`, 'success');
    titleInput.value = '';
    if (descInput) descInput.value = '';
    titleInput.focus();

    await Promise.all([
      loadDashboard(),
      loadTaskLists(),
      loadTasks()
    ]);
  } catch (err) {
    showToast(`Failed to create task: ${err.message}`, 'error');
  }
}

function initSidebarQuickList() {
  const input = document.getElementById('sidebar-quick-list-input');
  const btn = document.getElementById('btn-sidebar-quick-add-list');
  if (!input || !btn) return;

  const doAdd = async () => {
    const name = input.value.trim();
    if (!name) {
      input.focus();
      return;
    }
    try {
      const created = await apiRequest(`/api/users/${state.currentUser.id}/lists`, 'POST', {
        name,
        description: null
      });
      showToast(`Task list "${created.name}" created!`, 'success');
      input.value = '';
      await Promise.all([
        loadDashboard(),
        loadTaskLists()
      ]);
      selectListView(created.id);
    } catch (err) {
      showToast(`Failed to create list: ${err.message}`, 'error');
    }
  };

  btn.addEventListener('click', doAdd);
  input.addEventListener('keydown', (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      doAdd();
    }
  });
}

function initWorkspaceDataTools() {
  const clearBtn = document.getElementById('btn-clear-user-data');
  const reloadBtn = document.getElementById('btn-reload-sample-data');

  if (clearBtn) {
    clearBtn.addEventListener('click', async () => {
      if (!confirm("Start fresh for manual creation?\n\nThis will remove sample tasks and lists so you can manually build your own from scratch.")) {
        return;
      }
      try {
        await apiRequest(`/api/users/${state.currentUser.id}/clear-data`, 'POST');
        showToast('Workspace cleared! Ready for your manual lists & tasks.', 'info');
        selectView('all');
        await Promise.all([
          loadDashboard(),
          loadTaskLists(),
          loadTasks()
        ]);
      } catch (err) {
        showToast(`Failed to clear data: ${err.message}`, 'error');
      }
    });
  }

  if (reloadBtn) {
    reloadBtn.addEventListener('click', async () => {
      if (!confirm("Reload sample demo tasks and lists for this user?")) {
        return;
      }
      try {
        await apiRequest(`/api/users/${state.currentUser.id}/sample-data`, 'POST');
        showToast('Sample tasks and lists reloaded!', 'success');
        selectView('all');
        await Promise.all([
          loadDashboard(),
          loadTaskLists(),
          loadTasks()
        ]);
      } catch (err) {
        showToast(`Failed to reload sample data: ${err.message}`, 'error');
      }
    });
  }
}

function resetFiltersState() {
  state.filters = {
    search: '',
    priority: '',
    status: '',
    dueDate: ''
  };
  document.getElementById('filter-search').value = '';
  document.getElementById('filter-priority').value = '';
  document.getElementById('filter-status').value = '';
  document.getElementById('filter-duedate').value = '';
}

// ===================================================================
// Modal Management
// ===================================================================
function initModalListeners() {
  // Open buttons
  document.getElementById('btn-open-create-task-modal').addEventListener('click', openCreateTaskModal);
  document.getElementById('btn-open-create-list-modal').addEventListener('click', () => openModal('modal-create-list'));
  document.getElementById('btn-open-create-user-modal').addEventListener('click', () => openModal('modal-create-user'));

  // Close triggers
  document.querySelectorAll('[data-close-modal]').forEach(btn => {
    btn.addEventListener('click', () => {
      const modalId = btn.getAttribute('data-close-modal');
      closeModal(modalId);
    });
  });

  // Close when clicking outside modal container
  document.querySelectorAll('.modal-overlay').forEach(overlay => {
    overlay.addEventListener('click', (e) => {
      if (e.target === overlay) {
        closeModal(overlay.id);
      }
    });
  });

  // ESC key closes active modal
  window.addEventListener('keydown', (e) => {
    if (e.key === 'Escape') {
      const activeModal = document.querySelector('.modal-overlay.active');
      if (activeModal) closeModal(activeModal.id);
    }
  });
}

function openCreateTaskModal() {
  if (state.taskLists.length === 0) {
    showToast('Please create at least one task list first', 'info');
    openModal('modal-create-list');
    return;
  }
  populateTaskModalListOptions();
  openModal('modal-create-task');
}

function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.add('active');
}

function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) modal.classList.remove('active');
}

// Utility: HTML Escaping
function escapeHtml(str) {
  if (!str) return '';
  return String(str)
    .replace(/&/g, '&amp;')
    .replace(/</g, '&lt;')
    .replace(/>/g, '&gt;')
    .replace(/"/g, '&quot;')
    .replace(/'/g, '&#39;');
}
