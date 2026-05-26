// ============================================================
//  Firstech — Scripts principais
//  Requer: Tabler Icons carregados no <head>
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

/* ═══════════════════════════════════════════════════════════
   MODAL DE POST — criar e editar
   ═══════════════════════════════════════════════════════════ */

/** ID do post sendo editado. null = modo criação. */
let editingPostId = null;

/**
 * Abre o modal de post.
 * @param {object|null} post - Se fornecido, entra em modo de edição.
 */
function openModal(post) {
  if (post) {
    editingPostId = Number(post.id);
    document.getElementById('post-modal-heading').textContent  = 'Editar post';
    document.getElementById('post-publish-label').textContent  = 'Salvar';
    document.getElementById('post-publish-icon').className     = 'ti ti-device-floppy text-sm';

    document.getElementById('modal-title').value = post.title   || '';
    document.getElementById('modal-desc').value  = post.content || '';

    // Repopula tags
    document.querySelectorAll('#active-tags .m-tag').forEach(t => t.remove());
    (post.tags || []).forEach(addPostTag);

    // Restaura vaga vinculada (se houver select)
    const jobSel = document.getElementById('modal-linked-job');
    if (jobSel) jobSel.value = post.linkedJobId || '';
  } else {
    editingPostId = null;
    resetPostModal();
  }

  document.getElementById('post-overlay').classList.add('open');
  setTimeout(() => document.getElementById('modal-desc').focus(), 80);
}

function closeModal() {
  document.getElementById('post-overlay').classList.remove('open');
  editingPostId = null;
}

function handleOverlayClick(e) {
  if (e.target === document.getElementById('post-overlay')) closeModal();
}

function resetPostModal() {
  document.getElementById('post-modal-heading').textContent  = 'Novo post';
  document.getElementById('post-publish-label').textContent  = 'Publicar';
  document.getElementById('post-publish-icon').className     = 'ti ti-send text-sm';
  document.getElementById('modal-title').value = '';
  document.getElementById('modal-desc').value  = '';
  document.querySelectorAll('#active-tags .m-tag').forEach(t => t.remove());
  const jobSel = document.getElementById('modal-linked-job');
  if (jobSel) jobSel.value = '';
}

/* ── Tags do modal de post ── */
function handleTagKey(e) {
  if ((e.key === 'Enter' || e.key === ',') && e.target.value.trim()) {
    e.preventDefault();
    const val = e.target.value.trim().replace(/,$/, '');
    if (!val) return;
    addPostTag(val);
    e.target.value = '';
  }
}

function addPostTag(val) {
  const tag = document.createElement('div');
  tag.className = 'm-tag flex items-center gap-[5px] bg-purplebg text-purple2 border border-border rounded-[20px] py-[3px] px-[10px] text-xs font-medium';
  tag.innerHTML = `${val}<button type="button" class="bg-transparent border-none text-purple2 cursor-pointer flex items-center p-0 text-sm leading-none opacity-70 hover:opacity-100" onclick="this.closest('.m-tag').remove()" title="remover">×</button>`;
  document.getElementById('active-tags').insertBefore(tag, document.getElementById('tag-input'));
}

function collectPostTags() {
  return [...document.querySelectorAll('#active-tags .m-tag')]
    .map(el => el.childNodes[0].textContent.trim())
    .filter(Boolean);
}

