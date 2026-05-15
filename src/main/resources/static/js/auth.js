/* auth.js — lógica de autenticação para as telas Thymeleaf */

// ─── Token helpers ────────────────────────────────────────────────────────────
const TOKEN_KEY   = 'firstech_access_token';
const REFRESH_KEY = 'firstech_refresh_token';

const tokenStorage = {
    getAccess:  ()          => localStorage.getItem(TOKEN_KEY),
    getRefresh: ()          => localStorage.getItem(REFRESH_KEY),
    set:        (a, r)      => { localStorage.setItem(TOKEN_KEY, a); localStorage.setItem(REFRESH_KEY, r); },
    clear:      ()          => { localStorage.removeItem(TOKEN_KEY); localStorage.removeItem(REFRESH_KEY); },
};

// ─── HTTP helper ──────────────────────────────────────────────────────────────
async function post(endpoint, body) {
    const res = await fetch('/api/auth' + endpoint, {
        method:  'POST',
        headers: { 'Content-Type': 'application/json' },
        body:    JSON.stringify(body),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
        const err   = new Error(data.message || 'Erro desconhecido.');
        err.details = data.details;
        err.status  = res.status;
        throw err;
    }
    return data;
}

// ─── UI helpers ───────────────────────────────────────────────────────────────
function setLoading(form, loading) {
    const btn = form.querySelector('.btn-primary');
    if (!btn) return;
    btn.disabled = loading;
    const icon = btn.querySelector('.btn-icon');
    if (icon) icon.textContent = loading ? 'hourglass_empty' : (btn.dataset.icon || 'login');
    const label = btn.querySelector('.btn-label');
    if (label) label.textContent = loading ? 'Aguarde...' : (btn.dataset.label || 'Entrar');
}

function showError(form, message) {
    let el = form.querySelector('.alert');
    if (!el) {
        el = document.createElement('div');
        el.className = 'alert alert-error';
        form.prepend(el);
    }
    el.className   = 'alert alert-error';
    el.textContent = message;
}

function showSuccess(form, message) {
    let el = form.querySelector('.alert');
    if (!el) {
        el = document.createElement('div');
        el.className = 'alert alert-success';
        form.prepend(el);
    }
    el.className   = 'alert alert-success';
    el.textContent = message;
}

function clearAlert(form) {
    form.querySelector('.alert')?.remove();
}

// ─── Toggle senha ─────────────────────────────────────────────────────────────
document.querySelectorAll('[data-toggle-password]').forEach(btn => {
    btn.addEventListener('click', () => {
        const targetId = btn.dataset.togglePassword;
        const input    = document.getElementById(targetId);
        if (!input) return;
        const isHidden = input.type === 'password';
        input.type     = isHidden ? 'text' : 'password';
        btn.querySelector('.material-symbols-outlined').textContent =
            isHidden ? 'visibility_off' : 'visibility';
    });
});

// ─── LOGIN ────────────────────────────────────────────────────────────────────
const loginForm = document.getElementById('loginForm');
if (loginForm) {
    loginForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert(loginForm);
        setLoading(loginForm, true);
        try {
            const data = await post('/login', {
                email:    loginForm.email.value.trim(),
                password: loginForm.password.value,
            });
            tokenStorage.set(data.accessToken, data.refreshToken);
            window.location.href = '/dashboard';
        } catch (err) {
            showError(loginForm, err.message);
        } finally {
            setLoading(loginForm, false);
        }
    });
}

// ─── REGISTER ─────────────────────────────────────────────────────────────────
const registerForm = document.getElementById('registerForm');
if (registerForm) {
    registerForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert(registerForm);

        const password = registerForm.password.value;
        if (password.length < 8) {
            showError(registerForm, 'A senha deve ter no mínimo 8 caracteres.');
            return;
        }

        setLoading(registerForm, true);
        try {
            const data = await post('/register', {
                name:     registerForm.name.value.trim(),
                email:    registerForm.email.value.trim(),
                password,
            });
            tokenStorage.set(data.accessToken, data.refreshToken);
            window.location.href = '/dashboard';
        } catch (err) {
            showError(registerForm, err.message);
        } finally {
            setLoading(registerForm, false);
        }
    });
}

// ─── FORGOT PASSWORD ──────────────────────────────────────────────────────────
const forgotForm = document.getElementById('forgotForm');
if (forgotForm) {
    forgotForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert(forgotForm);
        setLoading(forgotForm, true);
        try {
            // Backend retorna msg genérica por segurança — mostramos sucesso sempre
            await post('/forgot-password', { email: forgotForm.email.value.trim() })
                .catch(() => {});
            showSuccess(forgotForm, 'Se o e-mail estiver cadastrado, você receberá as instruções em breve.');
            forgotForm.reset();
        } finally {
            setLoading(forgotForm, false);
        }
    });
}

// ─── RESET PASSWORD ───────────────────────────────────────────────────────────
const resetForm = document.getElementById('resetForm');
if (resetForm) {
    resetForm.addEventListener('submit', async (e) => {
        e.preventDefault();
        clearAlert(resetForm);

        const newPassword = resetForm.newPassword.value;
        if (newPassword.length < 8) {
            showError(resetForm, 'A senha deve ter no mínimo 8 caracteres.');
            return;
        }

        setLoading(resetForm, true);
        try {
            await post('/reset-password', {
                token:       resetForm.token.value,
                newPassword,
            });
            showSuccess(resetForm, 'Senha redefinida com sucesso! Redirecionando...');
            setTimeout(() => { window.location.href = '/login'; }, 2000);
        } catch (err) {
            showError(resetForm, err.message);
        } finally {
            setLoading(resetForm, false);
        }
    });
}
