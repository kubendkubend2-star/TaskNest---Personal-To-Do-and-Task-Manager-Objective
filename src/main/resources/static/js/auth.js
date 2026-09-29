/**
 * TaskNest - Authentication Controller Logic (auth.js)
 */

const API_BASE = '';

document.addEventListener('DOMContentLoaded', () => {
  initAuthUI();
  checkCurrentSession();
});

function initAuthUI() {
  // Tab Switching
  const tabSignIn = document.getElementById('tab-btn-signin');
  const tabSignUp = document.getElementById('tab-btn-signup');
  const panelSignIn = document.getElementById('panel-signin');
  const panelSignUp = document.getElementById('panel-signup');

  if (tabSignIn && tabSignUp) {
    tabSignIn.addEventListener('click', () => {
      tabSignIn.classList.add('active');
      tabSignIn.setAttribute('aria-selected', 'true');
      tabSignUp.classList.remove('active');
      tabSignUp.setAttribute('aria-selected', 'false');

      panelSignIn.style.display = 'block';
      panelSignUp.style.display = 'none';
      clearAlerts();
    });

    tabSignUp.addEventListener('click', () => {
      tabSignUp.classList.add('active');
      tabSignUp.setAttribute('aria-selected', 'true');
      tabSignIn.classList.remove('active');
      tabSignIn.setAttribute('aria-selected', 'false');

      panelSignUp.style.display = 'block';
      panelSignIn.style.display = 'none';
      clearAlerts();
    });
  }

  // Password Visibility Toggles
  setupPasswordToggle('btn-toggle-signin-password', 'signin-password');
  setupPasswordToggle('btn-toggle-signup-password', 'signup-password');

  // Password Strength Meter
  const signupPassInput = document.getElementById('signup-password');
  if (signupPassInput) {
    signupPassInput.addEventListener('input', (e) => {
      updatePasswordStrength(e.target.value);
    });
  }

  // Form Submissions
  const formSignIn = document.getElementById('form-signin');
  if (formSignIn) {
    formSignIn.addEventListener('submit', handleSignIn);
  }

  const formSignUp = document.getElementById('form-signup');
  if (formSignUp) {
    formSignUp.addEventListener('submit', handleSignUp);
  }

  // 1-Click Demo Buttons
  const demoAlexBtn = document.getElementById('btn-demo-alex');
  if (demoAlexBtn) {
    demoAlexBtn.addEventListener('click', () => {
      document.getElementById('signin-email').value = 'alex.mercer@tasknest.edu';
      document.getElementById('signin-password').value = 'secretPass123';
      handleSignIn(new Event('submit'));
    });
  }

  const demoMayaBtn = document.getElementById('btn-demo-maya');
  if (demoMayaBtn) {
    demoMayaBtn.addEventListener('click', () => {
      document.getElementById('signin-email').value = 'maya.lin@tasknest.edu';
      document.getElementById('signin-password').value = 'secureMaya!456';
      handleSignIn(new Event('submit'));
    });
  }

  // Forgot Password Hint
  const forgotPassLink = document.getElementById('link-forgot-password');
  if (forgotPassLink) {
    forgotPassLink.addEventListener('click', (e) => {
      e.preventDefault();
      showToast("Demo Hint: For Alex Mercer use 'secretPass123', for Maya Lin use 'secureMaya!456'. Or create a new account!", 'info');
    });
  }

  // Active session signout button
  const sessionSignoutBtn = document.getElementById('btn-session-signout');
  if (sessionSignoutBtn) {
    sessionSignoutBtn.addEventListener('click', () => {
      clearSession();
      const banner = document.getElementById('active-session-banner');
      if (banner) banner.style.display = 'none';
      showToast('Signed out of current account.', 'info');
    });
  }
}

function setupPasswordToggle(btnId, inputId) {
  const btn = document.getElementById(btnId);
  const input = document.getElementById(inputId);
  if (!btn || !input) return;

  btn.addEventListener('click', () => {
    const isPassword = input.type === 'password';
    input.type = isPassword ? 'text' : 'password';
    btn.textContent = isPassword ? '🙈' : '👁️';
    btn.setAttribute('aria-label', isPassword ? 'Hide password' : 'Show password');
  });
}

