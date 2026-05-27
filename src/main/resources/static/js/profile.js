/**
 * profile.js — Firstech
 * JavaScript exclusivo da página pública de perfil (/profile/{id}).
 *
 * Espera que a página defina, antes de carregar este script:
 *   const PROFILE_USER_ID   = <Long>;
 *   const PROFILE_USER_NAME = "<String>";
 *   const INIT_CONN_STATUS  = "<String>";   // NONE | PENDING_SENT | PENDING_RECEIVED | ACCEPTED | SELF
 */

/* ── Estado ─────────────────────────────────────────────── */
let _connStatus = (typeof INIT_CONN_STATUS !== 'undefined') ? INIT_CONN_STATUS : 'NONE';

/* ── Auth token ─────────────────────────────────────────── */
function _token() { return localStorage.getItem('firstech_access_token') || ''; }

/* ── Topbar: fechar menu ao clicar fora ─────────────────── */
function toggleUserMenu(e) {
  e.stopPropagation();
  document.getElementById('user-menu')?.classList.toggle('hidden');
}
function closeUserMenu() {
  document.getElementById('user-menu')?.classList.add('hidden');
}
document.addEventListener('click', function (e) {
  const menu = document.getElementById('user-menu');
  const btn  = document.getElementById('user-avatar-btn');
  if (menu && !menu.classList.contains('hidden') &&
      !menu.contains(e.target) && e.target !== btn) {
    menu.classList.add('hidden');
  }
});

/* ── Logout ─────────────────────────────────────────────── */
async function doLogout() {
  try {
    await fetch('/api/auth/logout', {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + _token() }
    });
  } catch {}
  localStorage.removeItem('firstech_access_token');
  window.location.href = '/';
}

/* ── Navegar para o dashboard (com tab opcional) ────────── */
function goToDashboard(tab) {
  window.location.href = tab ? '/dashboard?tab=' + tab : '/dashboard';
}

/* ── Ação de conexão ────────────────────────────────────── */
async function handleConnectionAction() {
  if (_connStatus === 'NONE')             await _sendConnectRequest();
  else if (_connStatus === 'ACCEPTED')    _confirmDisconnect();
  else if (_connStatus === 'PENDING_RECEIVED') await _acceptRequest();
}

async function _sendConnectRequest() {
  try {
    const res = await fetch('/api/connections/request/' + PROFILE_USER_ID, {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + _token() }
    });
    if (!res.ok) throw new Error();
    const data = await res.json();
    _setConnStatus(data.status || 'PENDING_SENT');
  } catch { _showToast('Erro ao enviar pedido de conexão.', true); }
}

async function _acceptRequest() {
  try {
    const res = await fetch('/api/connections/accept/' + PROFILE_USER_ID, {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + _token() }
    });
    if (!res.ok) throw new Error();
    _setConnStatus('ACCEPTED');
    _showToast('Conexão aceita!');
  } catch { _showToast('Erro ao aceitar pedido.', true); }
}

function _confirmDisconnect() {
  const ok = confirm('Remover conexão com ' + PROFILE_USER_NAME + '?');
  if (ok) _removeConnection();
}

async function _removeConnection() {
  try {
    const res = await fetch('/api/connections/' + PROFILE_USER_ID, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + _token() }
    });
    if (!res.ok) throw new Error();
    _setConnStatus('NONE');
    _showToast('Conexão removida.');
  } catch { _showToast('Erro ao remover conexão.', true); }
}

/* ── Atualiza botão de conectar ─────────────────────────── */
function _setConnStatus(status) {
  _connStatus = status;
  const btn = document.getElementById('profile-connect-btn');
  if (!btn) return;

  const cfgs = {
    'NONE': {
      html: '<i class="ti ti-user-plus"></i><span>Conectar</span>',
      cls:  'bg-purple text-white border-transparent hover:opacity-90',
      disabled: false
    },
    'PENDING_SENT': {
      html: '<i class="ti ti-check"></i><span>Solicitado</span>',
      cls:  'bg-bg3 text-text3 border-border2 cursor-default',
      disabled: true
    },
    'PENDING_RECEIVED': {
      html: '<i class="ti ti-user-check"></i><span>Aceitar pedido</span>',
      cls:  'border text-green hover:opacity-90',
      style: 'background:rgba(5,150,105,.1);border-color:rgba(5,150,105,.3)',
      disabled: false
    },
    'ACCEPTED': {
      html: '<i class="ti ti-check"></i><span>Conectado</span>',
      cls:  'border text-green hover:bg-red/10 hover:text-red hover:border-red/40 transition-colors',
      style: 'background:rgba(5,150,105,.08);border-color:rgba(5,150,105,.3)',
      title: 'Clique para remover conexão',
      disabled: false
    }
  };

  const cfg = cfgs[status] || cfgs['NONE'];
  btn.innerHTML  = cfg.html;
  btn.className  = `flex items-center gap-2 px-4 py-[9px] rounded-[10px] border font-semibold text-[13px] cursor-pointer font-sans transition-all duration-200 ${cfg.cls}`;
  btn.style.cssText = cfg.style || '';
  btn.disabled   = cfg.disabled;
  if (cfg.title) btn.title = cfg.title; else btn.removeAttribute('title');
}

/* ── Mensagem ────────────────────────────────────────────── */
function messageFromProfile() {
  const name = encodeURIComponent(PROFILE_USER_NAME || '');
  window.location.href = '/dashboard?openChat=' + PROFILE_USER_ID + '&chatName=' + name;
}

/* ── Toast simples ──────────────────────────────────────── */
function _showToast(msg, isError) {
  let t = document.getElementById('profile-toast');
  if (!t) {
    t = document.createElement('div');
    t.id = 'profile-toast';
    t.style.cssText = 'position:fixed;bottom:24px;left:50%;transform:translateX(-50%);z-index:9999;padding:10px 20px;border-radius:10px;font-size:13px;font-weight:600;transition:opacity .3s;pointer-events:none;font-family:inherit';
    document.body.appendChild(t);
  }
  t.textContent = msg;
  t.style.background   = isError ? 'rgba(220,38,38,.92)' : 'rgba(5,150,105,.92)';
  t.style.color        = '#fff';
  t.style.border       = isError ? '1px solid rgba(220,38,38,.6)' : '1px solid rgba(5,150,105,.6)';
  t.style.opacity      = '1';
  clearTimeout(t._timer);
  t._timer = setTimeout(() => { t.style.opacity = '0'; }, 3000);
}

/* ── Init ────────────────────────────────────────────────── */
document.addEventListener('DOMContentLoaded', function () {
  _setConnStatus(_connStatus);
});