/* ── Publicar ou atualizar post via API ── */
async function publishPost() {
  const title    = document.getElementById('modal-title').value.trim() || null;
  const content  = document.getElementById('modal-desc').value.trim();
  const tags     = collectPostTags();
  const jobSel   = document.getElementById('modal-linked-job');
  const linkedJobId = jobSel && jobSel.value ? Number(jobSel.value) : null;

  // Valida conteúdo
  if (!content) {
    const ta = document.getElementById('modal-desc');
    ta.focus();
    ta.style.borderColor = 'var(--red)';
    setTimeout(() => { ta.style.borderColor = ''; }, 2000);
    return;
  }

  const token = localStorage.getItem('firstech_access_token');
  if (!token) {
    alert('Sessão expirada. Faça login novamente.');
    window.location.href = '/login';
    return;
  }

  const isEdit = editingPostId !== null;
  const method = isEdit ? 'PUT'  : 'POST';
  const url    = isEdit ? `/api/posts/${editingPostId}` : '/api/posts';

  const btn = document.getElementById('post-publish-btn');
  btn.disabled = true;
  const savedHTML = btn.innerHTML;
  btn.innerHTML = '<i class="ti ti-loader-2 text-sm animate-spin"></i><span>Aguarde...</span>';

  try {
    const resp = await fetch(url, {
      method,
      headers: {
        'Content-Type':  'application/json',
        'Authorization': 'Bearer ' + token,
      },
      body: JSON.stringify({ title, content, tags, linkedJobId }),
    });

    if (!resp.ok) {
      const err = await resp.json().catch(() => ({}));
      throw new Error(err.message || 'Erro ao salvar post.');
    }

    const post = await resp.json();
    closeModal();

    if (isEdit) {
      showJobToast('Post atualizado com sucesso!');
      window.location.reload();
    } else {
      showJobToast('Post publicado!');
      injectPostCard(post);
    }

  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  } finally {
    btn.disabled  = false;
    btn.innerHTML = savedHTML;
  }
}

/* ── Abrir edição a partir do botão do card (server-side rendered) ── */
function editPostFromBtn(btn) {
  const post = {
    id:         btn.dataset.postId,
    title:      btn.dataset.postTitle   || '',
    content:    btn.dataset.postContent || '',
    tags:       btn.dataset.postTags ? btn.dataset.postTags.split(',').filter(Boolean) : [],
    linkedJobId: btn.dataset.linkedJobId || '',
  };
  openModal(post);
}

