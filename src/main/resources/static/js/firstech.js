// ============================================================
//  Firstech — Scripts principais
//  Requer: firstech.css + Tabler Icons carregados no <head>
// ============================================================

/* ── NAVEGAÇÃO ENTRE TELAS ── */
function go(name) {
  document.querySelectorAll('.screen, .port-screen')
    .forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-btn')
    .forEach(b => b.classList.remove('active'));

  document.getElementById('s-' + name).classList.add('active');
  document.getElementById('nb-' + name).classList.add('active');
}

/* ── FILTROS (toggle individual) ── */
function ftoggle(el) {
  el.classList.toggle('active');
}

/* ── FILTROS DE BUSCA (exclusivo) ── */
function stab(el) {
  el.closest('.filter-tabs')
    .querySelectorAll('.ftab')
    .forEach(t => t.classList.remove('active'));
  el.classList.add('active');
}

/* ── MODAL DE NOVO POST ── */
function openModal() {
  document.getElementById('post-overlay').classList.add('open');
  // foca o título após a animação começar
  setTimeout(() => document.getElementById('modal-title').focus(), 80);
}

function closeModal() {
  document.getElementById('post-overlay').classList.remove('open');
}

function handleOverlayClick(e) {
  if (e.target === document.getElementById('post-overlay')) closeModal();
}

/* ── TAGS DO MODAL ── */
function removeTag(btn) {
  btn.closest('.m-tag').remove();
}

function handleTagKey(e) {
  if ((e.key === 'Enter' || e.key === ',') && e.target.value.trim()) {
    e.preventDefault();
    const val = e.target.value.trim().replace(/,$/, '');
    if (!val) return;

    const tag = document.createElement('div');
    tag.className = 'm-tag';
    tag.innerHTML = val + ' <button onclick="removeTag(this)" title="remover">×</button>';
    document.getElementById('active-tags').insertBefore(tag, e.target);
    e.target.value = '';
  }
}

/* ── PUBLICAR POST ── */
function publishPost() {
  const title = document.getElementById('modal-title').value || 'Novo projeto';

  closeModal();

  // Cria o card no feed
  const feed     = document.querySelector('#s-home .col-feed');
  const composer = feed.querySelector('.composer');
  const post     = document.createElement('div');

  post.className     = 'post';
  post.style.animation = 'modalIn .3s ease';
  post.innerHTML = `
    <div class="post-head">
      <div class="post-av" style="background:linear-gradient(135deg,#6d28d9,#8b5cf6)">G</div>
      <div style="flex:1">
        <div class="post-author">Gustavo Marmo</div>
        <div class="post-meta">Back-end Dev · TIVIT · agora mesmo</div>
      </div>
      <i class="ti ti-dots" style="color:var(--text3);cursor:pointer"></i>
    </div>
    <div class="post-text">${title}</div>
    <div class="post-actions">
      <button class="act-btn"><i class="ti ti-heart"></i>0</button>
      <button class="act-btn"><i class="ti ti-message-circle"></i>0</button>
      <button class="act-btn"><i class="ti ti-share"></i>Compartilhar</button>
    </div>`;

  composer.insertAdjacentElement('afterend', post);

  // Limpa os campos do modal
  document.getElementById('modal-title').value = '';
  document.getElementById('modal-sub').value   = '';
  document.getElementById('modal-desc').value  = '';
}

/* ── FECHAR MODAL COM ESC ── */
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') closeModal();
});