function updatePasswordStrength(password) {
  const container = document.getElementById('strength-container');
  const bar1 = document.getElementById('strength-bar-1');
  const bar2 = document.getElementById('strength-bar-2');
  const bar3 = document.getElementById('strength-bar-3');
  const text = document.getElementById('strength-text');

  if (!password) {
    container.style.display = 'none';
    return;
  }
  container.style.display = 'flex';

  let score = 0;
  if (password.length >= 6) score++;
  if (password.length >= 8 && /[0-9]/.test(password)) score++;
  if (password.length >= 10 && /[^A-Za-z0-9]/.test(password)) score++;

  // Reset bars
  [bar1, bar2, bar3].forEach(b => {
    b.style.background = 'rgba(255, 255, 255, 0.1)';
  });

  if (score === 1) {
    bar1.style.background = '#f43f5e'; // red
    text.textContent = 'Weak (min. 6 characters)';
    text.style.color = '#f43f5e';
  } else if (score === 2) {
    bar1.style.background = '#f59e0b';
    bar2.style.background = '#f59e0b'; // amber
    text.textContent = 'Medium (add special characters)';
    text.style.color = '#f59e0b';
  } else if (score >= 3) {
    bar1.style.background = '#10b981';
    bar2.style.background = '#10b981';
    bar3.style.background = '#10b981'; // green
    text.textContent = 'Strong password';
    text.style.color = '#10b981';
  }
}

function checkCurrentSession() {
  const params = new URLSearchParams(window.location.search);
  if (params.get('logout') === 'true') {
    clearSession();
    showToast('You have been signed out.', 'info');
    // Clean URL
    window.history.replaceState({}, document.title, window.location.pathname);
    return;
  }

  const storedUserJson = localStorage.getItem('tasknest_active_user');
  if (storedUserJson) {
    try {
      const user = JSON.parse(storedUserJson);
      if (user && user.id && user.name) {
        const banner = document.getElementById('active-session-banner');
        const nameEl = document.getElementById('active-session-name');
        if (banner && nameEl) {
          nameEl.textContent = user.name;
          banner.style.display = 'flex';
        }
      }
    } catch {
      clearSession();
    }
  }
}

function clearSession() {
  localStorage.removeItem('tasknest_active_user');
  localStorage.removeItem('tasknest_active_user_id');
}

// ===================================================================
// Sign In Handler
// ===================================================================
async function handleSignIn(e) {
  e.preventDefault();
  clearAlerts();

  const emailInput = document.getElementById('signin-email');
  const passwordInput = document.getElementById('signin-password');
  const submitBtn = document.getElementById('btn-submit-signin');

  const email = emailInput.value.trim();
  const password = passwordInput.value;

  if (!email) {
    showAlert('signin-alert', 'Please enter your email address.', 'error');
    emailInput.focus();
    return;
  }

  if (!isValidEmail(email)) {
    showAlert('signin-alert', 'Please enter a valid email address.', 'error');
    emailInput.focus();
    return;
  }

  if (!password) {
    showAlert('signin-alert', 'Please enter your password.', 'error');
    passwordInput.focus();
    return;
  }

  setButtonLoading(submitBtn, true, 'Signing in...');

  try {
    const res = await fetch(`${API_BASE}/api/auth/login`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({ email, password })
    });

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      const errorMsg = data?.message || 'Invalid email or password. Please verify your credentials.';
      showAlert('signin-alert', errorMsg, 'error');
      setButtonLoading(submitBtn, false);
      return;
    }

    // Success: save user
    saveActiveUser(data);
    showAlert('signin-alert', `Welcome back, ${data.name}! Redirecting to workspace...`, 'success');
    showToast(`Signed in successfully as ${data.name}`, 'success');

    setTimeout(() => {
      window.location.href = 'index.html';
    }, 450);

  } catch (err) {
    showAlert('signin-alert', `Network error: ${err.message}. Is the server running?`, 'error');
    setButtonLoading(submitBtn, false);
  }
}