/* ── Excluir post ── */
async function deletePost(id) {
  if (!confirm('Excluir este post? Esta ação não pode ser desfeita.')) return;

  const token = localStorage.getItem('firstech_access_token');
  try {
    const resp = await fetch(`/api/posts/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + token },
    });
    if (!resp.ok) throw new Error('Erro ao excluir post.');

    // Remove card do DOM sem recarregar
    document.getElementById('post-' + id)?.remove();
    showJobToast('Post excluído.');
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Inserir card de novo post no DOM (sem reload) ── */
function injectPostCard(post) {
  const feed = document.querySelector('#s-home .col:nth-child(2)');
  if (!feed) return;

  const tagsHtml = (post.tags || [])
    .map(t => `<div class="text-[10px] py-[2px] px-2 rounded-[20px] bg-purplebg text-purple2 border border-border">${t}</div>`)
    .join('');

  const autorInicial = (post.autor?.nome || 'U').charAt(0).toUpperCase();
  const autorCor     = post.autor?.corAvatar || 'linear-gradient(135deg,#6d28d9,#8b5cf6)';

  const card = document.createElement('div');
  card.id = 'post-' + post.id;
  card.className = 'bg-bg2 border border-border2 rounded-[14px] p-4 mb-[13px] transition-[border-color] duration-200 hover:border-border';
  card.style.animation = 'modalIn .3s ease';
  card.innerHTML = `
    <div class="flex items-center gap-[10px] mb-3">
      <div class="w-10 h-10 rounded-full shrink-0 flex items-center justify-center text-sm font-bold text-white"
           style="background:${autorCor}">${autorInicial}</div>
      <div class="flex-1">
        <div class="text-sm font-semibold text-text1">${post.autor?.nome || 'Você'}</div>
        <div class="text-[11px] text-text3 mt-px">${post.autor?.cargo || ''} · agora</div>
      </div>
      <div class="flex gap-1">
        <button class="w-[30px] h-[30px] rounded-lg border-none bg-transparent text-text3 flex items-center justify-center cursor-pointer text-[16px] transition-all duration-[180ms] hover:bg-bg3 hover:text-purple2"
                title="Editar post"
                data-post-id="${post.id}"
                data-post-title="${post.title || ''}"
                data-post-content="${post.content || ''}"
                data-post-tags="${(post.tags || []).join(',')}"
                data-linked-job-id="${post.linkedJobId || ''}"
                onclick="editPostFromBtn(this)">
          <i class="ti ti-pencil"></i>
        </button>
        <button class="w-[30px] h-[30px] rounded-lg border-none bg-transparent text-text3 flex items-center justify-center cursor-pointer text-[16px] transition-all duration-[180ms] hover:bg-bg3 hover:text-red"
                title="Excluir post"
                onclick="deletePost(${post.id})">
          <i class="ti ti-trash"></i>
        </button>
      </div>
    </div>
    ${post.title ? `<div class="font-display text-[15px] font-semibold text-text1 mb-1">${post.title}</div>` : ''}
    <div class="text-sm text-text2 leading-[1.7] mb-3">${post.content}</div>
    ${tagsHtml ? `<div class="flex gap-1 flex-wrap mb-3">${tagsHtml}</div>` : ''}
    <div class="flex gap-1">
      <button class="flex items-center gap-[5px] py-[6px] px-[10px] rounded-[7px] border-none bg-transparent text-text3 font-[inherit] text-xs cursor-pointer transition-all duration-200 hover:bg-bg3 hover:text-purple2">
        <i class="ti ti-heart text-base"></i><span>0</span>
      </button>
      <button class="flex items-center gap-[5px] py-[6px] px-[10px] rounded-[7px] border-none bg-transparent text-text3 font-[inherit] text-xs cursor-pointer transition-all duration-200 hover:bg-bg3 hover:text-purple2">
        <i class="ti ti-message-circle text-base"></i><span>0</span>
      </button>
      <button class="flex items-center gap-[5px] py-[6px] px-[10px] rounded-[7px] border-none bg-transparent text-text3 font-[inherit] text-xs cursor-pointer transition-all duration-200 hover:bg-bg3 hover:text-purple2">
        <i class="ti ti-share text-base"></i>Compartilhar
      </button>
    </div>`;

  // Insere imediatamente abaixo do compositor
  const compositor = feed.firstElementChild;
  if (compositor) {
    compositor.insertAdjacentElement('afterend', card);
  } else {
    feed.prepend(card);
  }
}

/* ═══════════════════════════════════════════════════════════
   MODAL DE NOVA / EDITAR VAGA
   ═══════════════════════════════════════════════════════════ */

/** ID da vaga sendo editada. null = modo criação. */
let editingJobId = null;

/**
 * Abre o modal de vaga.
 * @param {object|null} vaga - Se fornecido, entra em modo de edição.
 */
function openJobModal(vaga) {
  editingJobId = vaga ? Number(vaga.id) : null;

  const heading      = document.getElementById('job-modal-heading');
  const publishLabel = document.getElementById('job-publish-label');
  const publishIcon  = document.getElementById('job-publish-icon');

  if (vaga) {
    // ── Modo edição ──
    heading.textContent      = 'Editar vaga';
    publishLabel.textContent = 'Salvar alterações';
    publishIcon.className    = 'ti ti-device-floppy text-sm';

    document.getElementById('job-titulo').value            = vaga.title          || '';
    document.getElementById('job-localidade').value        = vaga.location       || '';
    document.getElementById('job-salario').value           = vaga.salary         || '';
    document.getElementById('job-descricao').value         = vaga.description    || '';
    document.getElementById('job-company-name').value      = vaga.jobCompany     || '';
    document.getElementById('job-company-logo-url').value  = vaga.companyLogoUrl || '';

    // Selects: define o valor e garante que a opção exista
    _setSelectValue('job-modalidade', vaga.modality);
    _setSelectValue('job-nivel',      vaga.level);

    // Repopula tags
    document.querySelectorAll('#job-active-tags .job-tag').forEach(t => t.remove());
    (vaga.tags || []).forEach(tag => addJobTag(tag));
  } else {
    // ── Modo criação ──
    heading.textContent      = 'Nova vaga';
    publishLabel.textContent = 'Publicar vaga';
    publishIcon.className    = 'ti ti-send text-sm';
    resetJobModal();
  }

  document.getElementById('job-overlay').classList.add('open');
  setTimeout(() => document.getElementById('job-titulo').focus(), 80);
}

function _setSelectValue(selectId, value) {
  const sel = document.getElementById(selectId);
  if (!sel) return;
  sel.value = value || '';
}

function closeJobModal() {
  document.getElementById('job-overlay').classList.remove('open');
  editingJobId = null;
}

function handleJobOverlayClick(e) {
  if (e.target === document.getElementById('job-overlay')) closeJobModal();
}

/* ── Tags do modal de vaga ── */
function handleJobTagKey(e) {
  if ((e.key === 'Enter' || e.key === ',') && e.target.value.trim()) {
    e.preventDefault();
    const val = e.target.value.trim().replace(/,$/, '');
    if (!val) return;
    addJobTag(val);
    e.target.value = '';
  }
}

function addJobTag(val) {
  const tag = document.createElement('div');
  tag.className = 'job-tag flex items-center gap-[5px] bg-purplebg text-purple2 border border-border rounded-[20px] py-[3px] px-[10px] text-xs font-medium';
  tag.innerHTML = `${val}<button type="button" class="bg-transparent border-none text-purple2 cursor-pointer flex items-center p-0 text-sm leading-none opacity-70 hover:opacity-100" onclick="this.closest('.job-tag').remove()" title="remover">×</button>`;
  const input = document.getElementById('job-tag-input');
  document.getElementById('job-active-tags').insertBefore(tag, input);
}

function collectJobTags() {
  return [...document.querySelectorAll('#job-active-tags .job-tag')]
    .map(el => el.childNodes[0].textContent.trim())
    .filter(Boolean);
}

/* ── Publicar ou atualizar vaga via API ── */
async function publishJob() {
  const title            = document.getElementById('job-titulo').value.trim();
  const jobLocation      = document.getElementById('job-localidade').value.trim();
  const modality         = document.getElementById('job-modalidade').value || null;
  const level            = document.getElementById('job-nivel').value      || null;
  const salary           = document.getElementById('job-salario').value.trim() || null;
  const description      = document.getElementById('job-descricao').value.trim();
  const tags             = collectJobTags();
  const jobCompany       = document.getElementById('job-company-name').value.trim() || null;
  const jobCompanyLogoUrl = document.getElementById('job-company-logo-url').value.trim() || null;

  // Valida título
  if (!title) {
    const input = document.getElementById('job-titulo');
    input.focus();
    input.style.borderColor = 'var(--red)';
    setTimeout(() => { input.style.borderColor = ''; }, 2000);
    return;
  }

  const token = localStorage.getItem('firstech_access_token');
  if (!token) {
    alert('Sessão expirada. Faça login novamente.');
    window.location.href = '/login';
    return;
  }

  const isEdit  = editingJobId !== null;
  const method  = isEdit ? 'PUT'            : 'POST';
  const url     = isEdit ? `/api/jobs/${editingJobId}` : '/api/jobs';

  const btn = document.getElementById('job-publish-btn');
  btn.disabled = true;
  const savedHTML = btn.innerHTML;
  btn.innerHTML = '<i class="ti ti-loader-2 text-sm animate-spin"></i><span>Aguarde...</span>';

  try {
    const resp = await fetch(url, {
      method,
      headers: {
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + token,
      },
      body: JSON.stringify({ title, location: jobLocation, modality, level, salary, description, tags, jobCompany, jobCompanyLogoUrl }),
    });

    if (!resp.ok) {
      const err = await resp.json().catch(() => ({}));
      throw new Error(err.message || 'Erro ao salvar vaga.');
    }

    const vaga = await resp.json();
    closeJobModal();

    if (isEdit) {
      showJobToast('Vaga atualizada com sucesso!');
      window.location.reload();
    } else {
      showJobToast('Vaga "' + vaga.title + '" publicada com sucesso!');
      injectJobCard(vaga);
    }

  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  } finally {
    btn.disabled  = false;
    btn.innerHTML = savedHTML;
  }
}

function resetJobModal() {
  document.getElementById('job-titulo').value           = '';
  document.getElementById('job-localidade').value       = '';
  document.getElementById('job-modalidade').value       = '';
  document.getElementById('job-nivel').value            = '';
  document.getElementById('job-salario').value          = '';
  document.getElementById('job-descricao').value        = '';
  document.getElementById('job-company-name').value     = '';
  document.getElementById('job-company-logo-url').value = '';
  document.querySelectorAll('#job-active-tags .job-tag').forEach(t => t.remove());
}

/* ── Abrir edição a partir do botão do card (server-side rendered) ── */
function editJobFromBtn(btn) {
  const vaga = {
    id:               btn.dataset.id,
    title:            btn.dataset.title           || '',
    location:         btn.dataset.location        || '',
    modality:         btn.dataset.modality        || '',
    level:            btn.dataset.level           || '',
    salary:           btn.dataset.salary          || '',
    description:      btn.dataset.description     || '',
    tags:             btn.dataset.tags ? btn.dataset.tags.split(',').filter(Boolean) : [],
    jobCompany:       btn.dataset.jobCompany      || '',
    companyLogoUrl:   btn.dataset.jobCompanyLogoUrl || '',
  };
  openJobModal(vaga);
}

/* ── Inserir card de nova vaga no DOM sem reload ── */
function injectJobCard(vaga) {
  const jobsFeed = document.querySelector('#s-jobs .col:nth-child(2)');
  if (!jobsFeed) return;

  // Remove empty state (ícone briefcase-off)
  jobsFeed.querySelector('.ti-briefcase-off')?.closest('[class*="border-dashed"]')?.remove();

  const tagsHtml = (vaga.tags || [])
    .map(t => `<div class="text-[10px] py-[2px] px-2 rounded-[20px] bg-purplebg text-purple2 border border-border">${t}</div>`)
    .join('');

  const logoStyle = vaga.companyLogo || 'linear-gradient(135deg,#6d28d9,#8b5cf6)';
  const initial   = vaga.companyLogoUrl ? '' : (vaga.company || 'E').charAt(0).toUpperCase();
  const meta      = [vaga.location, vaga.modality, vaga.level].filter(Boolean).join(' · ');

  const card = document.createElement('div');
  card.className = 'bg-bg2 border border-border2 rounded-[14px] p-[18px] mb-[13px] transition-all duration-200 hover:border-border';
  card.style.animation = 'modalIn .3s ease';
  card.innerHTML = `
    <div class="flex items-center gap-[13px] mb-[10px]">
      <div class="w-[46px] h-[46px] rounded-[11px] shrink-0 flex items-center justify-center text-[18px] font-[800] text-white overflow-hidden"
           style="background:${logoStyle}">${initial}</div>
      <div class="flex-1 min-w-0">
        <div class="font-display text-[15px] font-semibold text-text1 flex items-center gap-2">
          <span>${vaga.title}</span>
          <span class="text-[9px] font-bold py-px px-2 rounded-full bg-[rgba(52,211,153,.12)] text-green border border-[rgba(52,211,153,.25)] uppercase tracking-wider">Ativa</span>
        </div>
        <div class="text-xs text-text2">${meta}</div>
      </div>
    </div>
    ${vaga.description ? `<div class="text-[13px] text-text3 leading-[1.6] mb-[10px] line-clamp-2">${vaga.description}</div>` : ''}
    <div class="flex gap-1 flex-wrap mb-[12px]">${tagsHtml}</div>
    <div class="flex items-center justify-between border-t border-border2 pt-[12px]">
      <div class="text-[11px] text-text3">Publicada agora</div>
      <div class="flex gap-2">
        <button class="flex items-center gap-[5px] py-[6px] px-[11px] rounded-[8px] border border-border2 bg-transparent text-text3 font-[inherit] text-[12px] cursor-pointer transition-all duration-200 hover:border-border hover:text-text1"
                data-id="${vaga.id}" data-title="${vaga.title}" data-location="${vaga.location || ''}"
                data-modality="${vaga.modality || ''}" data-level="${vaga.level || ''}"
                data-salary="${vaga.salary || ''}" data-description="${vaga.description || ''}"
                data-tags="${(vaga.tags || []).join(',')}"
                data-job-company="${vaga.jobCompany || ''}" data-job-company-logo-url="${vaga.companyLogoUrl || ''}"
                onclick="editJobFromBtn(this)">
          <i class="ti ti-pencil text-sm"></i>Editar
        </button>
        <button class="flex items-center gap-[5px] py-[6px] px-[11px] rounded-[8px] border border-border2 bg-transparent text-text3 font-[inherit] text-[12px] cursor-pointer transition-all duration-200 hover:border-amber hover:text-amber"
                onclick="closeJob(${vaga.id})">
          <i class="ti ti-circle-off text-sm"></i>Encerrar
        </button>
        <button class="flex items-center gap-[5px] py-[6px] px-[11px] rounded-[8px] border border-border2 bg-transparent text-text3 font-[inherit] text-[12px] cursor-pointer transition-all duration-200 hover:border-red hover:text-red"
                onclick="deleteJob(${vaga.id})">
          <i class="ti ti-trash text-sm"></i>Excluir
        </button>
      </div>
    </div>`;

  // Insere imediatamente após a linha de cabeçalho ("Minhas vagas" + botão)
  // O primeiro filho da coluna é sempre o header row depois do Thymeleaf renderizar
  const headerRow = jobsFeed.firstElementChild;
  if (headerRow) {
    headerRow.insertAdjacentElement('afterend', card);
  } else {
    jobsFeed.appendChild(card);
  }
}

/* ── Encerrar vaga ── */
async function closeJob(id) {
  if (!confirm('Encerrar esta vaga? Ela deixará de aparecer para candidatos.')) return;

  const token = localStorage.getItem('firstech_access_token');
  try {
    const resp = await fetch(`/api/jobs/${id}/fechar`, {
      method: 'PATCH',
      headers: { 'Authorization': 'Bearer ' + token },
    });
    if (!resp.ok) throw new Error('Erro ao encerrar vaga.');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Excluir vaga ── */
async function deleteJob(id) {
  if (!confirm('Excluir esta vaga permanentemente? Esta ação não pode ser desfeita.')) return;

  const token = localStorage.getItem('firstech_access_token');
  try {
    const resp = await fetch(`/api/jobs/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + token },
    });
    if (!resp.ok) throw new Error('Erro ao excluir vaga.');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Toast de feedback ── */