// ===================================================================
// Sign Up Handler
// ===================================================================
async function handleSignUp(e) {
  e.preventDefault();
  clearAlerts();

  const nameInput = document.getElementById('signup-name');
  const emailInput = document.getElementById('signup-email');
  const passwordInput = document.getElementById('signup-password');
  const confirmPasswordInput = document.getElementById('signup-confirm-password');
  const submitBtn = document.getElementById('btn-submit-signup');

  const name = nameInput.value.trim();
  const email = emailInput.value.trim();
  const password = passwordInput.value;
  const confirmPassword = confirmPasswordInput.value;

  if (!name || name.length < 2) {
    showAlert('signup-alert', 'Please enter your full name (at least 2 characters).', 'error');
    nameInput.focus();
    return;
  }

  if (!email || !isValidEmail(email)) {
    showAlert('signup-alert', 'Please enter a valid email address.', 'error');
    emailInput.focus();
    return;
  }

  if (!password || password.length < 6) {
    showAlert('signup-alert', 'Password must be at least 6 characters long.', 'error');
    passwordInput.focus();
    return;
  }

  if (password !== confirmPassword) {
    showAlert('signup-alert', 'Passwords do not match. Please re-check.', 'error');
    confirmPasswordInput.focus();
    return;
  }

  setButtonLoading(submitBtn, true, 'Creating account...');

  try {
    const res = await fetch(`${API_BASE}/api/auth/register`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: JSON.stringify({ name, email, password })
    });

    const data = await res.json().catch(() => null);

    if (!res.ok) {
      const errorMsg = data?.message || 'Registration failed. Please check your information.';
      showAlert('signup-alert', errorMsg, 'error');
      setButtonLoading(submitBtn, false);
      return;
    }

    // Automatically log in the created user
    saveActiveUser(data);
    showAlert('signup-alert', `Account created successfully! Welcome to TaskNest, ${data.name}!`, 'success');
    showToast(`Welcome to TaskNest, ${data.name}!`, 'success');

    setTimeout(() => {
      window.location.href = 'index.html';
    }, 600);

  } catch (err) {
    showAlert('signup-alert', `Network error: ${err.message}. Is the server running?`, 'error');
    setButtonLoading(submitBtn, false);
  }
}

function saveActiveUser(user) {
  localStorage.setItem('tasknest_active_user', JSON.stringify(user));
  localStorage.setItem('tasknest_active_user_id', user.id);
}

function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email);
}

function showAlert(alertId, message, type = 'error') {
  const alertEl = document.getElementById(alertId);
  if (!alertEl) return;

  alertEl.className = `auth-alert show ${type}`;
  const icon = type === 'success' ? '✅' : '⚠️';
  alertEl.innerHTML = `<span class="alert-icon">${icon}</span><span class="alert-text">${escapeHtml(message)}</span>`;
}

function clearAlerts() {
  ['signin-alert', 'signup-alert'].forEach(id => {
    const el = document.getElementById(id);
    if (el) {
      el.className = 'auth-alert';
      el.innerHTML = '';
    }
  });
}

function setButtonLoading(button, isLoading, loadingText = 'Processing...') {
  if (!button) return;
  button.disabled = isLoading;

  if (isLoading) {
    button.dataset.originalHtml = button.innerHTML;
    button.innerHTML = `<div class="spinner-inline"></div><span>${loadingText}</span>`;
  } else if (button.dataset.originalHtml) {
    button.innerHTML = button.dataset.originalHtml;
  }
}

function showToast(message, type = 'info') {
  const container = document.getElementById('toast-container');
  if (!container) return;

  const toast = document.createElement('div');
  toast.className = `toast ${type}`;
  const icon = type === 'success' ? '✅' : type === 'error' ? '⚠️' : 'ℹ️';
  toast.innerHTML = `<span>${icon}</span><span>${escapeHtml(message)}</span>`;

  container.appendChild(toast);

  requestAnimationFrame(() => toast.classList.add('show'));

  setTimeout(() => {
    toast.classList.remove('show');
    setTimeout(() => toast.remove(), 250);
  }, 3500);
}

function escapeHtml(str) {
  if (!str) return '';
  const div = document.createElement('div');
  div.textContent = str;
  return div.innerHTML;
}