function showJobToast(msg, isError = false) {
  let container = document.getElementById('job-toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'job-toast-container';
    container.style.cssText = 'position:fixed;bottom:24px;right:24px;z-index:9999;display:flex;flex-direction:column;gap:8px;pointer-events:none;';
    document.body.appendChild(container);
  }

  const toast = document.createElement('div');
  toast.style.cssText = `
    padding:10px 16px;border-radius:10px;font-size:13px;font-weight:500;pointer-events:auto;
    background:${isError ? '#f87171' : '#34d399'};color:#0d1117;
    box-shadow:0 4px 20px rgba(0,0,0,.4);animation:modalIn .25s ease;max-width:340px;
  `;
  toast.textContent = msg;
  container.appendChild(toast);
  setTimeout(() => toast.remove(), 4000);
}

/* ── Fechar modais com ESC ── */
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') {
    closeModal();
    closeJobModal();
    ['port-profile-overlay','port-exp-overlay','port-skill-overlay','port-cert-overlay','port-proj-overlay']
      .forEach(id => closePortModal(id));
  }
});

/* ═══════════════════════════════════════════════════════════
   PORTFÓLIO — modais e CRUD
   ═══════════════════════════════════════════════════════════ */

/** Helper: abre/fecha qualquer overlay de portfólio. */
function closePortModal(overlayId) {
  document.getElementById(overlayId)?.classList.remove('open');
}

function _portToken() {
  const t = localStorage.getItem('firstech_access_token');
  if (!t) { alert('Sessão expirada. Faça login novamente.'); window.location.href = '/login'; }
  return t;
}

function _highlightInvalid(el) {
  el.focus();
  el.style.borderColor = 'var(--red)';
  setTimeout(() => { el.style.borderColor = ''; }, 2000);
}

/* ── Perfil / Sobre ──────────────────────────────────────── */

function openPortfolioProfileModal() {
  document.getElementById('port-profile-overlay').classList.add('open');
  setTimeout(() => document.getElementById('port-name')?.focus(), 80);
}

async function savePortProfile() {
  const name     = document.getElementById('port-name').value.trim();
  const headline = document.getElementById('port-headline').value.trim();
  const city     = document.getElementById('port-city').value.trim();
  const about    = document.getElementById('port-sobre').value.trim();

  if (!name) { _highlightInvalid(document.getElementById('port-name')); return; }

  const token = _portToken(); if (!token) return;

  try {
    const resp = await fetch('/api/portfolio/profile', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ name, headline, city, about }),
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao salvar perfil.');
    closePortModal('port-profile-overlay');
    showJobToast('Perfil atualizado!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Experiências ─────────────────────────────────────────── */

function openExpModal(ds) {
  const heading = document.getElementById('exp-modal-heading');
  document.getElementById('exp-editing-id').value = ds?.id || '';
  document.getElementById('exp-title').value       = ds?.title   || '';
  document.getElementById('exp-company').value     = ds?.company || '';
  document.getElementById('exp-period').value      = ds?.period  || '';
  document.getElementById('exp-desc').value        = ds?.desc    || '';
  heading.textContent = ds?.id ? 'Editar experiência' : 'Adicionar experiência';
  document.getElementById('port-exp-overlay').classList.add('open');
  setTimeout(() => document.getElementById('exp-title').focus(), 80);
}

async function saveExperience() {
  const editingId   = document.getElementById('exp-editing-id').value;
  const title       = document.getElementById('exp-title').value.trim();
  const companyName = document.getElementById('exp-company').value.trim();
  const period      = document.getElementById('exp-period').value.trim();
  const description = document.getElementById('exp-desc').value.trim();

  if (!title)       { _highlightInvalid(document.getElementById('exp-title'));   return; }
  if (!companyName) { _highlightInvalid(document.getElementById('exp-company')); return; }

  const token = _portToken(); if (!token) return;
  const isEdit = !!editingId;
  const url    = isEdit ? `/api/portfolio/experiences/${editingId}` : '/api/portfolio/experiences';

  try {
    const resp = await fetch(url, {
      method: isEdit ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ title, companyName, period, description, orderIndex: 0 }),
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao salvar experiência.');
    closePortModal('port-exp-overlay');
    showJobToast(isEdit ? 'Experiência atualizada!' : 'Experiência adicionada!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Habilidades ──────────────────────────────────────────── */

function openSkillModal(ds) {
  document.getElementById('skill-editing-id').value  = ds?.id   || '';
  document.getElementById('skill-name').value         = ds?.name || '';
  document.getElementById('skill-highlight').checked  = false;
  document.getElementById('port-skill-overlay').classList.add('open');
  setTimeout(() => document.getElementById('skill-name').focus(), 80);
}

async function saveSkill() {
  const editingId = document.getElementById('skill-editing-id').value;
  const name      = document.getElementById('skill-name').value.trim();
  const highlight = document.getElementById('skill-highlight').checked;

  if (!name) { _highlightInvalid(document.getElementById('skill-name')); return; }

  const token = _portToken(); if (!token) return;
  const isEdit = !!editingId;
  const url    = isEdit ? `/api/portfolio/skills/${editingId}` : '/api/portfolio/skills';

  try {
    const resp = await fetch(url, {
      method: isEdit ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ name, highlight, orderIndex: 0 }),
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao salvar habilidade.');
    closePortModal('port-skill-overlay');
    showJobToast(isEdit ? 'Habilidade atualizada!' : 'Habilidade adicionada!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Certificações ────────────────────────────────────────── */

function openCertModal(ds) {
  const heading = document.getElementById('cert-modal-heading');
  document.getElementById('cert-editing-id').value = ds?.id     || '';
  document.getElementById('cert-emoji').value       = ds?.emoji  || '📜';
  document.getElementById('cert-name').value        = ds?.name   || '';
  document.getElementById('cert-issuer').value      = ds?.issuer || '';
  document.getElementById('cert-date').value        = ds?.date   || '';
  heading.textContent = ds?.id ? 'Editar certificação' : 'Adicionar certificação';
  document.getElementById('port-cert-overlay').classList.add('open');
  setTimeout(() => document.getElementById('cert-name').focus(), 80);
}

async function saveCertification() {
  const editingId = document.getElementById('cert-editing-id').value;
  const emoji     = document.getElementById('cert-emoji').value.trim()  || '📜';
  const name      = document.getElementById('cert-name').value.trim();
  const issuer    = document.getElementById('cert-issuer').value.trim();
  const issueDate = document.getElementById('cert-date').value.trim();

  if (!name) { _highlightInvalid(document.getElementById('cert-name')); return; }

  const token = _portToken(); if (!token) return;
  const isEdit = !!editingId;
  const url    = isEdit ? `/api/portfolio/certifications/${editingId}` : '/api/portfolio/certifications';

  try {
    const resp = await fetch(url, {
      method: isEdit ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ emoji, name, issuer, issueDate, orderIndex: 0 }),
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao salvar certificação.');
    closePortModal('port-cert-overlay');
    showJobToast(isEdit ? 'Certificação atualizada!' : 'Certificação adicionada!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Projetos ─────────────────────────────────────────────── */

const _DEFAULT_PROJ_GRADIENT = 'linear-gradient(135deg,#0f0521,#2d1b6e)';

function openProjModal(ds) {
  const heading = document.getElementById('proj-modal-heading');
  document.getElementById('proj-editing-id').value = ds?.id       || '';
  document.getElementById('proj-name').value        = ds?.name     || '';
  document.getElementById('proj-desc').value        = ds?.desc     || '';
  document.getElementById('proj-icon').value        = ds?.icon     || '';
  document.getElementById('proj-gradient').value    = ds?.gradient || _DEFAULT_PROJ_GRADIENT;
  heading.textContent = ds?.id ? 'Editar projeto' : 'Adicionar projeto';

  // Marca o botão de tema correspondente (ou o primeiro se não houver match)
  const activGrad = ds?.gradient || _DEFAULT_PROJ_GRADIENT;
  document.querySelectorAll('#proj-theme-picker .proj-theme-btn').forEach(btn => {
    const active = btn.dataset.gradient === activGrad;
    btn.style.borderColor = active ? 'white' : 'transparent';
  });

  document.getElementById('port-proj-overlay').classList.add('open');
  setTimeout(() => document.getElementById('proj-name').focus(), 80);
}

function selectProjTheme(btn) {
  document.querySelectorAll('#proj-theme-picker .proj-theme-btn').forEach(b => {
    b.style.borderColor = 'transparent';
  });
  btn.style.borderColor = 'white';
  document.getElementById('proj-gradient').value = btn.dataset.gradient;
}

async function saveProject() {
  const editingId    = document.getElementById('proj-editing-id').value;
  const name         = document.getElementById('proj-name').value.trim();
  const description  = document.getElementById('proj-desc').value.trim();
  const icon         = document.getElementById('proj-icon').value.trim() || 'ti-code';
  const thumbGradient = document.getElementById('proj-gradient').value || _DEFAULT_PROJ_GRADIENT;

  if (!name) { _highlightInvalid(document.getElementById('proj-name')); return; }

  const token = _portToken(); if (!token) return;
  const isEdit = !!editingId;
  const url    = isEdit ? `/api/portfolio/projects/${editingId}` : '/api/portfolio/projects';

  try {
    const resp = await fetch(url, {
      method: isEdit ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ name, description, thumbGradient, icon, orderIndex: 0 }),
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao salvar projeto.');
    closePortModal('port-proj-overlay');
    showJobToast(isEdit ? 'Projeto atualizado!' : 'Projeto adicionado!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Deletar qualquer item do portfólio ───────────────────── */

const _PORT_DELETE_LABELS = {
  experiences:    'experiência',
  skills:         'habilidade',
  certifications: 'certificação',
  projects:       'projeto',
};

async function deletePortfolioItem(type, id) {
  const label = _PORT_DELETE_LABELS[type] || 'item';
  if (!confirm(`Excluir esta ${label}? Esta ação não pode ser desfeita.`)) return;

  const token = _portToken(); if (!token) return;

  try {
    const resp = await fetch(`/api/portfolio/${type}/${id}`, {
      method: 'DELETE',
      headers: { 'Authorization': 'Bearer ' + token },
    });
    if (!resp.ok) throw new Error(`Erro ao excluir ${label}.`);
    showJobToast(`${label.charAt(0).toUpperCase() + label.slice(1)} excluída.`);
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}
