// ============================================================
//  Firstech — Scripts principais
//  Requer: Tabler Icons carregados no <head>
// ============================================================

/* ── NAVEGAÇÃO ENTRE TELAS ── */
function go(name) {
  document.querySelectorAll('.screen, .port-screen')
    .forEach(s => s.classList.remove('active'));
  document.querySelectorAll('.nav-btn, .mobile-nav-btn')
    .forEach(b => b.classList.remove('active'));

  document.getElementById('s-' + name)?.classList.add('active');
  document.getElementById('nb-' + name)?.classList.add('active');
  document.getElementById('mbn-' + name)?.classList.add('active');
}

/** Mobile: volta ao painel de conversas a partir do chat ativo. */
function mobileBackToConvs() {
  document.getElementById('s-messages')?.classList.remove('msg-chat-active');
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
let _isProject    = false;

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

    // Restaura flag de projeto
    _isProject = post.isProject || false;
    _applyProjectToggle();

    // Restaura GitHub URL
    const ghInp = document.getElementById('modal-github-url');
    const ghRow = document.getElementById('github-url-row');
    const ghBtn = document.getElementById('github-toggle-btn');
    if (ghInp) ghInp.value = post.githubUrl || '';
    if (ghRow) ghRow.style.display = post.githubUrl ? 'flex' : 'none';
    if (ghBtn) ghBtn.style.color = post.githubUrl ? 'var(--purple2)' : '';
  } else {
    editingPostId = null;
    resetPostModal();
  }

  syncCharCounters();
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
  _isProject = false;
  _applyProjectToggle();
  // Reset GitHub
  const ghRow = document.getElementById('github-url-row');
  const ghBtn = document.getElementById('github-toggle-btn');
  const ghInp = document.getElementById('modal-github-url');
  if (ghRow) ghRow.style.display = 'none';
  if (ghBtn) ghBtn.style.color = '';
  if (ghInp) ghInp.value = '';
}

function toggleGithubInput() {
  const row = document.getElementById('github-url-row');
  const btn = document.getElementById('github-toggle-btn');
  if (!row) return;
  const visible = row.style.display !== 'none';
  row.style.display = visible ? 'none' : 'flex';
  if (btn) btn.style.color = visible ? '' : 'var(--purple2)';
  if (!visible) document.getElementById('modal-github-url')?.focus();
  if (visible) { const inp = document.getElementById('modal-github-url'); if (inp) inp.value = ''; }
}

function toggleProjectFlag() {
  _isProject = !_isProject;
  _applyProjectToggle();
}

function _applyProjectToggle() {
  const sw   = document.getElementById('project-toggle-switch');
  const knob = document.getElementById('project-toggle-knob');
  if (!sw || !knob) return;
  if (_isProject) {
    sw.style.background  = 'rgba(139,92,246,0.25)';
    sw.style.borderColor = 'rgba(139,92,246,0.5)';
    knob.style.background = 'var(--purple2)';
    knob.style.left       = '21px';
  } else {
    sw.style.background  = '';
    sw.style.borderColor = '';
    knob.style.background = '';
    knob.style.left       = '3px';
  }
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
  const title     = document.getElementById('modal-title').value.trim() || null;
  const content   = document.getElementById('modal-desc').value.trim();
  const tags      = collectPostTags();
  const jobSel    = document.getElementById('modal-linked-job');
  const linkedJobId = jobSel && jobSel.value ? Number(jobSel.value) : null;
  const githubUrl = (document.getElementById('modal-github-url')?.value || '').trim() || null;

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
      body: JSON.stringify({ title, content, tags, linkedJobId, isProject: _isProject, githubUrl }),
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
    } else if (_isProject) {
      // Projeto criado → vai direto para o perfil
      window.location.href = '/dashboard?tab=portfolio';
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
    id:          btn.dataset.postId,
    title:       btn.dataset.postTitle   || '',
    content:     btn.dataset.postContent || '',
    tags:        btn.dataset.postTags ? btn.dataset.postTags.split(',').filter(Boolean) : [],
    linkedJobId: btn.dataset.linkedJobId || '',
    isProject:   btn.dataset.isProject === 'true',
    githubUrl:   btn.dataset.githubUrl || '',
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
        <div class="flex items-center gap-[6px] flex-wrap">
          <span class="text-sm font-semibold text-text1">${post.autor?.nome || 'Você'}</span>
          ${post.isProject ? '<span class="inline-flex items-center gap-[3px] text-[9px] font-semibold px-[6px] py-[1px] rounded-full" style="background:rgba(139,92,246,.12);color:var(--purple2);border:1px solid rgba(139,92,246,.3)"><i class=\'ti ti-folder-code text-[9px]\'></i>Projeto</span>' : ''}
        </div>
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
                data-is-project="${post.isProject || false}"
                data-github-url="${post.githubUrl || ''}"
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

    document.getElementById('job-titulo').value     = vaga.title       || '';
    document.getElementById('job-localidade').value = vaga.location    || '';
    document.getElementById('job-salario').value    = vaga.salary      || '';
    document.getElementById('job-descricao').value  = vaga.description || '';
    document.getElementById('job-apply-url').value  = vaga.applyUrl    || '';
    // Preenche campos de empresa (autocomplete + ocultos)
    _setCompanyFields(vaga.jobCompany || '', vaga.companyLogoUrl || '');

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

  syncCharCounters();
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
  const applyUrl         = document.getElementById('job-apply-url').value.trim() || null;
  const tags             = collectJobTags();
  const jobCompany        = document.getElementById('job-company-name').value.trim()     || null;
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
      body: JSON.stringify({ title, location: jobLocation, modality, level, salary, description, applyUrl, tags, jobCompany, jobCompanyLogoUrl }),
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
  document.getElementById('job-titulo').value     = '';
  document.getElementById('job-localidade').value = '';
  document.getElementById('job-modalidade').value = '';
  document.getElementById('job-nivel').value      = '';
  document.getElementById('job-salario').value    = '';
  document.getElementById('job-descricao').value  = '';
  document.getElementById('job-apply-url').value  = '';
  clearCompanySelection();
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
    applyUrl:         btn.dataset.applyUrl        || '',
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
                data-apply-url="${vaga.applyUrl || ''}"
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

/* ── Candidatar-se a uma vaga (abre link externo em nova aba) ── */
function applyToJob(url) {
  if (!url || url === 'null' || !url.trim()) return;
  window.open(url.trim(), '_blank', 'noopener,noreferrer');
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

/* ═══════════════════════════════════════════════════════════
   MENU DO USUÁRIO (topbar)
   ═══════════════════════════════════════════════════════════ */

function toggleUserMenu(e) {
  if (e) e.stopPropagation();
  document.getElementById('user-menu').classList.toggle('hidden');
}

function closeUserMenu() {
  document.getElementById('user-menu')?.classList.add('hidden');
}

document.addEventListener('click', e => {
  const menu = document.getElementById('user-menu');
  const btn  = document.getElementById('user-avatar-btn');
  if (menu && !menu.classList.contains('hidden') &&
      !menu.contains(e.target) && e.target !== btn && !btn?.contains(e.target)) {
    menu.classList.add('hidden');
  }
});

async function doLogout() {
  const token = localStorage.getItem('firstech_access_token');
  try {
    if (token) {
      await fetch('/api/auth/logout', {
        method: 'POST',
        headers: { 'Authorization': 'Bearer ' + token },
      });
    }
  } catch (_) { /* ignora erros de rede */ }
  localStorage.removeItem('firstech_access_token');
  localStorage.removeItem('firstech_refresh_token');
  window.location.href = '/login';
}

/* ── Fechar modais com ESC ── */
document.addEventListener('keydown', e => {
  if (e.key === 'Escape') {
    closeModal();
    closeJobModal();
    closeUserProfile();
    ['port-profile-overlay','port-sobre-overlay','port-exp-overlay','port-skill-overlay','port-cert-overlay','port-proj-overlay']
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

/* ── Avatar Upload ────────────────────────────────────────── */

async function uploadAvatar(input) {
  const file = input.files[0];
  if (!file) return;
  if (!file.type.startsWith('image/')) {
    showJobToast('Selecione uma imagem (JPEG, PNG, WebP...)', true); return;
  }
  if (file.size > 2 * 1024 * 1024) {
    showJobToast('A imagem deve ter no máximo 2 MB.', true); return;
  }
  const token = _portToken(); if (!token) return;

  const formData = new FormData();
  formData.append('file', file);

  showJobToast('Enviando foto...');
  try {
    const resp = await fetch('/api/portfolio/avatar', {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + token },
      body: formData,
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao enviar foto.');
    showJobToast('Foto atualizada!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Perfil (nome, headline, cidade) ─────────────────────── */

function openPortfolioProfileModal() {
  document.getElementById('port-profile-overlay').classList.add('open');
  setTimeout(() => document.getElementById('port-name')?.focus(), 80);
}

async function savePortProfile() {
  const name     = document.getElementById('port-name').value.trim();
  const headline = document.getElementById('port-headline').value.trim();
  const city     = document.getElementById('port-city').value.trim();

  if (!name) { _highlightInvalid(document.getElementById('port-name')); return; }

  const token = _portToken(); if (!token) return;

  try {
    const resp = await fetch('/api/portfolio/profile', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      // about: null → service ignores it (null-safe) — Sobre tem modal próprio
      body: JSON.stringify({ name, headline, city, about: null }),
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao salvar perfil.');
    closePortModal('port-profile-overlay');
    showJobToast('Perfil atualizado!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Sobre (modal separado) ──────────────────────────────── */

function openSobreModal() {
  document.getElementById('port-sobre-overlay').classList.add('open');
  setTimeout(() => document.getElementById('port-sobre')?.focus(), 80);
}

async function saveSobre() {
  const about = document.getElementById('port-sobre').value.trim();
  const token = _portToken(); if (!token) return;

  try {
    const resp = await fetch('/api/portfolio/profile', {
      method: 'PUT',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      // name/headline/city: null → service ignores them
      body: JSON.stringify({ name: null, headline: null, city: null, about }),
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao salvar.');
    closePortModal('port-sobre-overlay');
    showJobToast('Sobre atualizado!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ── Autocomplete de localização (IBGE) — genérico ──────── */

let _ibgeCities  = null;   // cache: ["São Paulo, SP", ...]
let _cityTimer   = null;

async function _loadCities() {
  if (_ibgeCities) return _ibgeCities;
  try {
    const r = await fetch(
      'https://servicodados.ibge.gov.br/api/v1/localidades/municipios?orderBy=nome'
    );
    const data = await r.json();
    _ibgeCities = data.map(m => `${m.nome}, ${m.microrregiao.mesorregiao.UF.sigla}`);
  } catch (_) {
    _ibgeCities = [];
  }
  return _ibgeCities;
}

// [̀-ͯ] = bloco de diacríticos combinantes (NFD decomposition)
function _normalize(s) {
  return s.toLowerCase().normalize('NFD').replace(/[̀-ͯ]/g, '');
}

// Pré-carrega cidades ao iniciar a página para zero latência no primeiro uso
_loadCities();

/**
 * Autocomplete de localização genérico (estilo Google).
 * @param {HTMLInputElement} input   - campo de texto
 * @param {string}           dropdownId - id do div dropdown
 */
function locationAutocomplete(input, dropdownId = 'city-ac-dropdown') {
  const dd = document.getElementById(dropdownId);
  if (!dd) return;
  clearTimeout(_cityTimer);

  const query = input.value; // sem .trim() para não esconder sugestões com espaço final
  if (!query) { dd.classList.add('hidden'); return; }

  _cityTimer = setTimeout(async () => {
    const cities = await _loadCities();
    const q      = _normalize(query.trim());
    if (!q) { dd.classList.add('hidden'); return; }

    const matches = cities
      .filter(c => _normalize(c).startsWith(q) || _normalize(c).includes(q))
      .sort((a, b) => {
        // Prioriza matches que começam com a query
        const aN = _normalize(a), bN = _normalize(b);
        return (aN.startsWith(q) ? 0 : 1) - (bN.startsWith(q) ? 0 : 1)
            || a.localeCompare(b, 'pt-BR');
      })
      .slice(0, 8);

    if (!matches.length) { dd.classList.add('hidden'); return; }

    dd.innerHTML = matches.map(c =>
      `<button type="button"
         class="w-full text-left px-3 py-[9px] text-[13px] text-text2 transition-colors duration-100 hover:bg-bg3 hover:text-text1 border-b border-border2 last:border-b-0 flex items-center gap-2"
         data-loc-input="${_escapeHtml(input.id)}"
         data-loc-dropdown="${_escapeHtml(dropdownId)}"
         data-loc-city="${_escapeHtml(c)}"
         onclick="selectLocationFromBtn(this)">
        <i class="ti ti-map-pin text-[12px] text-text3 shrink-0"></i>
        <span>${_highlightMatch(_escapeHtml(c), _escapeHtml(query.trim()))}</span>
      </button>`
    ).join('');
    dd.classList.remove('hidden');
  }, 150); // 150ms — rápido como Google
}

/** Seleciona localização a partir do botão do dropdown (sem problemas de escaping) */
function selectLocationFromBtn(btn) {
  selectLocation(btn.dataset.locInput, btn.dataset.locDropdown, btn.dataset.locCity);
}

/** Destaca a parte digitada com negrito */
function _highlightMatch(text, query) {
  if (!query) return text;
  const idx = _normalize(text).indexOf(_normalize(query));
  if (idx < 0) return text;
  return text.slice(0, idx)
    + '<strong class="text-text1">' + text.slice(idx, idx + query.length) + '</strong>'
    + text.slice(idx + query.length);
}

/** Atalho para o campo do portfólio (retrocompatibilidade) */
function cityAutocomplete(input) { locationAutocomplete(input, 'city-ac-dropdown'); }

function selectLocation(inputId, dropdownId, city) {
  const inp = document.getElementById(inputId);
  if (inp) inp.value = city;
  document.getElementById(dropdownId)?.classList.add('hidden');
}

/** Compatibilidade com selectCity anterior */
function selectCity(city) { selectLocation('port-city', 'city-ac-dropdown', city); }

// Fecha dropdowns de localização ao clicar fora
document.addEventListener('click', e => {
  ['city-ac-dropdown', 'job-loc-dropdown'].forEach(id => {
    const dd = document.getElementById(id);
    if (dd && !e.target.closest('#' + id) && !e.target.closest('[data-loc-dropdown="' + id + '"]')
        && !['port-city','job-localidade'].includes(e.target.id)) {
      dd.classList.add('hidden');
    }
  });
});

/* ── Experiências ─────────────────────────────────────────── */

// Meses curtos em PT-BR (mesmo índice das <option value="...">)
const _EXP_MONTHS = ['Jan','Fev','Mar','Abr','Mai','Jun','Jul','Ago','Set','Out','Nov','Dez'];

/** Preenche selects de ano (atual → 1970) se ainda estiverem vazios. */
function _populateYearSelects() {
  const cur = new Date().getFullYear();
  ['exp-start-year','exp-end-year'].forEach(id => {
    const sel = document.getElementById(id);
    if (!sel || sel.options.length > 1) return; // já preenchido
    for (let y = cur; y >= 1970; y--) {
      const opt = document.createElement('option');
      opt.value = opt.textContent = y;
      sel.appendChild(opt);
    }
  });
}

/** Mostra/oculta a linha de data final conforme checkbox "Presente". */
function toggleExpPresente(chk) {
  const row = document.getElementById('exp-end-date-row');
  row.style.opacity        = chk.checked ? '0.35' : '1';
  row.style.pointerEvents  = chk.checked ? 'none'  : '';
}

/** Converte string de período em objeto {startM, startY, endM, endY, presente}. */
function _parsePeriod(period) {
  const result = { startM:'', startY:'', endM:'', endY:'', presente: false };
  if (!period) return result;
  const parts = period.split(' – ');
  if (parts[0]) {
    const [m, y] = parts[0].split(' ');
    result.startM = _EXP_MONTHS.includes(m) ? m : '';
    result.startY = y || '';
  }
  if (parts[1]) {
    if (parts[1].toLowerCase() === 'presente') {
      result.presente = true;
    } else {
      const [m, y] = parts[1].split(' ');
      result.endM = _EXP_MONTHS.includes(m) ? m : '';
      result.endY = y || '';
    }
  }
  return result;
}

function openExpModal(ds) {
  _populateYearSelects();
  document.getElementById('exp-modal-heading').textContent = ds?.id ? 'Editar experiência' : 'Adicionar experiência';
  document.getElementById('exp-editing-id').value = ds?.id      || '';
  document.getElementById('exp-title').value      = ds?.title   || '';
  document.getElementById('exp-company').value    = ds?.company || '';
  document.getElementById('exp-desc').value       = ds?.desc    || '';

  const p = _parsePeriod(ds?.period || '');
  document.getElementById('exp-start-month').value = p.startM;
  document.getElementById('exp-start-year').value  = p.startY;
  document.getElementById('exp-end-month').value   = p.endM;
  document.getElementById('exp-end-year').value    = p.endY;

  const chk = document.getElementById('exp-presente');
  chk.checked = p.presente;
  toggleExpPresente(chk);

  syncCharCounters();
  document.getElementById('port-exp-overlay').classList.add('open');
  setTimeout(() => document.getElementById('exp-title').focus(), 80);
}

/** Monta string de período a partir dos selects. Ex: "Jan 2022 – Presente" */
function _buildPeriod() {
  const sm = document.getElementById('exp-start-month').value;
  const sy = document.getElementById('exp-start-year').value;
  const presente = document.getElementById('exp-presente').checked;
  const em = document.getElementById('exp-end-month').value;
  const ey = document.getElementById('exp-end-year').value;

  const start = [sm, sy].filter(Boolean).join(' ') || '';
  let end = '';
  if (presente) {
    end = 'Presente';
  } else {
    end = [em, ey].filter(Boolean).join(' ');
  }
  if (!start && !end) return '';
  if (!end) return start;
  return `${start} – ${end}`;
}

async function saveExperience() {
  const editingId   = document.getElementById('exp-editing-id').value;
  const title       = document.getElementById('exp-title').value.trim();
  const companyName = document.getElementById('exp-company').value.trim();
  const period      = _buildPeriod();
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

/* ── Habilidades (catálogo) ───────────────────────────────── */

let _skillCatalog = null; // cache: ["Java", "Python", ...]

async function _loadSkillCatalog() {
  if (_skillCatalog) return _skillCatalog;
  const token = localStorage.getItem('firstech_access_token');
  try {
    const resp = await fetch('/api/portfolio/skills/catalog', {
      headers: { 'Authorization': 'Bearer ' + token },
    });
    _skillCatalog = resp.ok ? await resp.json() : [];
  } catch (_) { _skillCatalog = []; }
  return _skillCatalog;
}

function _renderSkillChips(skills) {
  const container = document.getElementById('skill-catalog-chips');
  if (!container) return;
  const selected = document.getElementById('skill-selected-name')?.value || '';

  if (!skills.length) {
    container.innerHTML = '<div class="text-xs text-text3 italic">Nenhuma habilidade encontrada.</div>';
    return;
  }
  container.innerHTML = skills.map(s => {
    const isSel = s === selected;
    const base  = 'skill-chip py-[4px] px-[11px] rounded-lg text-xs font-medium cursor-pointer border transition-all duration-150 ';
    const cls   = isSel
      ? base + 'bg-purple text-white border-purple'
      : base + 'bg-bg3 text-text2 border-border2 hover:border-purple2 hover:text-text1';
    const safe  = s.replace(/'/g, "\\'");
    return `<button type="button" class="${cls}" onclick="selectSkillFromCatalog('${safe}')">${s}</button>`;
  }).join('');
}

function filterSkillCatalog(query) {
  if (!_skillCatalog) return;
  const q = query.trim().toLowerCase();
  const filtered = q ? _skillCatalog.filter(s => s.toLowerCase().includes(q)) : _skillCatalog;
  _renderSkillChips(filtered);
}

function selectSkillFromCatalog(name) {
  document.getElementById('skill-selected-name').value = name;
  const badge = document.getElementById('skill-selected-badge');
  if (badge) badge.textContent = name;
  document.getElementById('skill-selected-display')?.classList.remove('hidden');
  // Re-renderiza chips para marcar selecionada
  const query = document.getElementById('skill-search')?.value || '';
  filterSkillCatalog(query);
}

async function openSkillModal(ds) {
  document.getElementById('skill-editing-id').value    = '';
  document.getElementById('skill-selected-name').value = '';
  document.getElementById('skill-search').value        = '';
  document.getElementById('skill-highlight').checked   = false;
  document.getElementById('skill-selected-display')?.classList.add('hidden');
  document.getElementById('skill-catalog-chips').innerHTML =
    '<div class="text-xs text-text3 italic">Carregando habilidades...</div>';

  document.getElementById('port-skill-overlay').classList.add('open');

  const catalog = await _loadSkillCatalog();
  _renderSkillChips(catalog);
  setTimeout(() => document.getElementById('skill-search')?.focus(), 80);
}

async function saveSkill() {
  const name      = document.getElementById('skill-selected-name').value.trim();
  const highlight = document.getElementById('skill-highlight').checked;

  if (!name) {
    showJobToast('Selecione uma habilidade da lista.', true);
    _highlightInvalid(document.getElementById('skill-search'));
    return;
  }

  const token = _portToken(); if (!token) return;

  try {
    const resp = await fetch('/api/portfolio/skills', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ name, highlight, orderIndex: 0 }),
    });
    if (!resp.ok) throw new Error((await resp.json().catch(() => ({}))).message || 'Erro ao salvar habilidade.');
    closePortModal('port-skill-overlay');
    showJobToast('Habilidade adicionada!');
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
  syncCharCounters();
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

/** Base64 da imagem de thumbnail selecionada no modal (null = sem nova imagem). */
let _projImageBase64 = null;

/** Abre o GitHub em nova aba (chamado pelo onclick do card). */
function openProjGithub(url) {
  if (url) window.open(url, '_blank', 'noopener');
}

/** Trata seleção de imagem para o thumbnail do projeto. */
function handleProjImage(input) {
  const file = input.files[0];
  if (!file) return;
  if (file.size > 1024 * 1024) {
    showJobToast('A imagem deve ter no máximo 1 MB.', true);
    input.value = '';
    return;
  }
  const reader = new FileReader();
  reader.onload = ev => {
    _projImageBase64 = ev.target.result;
    document.getElementById('proj-image-preview-img').src = _projImageBase64;
    document.getElementById('proj-image-preview').classList.remove('hidden');
    document.getElementById('proj-image-upload-label').classList.add('hidden');
  };
  reader.readAsDataURL(file);
}

/** Remove a imagem selecionada no modal de projeto. */
function clearProjImage() {
  _projImageBase64 = '';           // string vazia → sinaliza "remover imagem existente"
  document.getElementById('proj-image-preview').classList.add('hidden');
  document.getElementById('proj-image-upload-label').classList.remove('hidden');
  document.getElementById('proj-image-input').value = '';
}

function openProjModal(ds) {
  const heading = document.getElementById('proj-modal-heading');
  document.getElementById('proj-editing-id').value = ds?.id     || '';
  document.getElementById('proj-name').value        = ds?.name   || '';
  document.getElementById('proj-desc').value        = ds?.desc   || '';
  document.getElementById('proj-github').value      = ds?.github || '';
  heading.textContent = ds?.id ? 'Editar projeto' : 'Adicionar projeto';

  // Restaura imagem (se existente)
  const existingImage = ds?.image || null;
  if (existingImage && existingImage !== 'null') {
    _projImageBase64 = existingImage;
    document.getElementById('proj-image-preview-img').src = existingImage;
    document.getElementById('proj-image-preview').classList.remove('hidden');
    document.getElementById('proj-image-upload-label').classList.add('hidden');
  } else {
    _projImageBase64 = null;
    document.getElementById('proj-image-preview').classList.add('hidden');
    document.getElementById('proj-image-upload-label').classList.remove('hidden');
    document.getElementById('proj-image-input').value = '';
  }

  syncCharCounters();
  document.getElementById('port-proj-overlay').classList.add('open');
  setTimeout(() => document.getElementById('proj-name').focus(), 80);
}

async function saveProject() {
  const editingId       = document.getElementById('proj-editing-id').value;
  const name            = document.getElementById('proj-name').value.trim();
  const description     = document.getElementById('proj-desc').value.trim();
  const githubUrl       = document.getElementById('proj-github').value.trim() || null;
  // _projImageBase64: null = não alterar, '' = remover, 'data:...' = nova imagem
  const thumbImageBase64 = _projImageBase64;

  if (!name) { _highlightInvalid(document.getElementById('proj-name')); return; }

  const token = _portToken(); if (!token) return;
  const isEdit = !!editingId;
  const url    = isEdit ? `/api/portfolio/projects/${editingId}` : '/api/portfolio/projects';

  try {
    const resp = await fetch(url, {
      method: isEdit ? 'PUT' : 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ name, description, githubUrl, thumbImageBase64, orderIndex: 0 }),
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

/* ═══════════════════════════════════════════════════════════
   BUSCA — search screen
   ═══════════════════════════════════════════════════════════ */

let _searchTimer        = null;
let _currentSearchData  = [];   // todos os resultados da última busca
let _currentSearchFilter = 'tudo'; // filtro ativo

/** Debounce: dispara doSearch após 350 ms de inatividade */
function searchDebounced(q) {
  clearTimeout(_searchTimer);
  _searchTimer = setTimeout(() => doSearch(q.trim()), 350);
}

/** Busca os resultados no backend e renderiza */
async function doSearch(q) {
  const emptyState = document.getElementById('search-empty-state');
  const countEl    = document.getElementById('search-count');
  const results    = document.getElementById('search-results');
  const spinner    = document.getElementById('search-spinner');

  if (!q) {
    // Sem termo → mostra estado inicial
    _currentSearchData = [];
    results.innerHTML = '';
    countEl.classList.add('hidden');
    emptyState && emptyState.classList.remove('hidden');
    return;
  }

  emptyState && emptyState.classList.add('hidden');
  spinner && spinner.classList.remove('hidden');

  try {
    const token = localStorage.getItem('firstech_access_token') || '';
    const resp  = await fetch('/api/search?q=' + encodeURIComponent(q), {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {},
    });
    if (!resp.ok) throw new Error('Falha na busca');
    _currentSearchData = await resp.json();
  } catch (err) {
    _currentSearchData = [];
    results.innerHTML = '<div class="text-sm text-text3 text-center py-8">Erro ao buscar. Tente novamente.</div>';
    countEl.classList.add('hidden');
    spinner && spinner.classList.add('hidden');
    return;
  }

  spinner && spinner.classList.add('hidden');
  _renderSearchResults(q);
}

/** Troca o filtro ativo e re-renderiza sem nova requisição */
function setSearchFilter(filter, btn) {
  _currentSearchFilter = filter;
  // Atualiza estilo das abas
  btn.closest('.filter-tabs, [class*="flex gap"]')
    ?.querySelectorAll('.ftab').forEach(t => t.classList.remove('active'));
  btn.classList.add('active');
  // Re-renderiza com o filtro
  const q = (document.getElementById('search-input')?.value || '').trim();
  if (q) _renderSearchResults(q);
}

/** Renderiza os cards de resultado aplicando o filtro ativo */
function _renderSearchResults(q) {
  const countEl = document.getElementById('search-count');
  const container = document.getElementById('search-results');
  if (!container) return;

  const filtered = _currentSearchFilter === 'tudo'
    ? _currentSearchData
    : _currentSearchData.filter(r => r.tipo === _currentSearchFilter);

  // Contador
  const total = filtered.length;
  if (countEl) {
    countEl.textContent = total + ' resultado' + (total !== 1 ? 's' : '') + ' para "' + q + '"';
    countEl.classList.toggle('hidden', false);
  }

  if (total === 0) {
    container.innerHTML = `
      <div class="text-center py-12">
        <i class="ti ti-search-off text-[38px] text-text3 mb-3 block"></i>
        <div class="text-sm font-semibold text-text2 mb-1">Nenhum resultado encontrado</div>
        <div class="text-[13px] text-text3">Tente outros termos ou verifique a ortografia</div>
      </div>`;
    return;
  }

  container.innerHTML = filtered.map(r => {
    const isJob    = r.tipo === 'vaga';
    const radius   = isJob ? 'border-radius:8px' : '';
    const badge    = `badge-${r.tipo}`;
    const avatar   = r.avatarBase64
      ? `<img src="${r.avatarBase64}" alt="avatar" style="width:100%;height:100%;object-fit:cover;border-radius:inherit"/>`
      : _escapeHtml(r.inicial || '?');

    return `<div class="bg-bg2 border border-border2 rounded-xl py-[13px] px-[14px] mb-[9px] flex items-center gap-3 cursor-pointer transition-all duration-200 hover:border-border">
      <div class="w-[42px] h-[42px] shrink-0 overflow-hidden flex items-center justify-center text-sm font-bold text-white"
           style="background:${r.corAvatar};${radius};border-radius:${isJob ? '8px' : '50%'}">
        ${avatar}
      </div>
      <div class="flex-1 min-w-0">
        <div class="text-sm font-semibold text-text1 truncate">${_escapeHtml(r.nome)}</div>
        <div class="text-xs text-text2 mt-px truncate">${_escapeHtml(r.subtitulo)}</div>
      </div>
      <span class="text-[10px] font-semibold py-[3px] px-[9px] rounded-[20px] whitespace-nowrap ${badge}">${_escapeHtml(r.tipoLabel)}</span>
    </div>`;
  }).join('');
}

/* ═══════════════════════════════════════════════════════════
   FILTRO DE POSTS (Home)
   ═══════════════════════════════════════════════════════════ */

function filterPosts(query) {
  const q   = query.trim().toLowerCase();
  const feed = document.querySelector('#s-home .col:nth-child(2)');
  if (!feed) return;

  const cards = feed.querySelectorAll('[id^="post-"]');
  let visible = 0;

  cards.forEach(card => {
    const text = card.textContent.toLowerCase();
    const show = !q || text.includes(q);
    card.style.display = show ? '' : 'none';
    if (show) visible++;
  });

  // Contador
  const countEl = document.getElementById('home-post-count');
  if (countEl) {
    if (q) {
      countEl.textContent = visible + ' resultado' + (visible !== 1 ? 's' : '');
      countEl.classList.remove('hidden');
    } else {
      countEl.classList.add('hidden');
    }
  }

  // Empty state
  const emptyEl = feed.querySelector('[class*="border-dashed"]');
  if (emptyEl) emptyEl.style.display = (visible === 0 && !q) ? '' : 'none';
}

/* ═══════════════════════════════════════════════════════════
   FILTRO DE VAGAS (Jobs)
   ═══════════════════════════════════════════════════════════ */

function toggleJobFilter(btn) {
  btn.classList.toggle('active');
  filterJobCards();
}

function filterJobCards() {
  const query = (document.getElementById('job-search-input')?.value || '').trim().toLowerCase();

  const activeModalities = [...document.querySelectorAll('#job-modality-filters .ftab.active')]
    .map(b => b.dataset.value.toLowerCase());
  const activeLevels = [...document.querySelectorAll('#job-level-filters .ftab.active')]
    .map(b => b.dataset.value.toLowerCase());

  const cards = document.querySelectorAll('#s-jobs .jcard');
  let visible = 0;

  cards.forEach(card => {
    const modality = (card.dataset.modality || '').toLowerCase();
    const level    = (card.dataset.level    || '').toLowerCase();
    const text     = (card.dataset.text     || '').toLowerCase() + ' ' + card.textContent.toLowerCase();

    const matchesQuery    = !query               || text.includes(query);
    const matchesModality = !activeModalities.length || activeModalities.some(m => modality.includes(m));
    const matchesLevel    = !activeLevels.length    || activeLevels.some(l => level.includes(l));

    const show = matchesQuery && matchesModality && matchesLevel;
    card.style.display = show ? '' : 'none';
    if (show) visible++;
  });

  // Atualiza contador
  const countEl = document.getElementById('job-count-label');
  if (countEl) {
    const total = cards.length;
    countEl.textContent = visible + (visible !== total ? '/' + total : '') + ' vaga' + (total !== 1 ? 's' : '');
  }

  // Empty state para candidato
  const feed = document.querySelector('#s-jobs .col:nth-child(2)');
  let emptyState = feed?.querySelector('[id="job-filter-empty"]');
  if (!emptyState) {
    emptyState = document.createElement('div');
    emptyState.id = 'job-filter-empty';
    emptyState.className = 'text-center py-10';
    emptyState.innerHTML = '<i class="ti ti-search-off text-[36px] text-text3 mb-2 block"></i><div class="text-sm text-text3">Nenhuma vaga encontra os filtros selecionados.</div>';
    feed?.appendChild(emptyState);
  }
  emptyState.style.display = (visible === 0 && cards.length > 0) ? '' : 'none';
}

/** Escapa HTML para injeção segura via innerHTML */
function _escapeHtml(s) {
  if (!s) return '';
  return String(s)
    .replace(/&/g,'&amp;')
    .replace(/</g,'&lt;')
    .replace(/>/g,'&gt;')
    .replace(/"/g,'&quot;');
}

/* ═══════════════════════════════════════════════════════════
   BANNER DO PORTFÓLIO
   ═══════════════════════════════════════════════════════════ */

async function uploadBanner(input) {
  const file = input.files[0];
  if (!file) return;

  if (!file.type.startsWith('image/')) {
    showJobToast('Selecione um arquivo de imagem (JPEG, PNG, WebP).', true); return;
  }
  if (file.size > 3 * 1024 * 1024) {
    showJobToast('A imagem do banner deve ter no máximo 3 MB.', true); return;
  }

  const token = _portToken(); if (!token) return;
  const formData = new FormData();
  formData.append('file', file);

  try {
    const resp = await fetch('/api/portfolio/banner', {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + token },
      body: formData,
    });
    if (!resp.ok) {
      const err = await resp.json().catch(() => ({}));
      throw new Error(err.message || 'Erro ao enviar banner.');
    }
    showJobToast('Banner atualizado!');
    window.location.reload();
  } catch (err) {
    showJobToast('Erro: ' + err.message, true);
  }
}

/* ═══════════════════════════════════════════════════════════
   CURTIDAS E COMENTÁRIOS
   ═══════════════════════════════════════════════════════════ */

/** Alterna curtida no post via API e atualiza o botão */
async function toggleLike(postId, btn) {
  const token = localStorage.getItem('firstech_access_token');
  if (!token) { window.location.href = '/login'; return; }

  try {
    const resp = await fetch(`/api/posts/${postId}/like`, {
      method: 'POST',
      headers: { 'Authorization': 'Bearer ' + token },
    });
    if (!resp.ok) throw new Error();
    const { liked, count } = await resp.json();

    // Atualiza contagem
    const countEl = document.getElementById('like-count-' + postId);
    if (countEl) countEl.textContent = count;

    // Atualiza ícone e cor
    const icon = btn.querySelector('i');
    if (liked) {
      icon.className = 'ti ti-heart-filled text-base';
      btn.classList.add('text-red');
      btn.classList.remove('text-text3', 'hover:text-red');
    } else {
      icon.className = 'ti ti-heart text-base';
      btn.classList.remove('text-red');
      btn.classList.add('text-text3', 'hover:text-red');
    }
    btn.dataset.liked = liked ? 'true' : 'false';
  } catch (_) {
    showJobToast('Não foi possível registrar a curtida.', true);
  }
}

/** Expande / recolhe seção de comentários de um post */
async function toggleComments(postId, btn) {
  const section = document.getElementById('comments-' + postId);
  if (!section) return;

  const isOpen = !section.classList.contains('hidden');
  if (isOpen) {
    section.classList.add('hidden');
    return;
  }

  section.classList.remove('hidden');
  const list = document.getElementById('comments-list-' + postId);
  if (!list || list.dataset.loaded) return; // já carregado

  list.innerHTML = '<div class="text-[12px] text-text3">Carregando comentários...</div>';

  try {
    const token = localStorage.getItem('firstech_access_token') || '';
    const resp = await fetch(`/api/posts/${postId}/comments`, {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {},
    });
    const comments = resp.ok ? await resp.json() : [];
    list.dataset.loaded = 'true';
    _renderComments(list, comments);
  } catch (_) {
    list.innerHTML = '<div class="text-[12px] text-red">Erro ao carregar comentários.</div>';
  }
}

/** Renderiza comentários numa lista */
function _renderComments(listEl, comments) {
  if (!comments.length) {
    listEl.innerHTML = '<div class="text-[12px] text-text3 italic">Ainda sem comentários. Seja o primeiro!</div>';
    return;
  }
  listEl.innerHTML = comments.map(c => {
    const avatarHtml = c.autorAvatarBase64
      ? `<img src="${_escapeHtml(c.autorAvatarBase64)}" alt="" class="w-full h-full object-cover"/>`
      : `<span class="text-[10px] font-bold text-white">${_escapeHtml(c.autorInicial)}</span>`;
    return `<div class="flex gap-[8px]">
      <div class="w-7 h-7 rounded-full shrink-0 overflow-hidden flex items-center justify-center comment-avatar">
        ${avatarHtml}
      </div>
      <div class="flex-1 bg-bg3 rounded-[9px] px-3 py-2">
        <div class="flex items-baseline gap-2 mb-[2px]">
          <span class="text-[12px] font-semibold text-text1">${_escapeHtml(c.autorNome)}</span>
          <span class="text-[10px] text-text3">${_escapeHtml(c.tempoRelativo)}</span>
        </div>
        <div class="text-[13px] text-text2 leading-[1.5]">${_escapeHtml(c.content)}</div>
      </div>
    </div>`;
  }).join('');
}

/** Enter no campo de comentário */
function handleCommentKey(e, postId) {
  if (e.key === 'Enter' && !e.shiftKey) { e.preventDefault(); submitComment(postId); }
}

/** Envia novo comentário */
async function submitComment(postId) {
  const input = document.getElementById('comment-input-' + postId);
  const content = input?.value.trim();
  if (!content) return;

  const token = localStorage.getItem('firstech_access_token');
  if (!token) { window.location.href = '/login'; return; }

  input.value = '';
  input.disabled = true;

  try {
    const resp = await fetch(`/api/posts/${postId}/comments`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', 'Authorization': 'Bearer ' + token },
      body: JSON.stringify({ content }),
    });
    if (!resp.ok) throw new Error();
    const comment = await resp.json();

    // Adiciona à lista e atualiza contagem
    const list = document.getElementById('comments-list-' + postId);
    if (list) {
      if (list.querySelector('.italic')) list.innerHTML = ''; // remove "sem comentários"
      const item = document.createElement('div');
      item.innerHTML = _renderComments._html ? '' : ''; // trick to reuse
      // Build the item directly
      const avatarHtml = comment.autorAvatarBase64
        ? `<img src="${_escapeHtml(comment.autorAvatarBase64)}" alt="" class="w-full h-full object-cover"/>`
        : `<span class="text-[10px] font-bold text-white">${_escapeHtml(comment.autorInicial)}</span>`;
      item.className = 'flex gap-[8px]';
      item.innerHTML = `
        <div class="w-7 h-7 rounded-full shrink-0 overflow-hidden flex items-center justify-center comment-avatar">
          ${avatarHtml}
        </div>
        <div class="flex-1 bg-bg3 rounded-[9px] px-3 py-2">
          <div class="flex items-baseline gap-2 mb-[2px]">
            <span class="text-[12px] font-semibold text-text1">${_escapeHtml(comment.autorNome)}</span>
            <span class="text-[10px] text-text3">agora</span>
          </div>
          <div class="text-[13px] text-text2 leading-[1.5]">${_escapeHtml(comment.content)}</div>
        </div>`;
      list.appendChild(item);
    }

    // Incrementa contagem no botão
    const countEl = document.getElementById('comment-count-' + postId);
    if (countEl) countEl.textContent = parseInt(countEl.textContent || '0') + 1;

  } catch (_) {
    showJobToast('Não foi possível enviar o comentário.', true);
  } finally {
    if (input) input.disabled = false;
  }
}

/* ═══════════════════════════════════════════════════════════
   AUTOCOMPLETE DE EMPRESA (Clearbit)
   ═══════════════════════════════════════════════════════════ */

let _companyTimer = null;

/**
 * Autocomplete de empresas usando a API do Clearbit.
 * Popula os campos ocultos job-company-name e job-company-logo-url.
 */
async function companyAutocomplete(input) {
  const query = input.value.trim();
  const dd    = document.getElementById('job-company-dropdown');
  clearTimeout(_companyTimer);

  if (query.length < 2) { dd?.classList.add('hidden'); return; }

  _companyTimer = setTimeout(async () => {
    try {
      const resp = await fetch(
        `https://autocomplete.clearbit.com/v1/companies/suggest?query=${encodeURIComponent(query)}`
      );
      if (!resp.ok) throw new Error();
      const companies = await resp.json(); // [{name, domain, logo}]

      if (!companies.length) { dd?.classList.add('hidden'); return; }

      dd.innerHTML = companies.slice(0, 7).map(c => `<button type="button"
          class="w-full text-left px-3 py-[8px] flex items-center gap-3 text-[13px] text-text2 transition-colors duration-100 hover:bg-bg3 hover:text-text1 border-b border-border2 last:border-b-0"
          data-co-name="${_escapeHtml(c.name || '')}"
          data-co-logo="${_escapeHtml(c.logo || '')}"
          onclick="selectCompanyFromBtn(this)"
        >
          ${c.logo
            ? `<img src="${_escapeHtml(c.logo)}" alt="" class="w-5 h-5 rounded object-contain shrink-0"/>`
            : `<div class="w-5 h-5 rounded bg-bg4 flex items-center justify-center text-[10px] font-bold text-text3 shrink-0">${_escapeHtml((c.name||'?').charAt(0).toUpperCase())}</div>`
          }
          <span class="flex-1 truncate">${_escapeHtml(c.name || '')}</span>
          <span class="text-[11px] text-text3 shrink-0">${_escapeHtml(c.domain || '')}</span>
        </button>`).join('');
      dd.classList.remove('hidden');
    } catch (_) {
      dd?.classList.add('hidden');
    }
  }, 300);
}

/** Seleciona uma empresa a partir do botão do dropdown (sem problemas de escaping) */
function selectCompanyFromBtn(btn) {
  _setCompanyFields(btn.dataset.coName, btn.dataset.coLogo);
  document.getElementById('job-company-dropdown')?.classList.add('hidden');
}

/** @deprecated use selectCompanyFromBtn */
function selectCompany({ name, logo }) {
  _setCompanyFields(name, logo);
  document.getElementById('job-company-dropdown')?.classList.add('hidden');
}

/** Preenche os campos de empresa (autocomplete visível + ocultos) */
function _setCompanyFields(name, logoUrl) {
  const ac     = document.getElementById('job-company-ac');
  const hidden = document.getElementById('job-company-name');
  const hidLogo= document.getElementById('job-company-logo-url');
  const preview= document.getElementById('job-company-logo-preview');
  const img    = document.getElementById('job-company-logo-img');
  const icon   = document.getElementById('job-company-search-icon');
  const clear  = document.getElementById('job-company-clear');

  if (ac)      ac.value      = name || '';
  if (hidden)  hidden.value  = name || '';
  if (hidLogo) hidLogo.value = logoUrl || '';

  if (logoUrl && preview && img) {
    img.src = logoUrl;
    preview.classList.remove('hidden');
    if (icon) icon.classList.add('hidden');
  } else if (preview) {
    preview.classList.add('hidden');
    if (icon) icon.classList.remove('hidden');
  }

  if (clear) clear.classList.toggle('hidden', !name);
}

/** Limpa a seleção de empresa */
function clearCompanySelection() {
  _setCompanyFields('', '');
}

// Fecha dropdown de empresa ao clicar fora
document.addEventListener('click', e => {
  if (!e.target.closest('#job-company-ac') && !e.target.closest('#job-company-dropdown')) {
    document.getElementById('job-company-dropdown')?.classList.add('hidden');
  }
});

/* ═══════════════════════════════════════════════════════════
   PERFIL PÚBLICO DE USUÁRIO (Modal)
   ═══════════════════════════════════════════════════════════ */

/* ═══════════════════════════════════════════════════════════
   PERFIL PÚBLICO DE USUÁRIO — Drawer lateral
   ═══════════════════════════════════════════════════════════ */

let _activeProfileUserId = null;

/** Navega para a página pública de perfil do usuário. */
function openUserProfile(userId) {
  window.location.href = '/profile/' + userId;
}

function _renderUserProfile(p) {
  // ── Banner ──────────────────────────────────────────────────
  const bannerWrap = document.getElementById('upm-banner-img-wrap');
  const bannerImg  = document.getElementById('upm-banner-img');
  if (p.bannerBase64) {
    bannerImg.src = p.bannerBase64;
    bannerWrap.classList.remove('hidden');
  } else {
    bannerWrap.classList.add('hidden');
  }

  // ── Avatar ──────────────────────────────────────────────────
  const avatarEl = document.getElementById('upm-avatar');
  avatarEl.style.background = p.corAvatar;
  avatarEl.innerHTML = p.avatarBase64
    ? `<img src="${p.avatarBase64}" class="w-full h-full object-cover" alt=""/>`
    : `<span>${_escapeHtml(p.inicial)}</span>`;

  // ── Texto ────────────────────────────────────────────────────
  document.getElementById('upm-name').textContent          = p.nome;
  document.getElementById('upm-headline').textContent      = p.headline;
  document.getElementById('upm-location-text').textContent = p.cidade || 'Localização não informada';
  document.getElementById('upm-connections').textContent   = p.totalConexoes;

  // ── Botão de conexão ─────────────────────────────────────────
  _updateConnectBtn(p.connectionStatus);

  // ── Botão de mensagem (oculto no próprio perfil) ─────────────
  const msgBtn = document.getElementById('upm-message-btn');
  if (msgBtn) msgBtn.style.display = p.connectionStatus === 'SELF' ? 'none' : '';

  // ── Sobre ────────────────────────────────────────────────────
  const sobreSection = document.getElementById('upm-sobre-section');
  const sobreEl      = document.getElementById('upm-sobre');
  if (p.sobre) {
    sobreEl.textContent = p.sobre;
    sobreSection.classList.remove('hidden');
  } else {
    sobreSection.classList.add('hidden');
  }

  // ── Tecnologias ──────────────────────────────────────────────
  const techSection = document.getElementById('upm-tech-section');
  const techChips   = document.getElementById('upm-tech-chips');
  if (p.tecnologias && p.tecnologias.length > 0) {
    techChips.innerHTML = p.tecnologias.map(t =>
      `<span class="text-[11px] py-[3px] px-[10px] rounded-lg bg-bg3 border border-border2 text-text2">${_escapeHtml(t)}</span>`
    ).join('');
    techSection.classList.remove('hidden');
  } else {
    techSection.classList.add('hidden');
  }

  // ── Experiências ─────────────────────────────────────────────
  const expSection = document.getElementById('upm-exp-section');
  const expList    = document.getElementById('upm-exp-list');
  if (p.experiencias && p.experiencias.length > 0) {
    expList.innerHTML = p.experiencias.map((e, i) => `
      <div class="${i > 0 ? 'pt-4 border-t border-border2' : ''} flex gap-3">
        <div class="w-9 h-9 rounded-[8px] shrink-0 flex items-center justify-center text-[13px] font-bold text-white"
             style="background:${_escapeHtml(e.empresa?.corLogo || 'linear-gradient(135deg,#6d28d9,#8b5cf6)')}">
          ${_escapeHtml(e.empresa?.inicial || 'E')}
        </div>
        <div class="flex-1 min-w-0">
          <div class="text-[13px] font-semibold text-text1">${_escapeHtml(e.cargo || '')}</div>
          <div class="text-[12px] text-purple2 font-medium">${_escapeHtml(e.empresa?.nome || '')}</div>
          <div class="text-[11px] text-text3 mt-[2px]">${_escapeHtml(e.periodo || '')}</div>
          ${e.descricao ? `<div class="text-[12px] text-text2 mt-[5px] leading-[1.6]">${_escapeHtml(e.descricao)}</div>` : ''}
        </div>
      </div>`).join('');
    expSection.classList.remove('hidden');
  } else {
    expSection.classList.add('hidden');
  }

  // ── Habilidades ──────────────────────────────────────────────
  const skillsSection = document.getElementById('upm-skills-section');
  const skillsList    = document.getElementById('upm-skills-list');
  if (p.habilidades && p.habilidades.length > 0) {
    skillsList.innerHTML = p.habilidades.map(h =>
      `<span class="py-[4px] px-[12px] rounded-lg text-xs font-medium border ${h.destaque
        ? 'bg-purplebg text-purple2 border-border'
        : 'bg-bg4 text-text2 border-border2'}">${_escapeHtml(h.nome)}</span>`
    ).join('');
    skillsSection.classList.remove('hidden');
  } else {
    skillsSection.classList.add('hidden');
  }

  // ── Certificações ─────────────────────────────────────────────
  const certSection = document.getElementById('upm-cert-section');
  const certList    = document.getElementById('upm-cert-list');
  if (p.certificacoes && p.certificacoes.length > 0) {
    certList.innerHTML = p.certificacoes.map(c => `
      <div class="flex items-center gap-[10px]">
        <div class="w-9 h-9 rounded-lg bg-bg4 flex items-center justify-center text-[18px] shrink-0">${c.emoji || '📜'}</div>
        <div class="min-w-0">
          <div class="text-[13px] font-semibold text-text1">${_escapeHtml(c.nome || '')}</div>
          <div class="text-[11px] text-text3">${_escapeHtml(c.emissor || '')}${c.dataEmissao ? ' · ' + _escapeHtml(c.dataEmissao) : ''}</div>
        </div>
      </div>`).join('');
    certSection.classList.remove('hidden');
  } else {
    certSection.classList.add('hidden');
  }

  // ── Projetos (candidatos) / Vagas (recrutadores) ────────────
  const projSection = document.getElementById('upm-proj-section');
  const projList    = document.getElementById('upm-proj-list');
  const jobsSection = document.getElementById('upm-jobs-section');
  const jobsList    = document.getElementById('upm-jobs-list');

  if (p.recrutador) {
    // Recrutadores: esconde projetos, mostra vagas publicadas
    projSection.classList.add('hidden');
    if (p.vagasPublicadas && p.vagasPublicadas.length > 0) {
      jobsList.innerHTML = p.vagasPublicadas.map(v => {
        const logoHtml = v.companyLogoUrl
          ? `<img src="${_escapeHtml(v.companyLogoUrl)}" class="w-full h-full object-cover" alt=""/>`
          : `<span class="text-[14px] font-bold text-white">${_escapeHtml((v.empresa || '?')[0].toUpperCase())}</span>`;
        const badges = [v.modalidade, v.nivel].filter(Boolean).map(b =>
          `<span class="text-[9px] font-semibold px-[7px] py-[2px] rounded-full bg-bg4 text-text3 border border-border2">${_escapeHtml(b)}</span>`
        ).join('');
        const statusHtml = v.ativa
          ? `<span class="text-[9px] font-bold px-[8px] py-[3px] rounded-full text-green" style="background:rgba(52,211,153,.1);border:1px solid rgba(52,211,153,.25)">Ativa</span>`
          : `<span class="text-[9px] font-bold px-[8px] py-[3px] rounded-full bg-bg4 text-text3 border border-border2">Encerrada</span>`;
        return `
          <div class="flex items-center gap-[12px] p-[10px_12px] bg-bg4 rounded-[10px] border border-border2">
            <div class="w-9 h-9 rounded-[8px] shrink-0 overflow-hidden flex items-center justify-center"
                 style="background:${_escapeHtml(v.companyLogo || 'linear-gradient(135deg,#6d28d9,#8b5cf6)')}">${logoHtml}</div>
            <div class="flex-1 min-w-0">
              <div class="text-[12px] font-semibold text-text1 truncate">${_escapeHtml(v.titulo || '')}</div>
              <div class="text-[11px] text-text3 truncate">${_escapeHtml(v.empresa || '')}</div>
              ${badges ? `<div class="flex gap-[5px] mt-[4px] flex-wrap">${badges}</div>` : ''}
            </div>
            ${statusHtml}
          </div>`;
      }).join('');
      jobsSection.classList.remove('hidden');
    } else {
      jobsList.innerHTML = '<div class="text-[12px] text-text3 italic text-center py-2">Nenhuma vaga publicada ainda.</div>';
      jobsSection.classList.remove('hidden');
    }
  } else {
    // Candidatos: mostra projetos, esconde vagas
    jobsSection.classList.add('hidden');
    if (p.projetos && p.projetos.length > 0) {
      projList.innerHTML = p.projetos.map(proj => `
        <div class="bg-bg4 rounded-[10px] overflow-hidden border border-border2 ${proj.githubUrl ? 'cursor-pointer hover:border-border' : ''} transition-colors"
             ${proj.githubUrl ? `onclick="window.open('${_escapeHtml(proj.githubUrl)}','_blank')"` : ''}>
          <div class="h-[72px] flex items-center justify-center"
               style="background:${_escapeHtml(proj.corThumb || 'linear-gradient(135deg,#0f0521,#2d1b6e)')}">
            ${proj.thumbImageBase64
              ? `<img src="${proj.thumbImageBase64}" class="w-full h-full object-cover" alt=""/>`
              : `<i class="ti ${_escapeHtml(proj.icone || 'ti-code')}" style="font-size:24px;color:var(--purple2)"></i>`}
          </div>
          <div class="p-[8px_10px]">
            <div class="text-[12px] font-semibold text-text1 truncate flex items-center gap-1">
              ${_escapeHtml(proj.nome || '')}
              ${proj.githubUrl ? '<i class="ti ti-brand-github text-[11px] text-text3 shrink-0"></i>' : ''}
            </div>
            <div class="text-[10px] text-text3 leading-[1.4] mt-[2px] line-clamp-2">${_escapeHtml(proj.descricao || '')}</div>
          </div>
        </div>`).join('');
      projSection.classList.remove('hidden');
    } else {
      projSection.classList.add('hidden');
    }
  }

  // ── Mostrar conteúdo ─────────────────────────────────────────
  document.getElementById('upm-loading').classList.add('hidden');
  const content = document.getElementById('upm-content');
  content.classList.remove('hidden');
  content.style.display = 'flex';
}

function _updateConnectBtn(status) {
  const btn = document.getElementById('upm-connect-btn');
  if (!btn) return;
  const configs = {
    'SELF':             { label: 'Você',           style: 'background:var(--bg3);color:var(--text3);border:1px solid var(--border2);cursor:default',                    disabled: true  },
    'NONE':             { label: '+ Conectar',      style: 'background:var(--purple);color:#fff;border:none',                                                           disabled: false },
    'PENDING_SENT':     { label: '✓ Solicitado',    style: 'background:var(--bg3);color:var(--text3);border:1px solid var(--border2);cursor:default',                   disabled: true  },
    'PENDING_RECEIVED': { label: 'Aceitar pedido',  style: 'background:rgba(52,211,153,.15);color:var(--green);border:1px solid rgba(52,211,153,.3)',                   disabled: false },
    'ACCEPTED':         { label: '✓ Conectado',     style: 'background:var(--bg3);color:var(--green);border:1px solid rgba(52,211,153,.3)',                             disabled: false },
  };
  const cfg = configs[status] || configs['NONE'];
  btn.style.cssText = `display:flex;align-items:center;gap:6px;padding:7px 14px;border-radius:9px;font-family:inherit;font-size:13px;font-weight:600;cursor:pointer;transition:opacity .2s;${cfg.style}`;
  btn.innerHTML  = cfg.label;
  btn.disabled   = cfg.disabled;
  btn.dataset.connStatus = status;
}

async function handleConnectionAction() {
  const btn    = document.getElementById('upm-connect-btn');
  const status = btn.dataset.connStatus;
  const userId = _activeProfileUserId;
  if (!userId) return;

  const token = localStorage.getItem('firstech_access_token');
  const headers = { 'Content-Type': 'application/json', ...(token ? { 'Authorization': 'Bearer ' + token } : {}) };

  try {
    let res;
    if (status === 'NONE') {
      res = await fetch(`/api/connections/request/${userId}`, { method: 'POST', headers });
    } else if (status === 'PENDING_RECEIVED') {
      res = await fetch(`/api/connections/accept/${userId}`, { method: 'POST', headers });
    } else if (status === 'ACCEPTED') {
      if (!confirm('Remover esta conexão?')) return;
      res = await fetch(`/api/connections/${userId}`, { method: 'DELETE', headers });
    } else {
      return;
    }
    if (!res.ok) throw new Error();
    const data = await res.json();
    _updateConnectBtn(data.status);
    document.getElementById('upm-connections').textContent = data.totalConnections;
  } catch {
    console.error('Falha ao atualizar conexão.');
  }
}

function closeUserProfile() {
  document.getElementById('user-profile-overlay').classList.remove('open');
  document.body.style.overflow = '';
  _activeProfileUserId = null;
}

function startMessageFromProfile() {
  if (!_activeProfileUserId) return;
  const name = document.getElementById('upm-name').textContent;
  const userId = _activeProfileUserId;
  closeUserProfile();
  go('messages');
  loadConversations();
  switchMsgTab('convs');
  openConversation(userId, name);
}

function openUserProfileFromMsg() {
  if (_activeChatUserId) openUserProfile(_activeChatUserId);
}

/* ═══════════════════════════════════════════════════════════
   MENSAGENS — estado
   ═══════════════════════════════════════════════════════════ */

let _activeChatUserId = null;
let _activeChatName   = null;
let _convData         = [];
let _connData         = [];       // cache de conexões aceitas
let _requestsData     = [];       // cache de pedidos recebidos
let _msgPollTimer     = null;
let _activeMsgTab     = 'convs';
let _connLoaded       = false;
let _requestsLoaded   = false;
let _peopleSrchTimer  = null;

/* ── Troca de aba ────────────────────────────────────────── */

function switchMsgTab(tab) {
  _activeMsgTab = tab;
  const tabs = ['convs', 'people', 'requests'];

  tabs.forEach(t => {
    const panel = document.getElementById('msg-panel-' + t);
    const btn   = document.getElementById('msg-tab-' + t);
    const isActive = (t === tab);

    // painel
    if (isActive) {
      panel.classList.remove('hidden');
      panel.style.display = 'flex';
    } else {
      panel.classList.add('hidden');
      panel.style.display = '';
    }

    // estilo do botão
    if (btn) {
      btn.style.cssText = isActive
        ? 'border-bottom:2px solid var(--purple2);color:var(--purple2);background:transparent;'
        : 'border-bottom:2px solid transparent;color:var(--text3);background:transparent;';
    }
  });

  // Carrega dados sob demanda
  if (tab === 'people' && !_connLoaded) {
    loadAllPeopleForMsg();
    _connLoaded = true;
  }
  if (tab === 'requests' && !_requestsLoaded) {
    loadConnectionRequests();
    _requestsLoaded = true;
  }
}

/* ── Conversas ───────────────────────────────────────────── */

/** Carrega e renderiza a lista de conversas. Chamado ao navegar para a tela. */
async function loadConversations() {
  const loading = document.getElementById('conv-loading');
  loading && loading.classList.remove('hidden');

  try {
    const token = localStorage.getItem('firstech_access_token');
    const res = await fetch('/api/messages/conversations', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    });
    if (!res.ok) throw new Error();
    _convData = await res.json();
    _renderConversations(_convData);
    _updateUnreadBadge(_convData.reduce((s, c) => s + c.unreadCount, 0));
  } catch {
    console.error('Falha ao carregar conversas');
  } finally {
    loading && loading.classList.add('hidden');
  }

  // Carrega badge de pedidos pendentes silenciosamente
  _pollPendingRequests();
}

async function _pollPendingRequests() {
  try {
    const token = localStorage.getItem('firstech_access_token');
    if (!token) return;
    const res = await fetch('/api/connections/pending-received', {
      headers: { 'Authorization': 'Bearer ' + token }
    });
    if (!res.ok) return;
    const data = await res.json();
    _updateRequestsBadge(data.length);
    // Se a aba de pedidos já estiver carregada, atualiza
    if (_requestsLoaded) {
      _requestsData = data;
      _renderConnectionRequests(data);
    }
  } catch {}
}

function _renderConversations(convs) {
  const list  = document.getElementById('conversations-list');
  const empty = document.getElementById('conv-empty');
  list.querySelectorAll('.conv-item').forEach(el => el.remove());

  if (!convs.length) {
    if (empty) empty.style.removeProperty('display');
    return;
  }
  if (empty) empty.style.setProperty('display', 'none', 'important');

  convs.forEach(c => {
    const item = document.createElement('button');
    item.className = `conv-item w-full flex items-center gap-3 px-4 py-[11px] text-left border-b border-border2 hover:bg-bg3 transition-colors duration-150${_activeChatUserId == c.userId ? ' active' : ''}`;
    item.dataset.userId   = c.userId;
    item.dataset.userName = c.nome;
    item.onclick = () => openConversation(c.userId, c.nome);

    const avatarHtml = c.avatarBase64
      ? `<img src="${c.avatarBase64}" class="w-full h-full object-cover" alt=""/>`
      : `<span class="text-sm font-bold text-white">${_escapeHtml(c.inicial)}</span>`;

    item.innerHTML = `
      <div class="w-9 h-9 rounded-full shrink-0 overflow-hidden flex items-center justify-center" style="background:${c.corAvatar}">
        ${avatarHtml}
      </div>
      <div class="flex-1 min-w-0">
        <div class="flex items-center justify-between gap-2">
          <div class="text-[13px] font-semibold text-text1 truncate">${_escapeHtml(c.nome)}</div>
          <div class="text-[10px] text-text3 shrink-0">${_escapeHtml(c.lastMessageTime)}</div>
        </div>
        <div class="text-[12px] text-text3 truncate">${_escapeHtml(c.lastMessage)}</div>
      </div>
      ${c.unreadCount > 0
        ? `<div class="min-w-[18px] h-[18px] rounded-full bg-purple text-white text-[9px] font-bold flex items-center justify-center px-1 shrink-0">${c.unreadCount}</div>`
        : ''}`;
    list.appendChild(item);
  });
}

function filterConversations(q) {
  const norm = q.toLowerCase().trim();
  if (!norm) { _renderConversations(_convData); return; }
  _renderConversations(_convData.filter(c => c.nome.toLowerCase().includes(norm)));
}

/* ── Aba Pessoas ─────────────────────────────────────────── */

let _allPeopleData = [];   // cache de todos os usuários da plataforma

/**
 * Carrega em paralelo as conexões do usuário (para badges "Conectado") e
 * todos os usuários da plataforma (para renderizar na aba Pessoas).
 */
async function loadAllPeopleForMsg() {
  const container = document.getElementById('msg-connections-list');
  const loadingEl = document.getElementById('conn-msg-loading');
  if (!container) return;
  loadingEl && loadingEl.classList.remove('hidden');

  try {
    const token = localStorage.getItem('firstech_access_token');
    const headers = token ? { 'Authorization': 'Bearer ' + token } : {};

    // Busca conexões + todos usuários em paralelo
    const [connRes, peopleRes] = await Promise.all([
      fetch('/api/connections/my',       { headers }),
      fetch('/api/search/users?q=',      { headers })
    ]);

    if (connRes.ok)   _connData      = await connRes.json();
    if (peopleRes.ok) _allPeopleData = await peopleRes.json();

    const labelEl = document.querySelector('#msg-people-section-label span');
    if (labelEl) labelEl.textContent = 'Todas as pessoas';
    _renderPeopleSearchForMsg(_allPeopleData);
  } catch {
    if (container) container.innerHTML = '<div class="px-4 py-3 text-[12px] text-text3">Erro ao carregar pessoas.</div>';
  } finally {
    loadingEl && loadingEl.classList.add('hidden');
  }
}

/** @deprecated Usado apenas internamente para refrescar _connData sem re-render. */
async function _refreshConnData() {
  try {
    const token = localStorage.getItem('firstech_access_token');
    const res = await fetch('/api/connections/my', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    });
    if (res.ok) _connData = await res.json();
  } catch { /* silencioso */ }
}

function _renderConnectionsForMsg(conns) {
  const container = document.getElementById('msg-connections-list');
  if (!container) return;

  if (!conns.length) {
    container.innerHTML = '<div class="px-4 py-4 text-[12px] text-text3 text-center">Você ainda não tem conexões.<br>Conecte-se com pessoas via busca acima!</div>';
    return;
  }

  container.innerHTML = '';
  conns.forEach(c => {
    const item = document.createElement('div');
    item.className = 'flex items-center gap-3 px-4 py-[9px] hover:bg-bg3 transition-colors border-b border-border2 last:border-b-0 cursor-pointer group';
    item.onclick = () => openUserProfile(c.id);

    const avatarHtml = c.avatarBase64
      ? `<img src="${c.avatarBase64}" class="w-full h-full object-cover" alt=""/>`
      : `<span class="text-[11px] font-bold text-white">${_escapeHtml(c.inicial)}</span>`;

    item.innerHTML = `
      <div class="w-8 h-8 rounded-full shrink-0 overflow-hidden flex items-center justify-center" style="background:${c.corAvatar}">${avatarHtml}</div>
      <div class="flex-1 min-w-0">
        <div class="text-[13px] font-semibold text-text1 truncate group-hover:text-purple2 transition-colors">${_escapeHtml(c.nome)}</div>
        <div class="text-[11px] text-text3 truncate">${_escapeHtml(c.headline || '')}</div>
      </div>
      <button class="shrink-0 w-7 h-7 rounded-lg border border-border2 bg-transparent text-text3 flex items-center justify-center cursor-pointer hover:bg-bg4 hover:text-purple2 transition-colors opacity-0 group-hover:opacity-100"
              title="Enviar mensagem"
              onclick="event.stopPropagation();switchMsgTab('convs');openConversation(${c.id},'${_escapeHtml(c.nome).replace(/'/g,"\\'")}')">
        <i class="ti ti-message-circle text-[13px]"></i>
      </button>`;
    container.appendChild(item);
  });
}

/** Busca pessoas (debounced). Quando vazia → mostra todos da plataforma (cache). */
let _peopleSrchData = [];
function searchPeopleForMsg(q) {
  clearTimeout(_peopleSrchTimer);
  const query = q.trim();
  const labelEl = document.querySelector('#msg-people-section-label span');

  if (!query) {
    // Sem texto: mostra todos os usuários da plataforma (usa cache se disponível)
    if (labelEl) labelEl.textContent = 'Todas as pessoas';
    if (_allPeopleData.length) {
      _renderPeopleSearchForMsg(_allPeopleData);
    } else {
      loadAllPeopleForMsg();
    }
    return;
  }

  _peopleSrchTimer = setTimeout(async () => {
    const container = document.getElementById('msg-connections-list');
    if (labelEl) labelEl.textContent = `Resultados para "${query}"`;
    if (container) container.innerHTML = '<div class="flex items-center justify-center py-5"><div class="w-4 h-4 border-2 border-purple border-t-transparent rounded-full animate-spin"></div></div>';

    try {
      const token = localStorage.getItem('firstech_access_token');
      // Usa o endpoint dedicado de usuários (candidatos + recrutadores)
      const res = await fetch(`/api/search/users?q=${encodeURIComponent(query)}`, {
        headers: token ? { 'Authorization': 'Bearer ' + token } : {}
      });
      if (!res.ok) throw new Error();
      _peopleSrchData = await res.json();
      _renderPeopleSearchForMsg(_peopleSrchData);
    } catch {
      if (container) container.innerHTML = '<div class="px-4 py-3 text-[12px] text-text3">Erro ao buscar.</div>';
    }
  }, 350);
}

function _renderPeopleSearchForMsg(people) {
  const container = document.getElementById('msg-connections-list');
  if (!container) return;

  if (!people.length) {
    container.innerHTML = '<div class="px-4 py-4 text-[12px] text-text3 text-center">Nenhuma pessoa encontrada.</div>';
    return;
  }

  container.innerHTML = '';
  people.forEach(p => {
    const isConn = _connData.some(c => c.id == p.id);
    const item = document.createElement('div');
    item.className = 'flex items-center gap-3 px-4 py-[9px] border-b border-border2 last:border-b-0 hover:bg-bg3 transition-colors cursor-pointer group';
    item.onclick = () => openUserProfile(p.id);

    const avatarHtml = p.avatarBase64
      ? `<img src="${p.avatarBase64}" class="w-full h-full object-cover" alt=""/>`
      : `<span class="text-[11px] font-bold text-white">${_escapeHtml(p.inicial || '?')}</span>`;

    // Badge de conexão ou botão de mensagem rápida
    const connBadge = isConn
      ? `<div class="flex items-center gap-1 shrink-0">
           <span class="text-[9px] font-semibold px-[6px] py-[1px] rounded-full text-green" style="background:rgba(52,211,153,.1);border:1px solid rgba(52,211,153,.25)">Conectado</span>
           <button class="w-7 h-7 rounded-lg border border-border2 bg-transparent text-text3 flex items-center justify-center cursor-pointer hover:bg-bg4 hover:text-purple2 transition-colors opacity-0 group-hover:opacity-100"
                   title="Enviar mensagem"
                   onclick="event.stopPropagation();switchMsgTab('convs');openConversation(${p.id},'${_escapeHtml(p.nome).replace(/'/g,"\\'")}')">
             <i class="ti ti-message-circle text-[13px]"></i>
           </button>
         </div>`
      : `<i class="ti ti-chevron-right text-[13px] text-text3 shrink-0 opacity-0 group-hover:opacity-100 transition-opacity"></i>`;

    item.innerHTML = `
      <div class="w-8 h-8 rounded-full shrink-0 overflow-hidden flex items-center justify-center"
           style="background:${p.corAvatar}">${avatarHtml}</div>
      <div class="flex-1 min-w-0">
        <div class="text-[13px] font-semibold text-text1 truncate group-hover:text-purple2 transition-colors">${_escapeHtml(p.nome)}</div>
        <div class="text-[11px] text-text3 truncate">${_escapeHtml(p.subtitulo || '')}</div>
      </div>
      ${connBadge}`;
    container.appendChild(item);
  });
}

/* ── Aba Pedidos de conexão ──────────────────────────────── */

async function loadConnectionRequests() {
  const list      = document.getElementById('msg-requests-list');
  const loadingEl = document.getElementById('requests-loading');
  if (!list) return;
  loadingEl && loadingEl.classList.remove('hidden');

  try {
    const token = localStorage.getItem('firstech_access_token');
    const res = await fetch('/api/connections/pending-received', {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    });
    if (!res.ok) throw new Error();
    _requestsData = await res.json();
    _renderConnectionRequests(_requestsData);
    _updateRequestsBadge(_requestsData.length);
  } catch {
    list.innerHTML = '<div class="px-4 py-3 text-[12px] text-text3">Erro ao carregar pedidos.</div>';
  } finally {
    loadingEl && loadingEl.classList.add('hidden');
  }
}

function _renderConnectionRequests(requests) {
  const list = document.getElementById('msg-requests-list');
  if (!list) return;

  if (!requests.length) {
    list.innerHTML = `
      <div class="flex flex-col items-center justify-center py-12 gap-2 text-text3">
        <i class="ti ti-user-check text-[32px]"></i>
        <div class="text-[13px] font-medium">Nenhum pedido pendente</div>
        <div class="text-[11px] text-center px-4">Quando alguém quiser se conectar com você, aparecerá aqui.</div>
      </div>`;
    return;
  }

  list.innerHTML = '';
  requests.forEach(r => {
    const item = document.createElement('div');
    item.id = 'req-item-' + r.id;
    item.className = 'flex items-start gap-3 px-4 py-[12px] border-b border-border2 last:border-b-0';

    const avatarHtml = r.avatarBase64
      ? `<img src="${r.avatarBase64}" class="w-full h-full object-cover" alt=""/>`
      : `<span class="text-[12px] font-bold text-white">${_escapeHtml(r.inicial)}</span>`;

    item.innerHTML = `
      <div class="w-10 h-10 rounded-full shrink-0 overflow-hidden flex items-center justify-center cursor-pointer hover:opacity-80"
           style="background:${r.corAvatar}" onclick="openUserProfile(${r.id})">${avatarHtml}</div>
      <div class="flex-1 min-w-0">
        <div class="text-[13px] font-semibold text-text1 truncate cursor-pointer hover:text-purple2"
             onclick="openUserProfile(${r.id})">${_escapeHtml(r.nome)}</div>
        <div class="text-[11px] text-text3 truncate mb-[8px]">${_escapeHtml(r.headline || '')}</div>
        <div class="flex gap-2">
          <button class="flex-1 py-[5px] rounded-[7px] text-[11px] font-semibold font-[inherit] cursor-pointer border-none transition-all duration-150"
                  style="background:var(--purple);color:#fff"
                  onclick="acceptConnectionRequest(${r.id}, this)">
            Aceitar
          </button>
          <button class="flex-1 py-[5px] rounded-[7px] text-[11px] font-semibold font-[inherit] cursor-pointer border-none transition-all duration-150"
                  style="background:var(--bg3);color:var(--text3);border:1px solid var(--border2)"
                  onclick="rejectConnectionRequest(${r.id}, this)">
            Recusar
          </button>
        </div>
      </div>`;
    list.appendChild(item);
  });
}

async function acceptConnectionRequest(fromId, btn) {
  btn.disabled = true;
  btn.textContent = '...';
  try {
    const token = localStorage.getItem('firstech_access_token');
    const res = await fetch(`/api/connections/accept/${fromId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(token ? { 'Authorization': 'Bearer ' + token } : {}) }
    });
    if (!res.ok) throw new Error();
    _removeRequestItem(fromId);
    _connLoaded = false;      // força reload das conexões
    _allPeopleData = [];      // invalida cache de pessoas (badges mudam)
    showJobToast('Conexão aceita!');
  } catch {
    btn.disabled = false;
    btn.textContent = 'Aceitar';
    showJobToast('Erro ao aceitar pedido.', true);
  }
}

async function rejectConnectionRequest(fromId, btn) {
  btn.disabled = true;
  btn.textContent = '...';
  try {
    const token = localStorage.getItem('firstech_access_token');
    const res = await fetch(`/api/connections/${fromId}`, {
      method: 'DELETE',
      headers: { 'Content-Type': 'application/json', ...(token ? { 'Authorization': 'Bearer ' + token } : {}) }
    });
    if (!res.ok) throw new Error();
    _removeRequestItem(fromId);
    showJobToast('Pedido recusado.');
  } catch {
    btn.disabled = false;
    btn.textContent = 'Recusar';
    showJobToast('Erro ao recusar pedido.', true);
  }
}

function _removeRequestItem(fromId) {
  document.getElementById('req-item-' + fromId)?.remove();
  _requestsData = _requestsData.filter(r => r.id !== fromId);
  _updateRequestsBadge(_requestsData.length);
  if (!_requestsData.length) _renderConnectionRequests([]);
}

function _updateRequestsBadge(count) {
  const badge = document.getElementById('msg-requests-badge');
  if (!badge) return;
  if (count > 0) {
    badge.textContent = count > 9 ? '9+' : count;
    badge.classList.remove('hidden');
  } else {
    badge.classList.add('hidden');
  }
  const mobileDot = document.getElementById('mbn-requests-badge');
  if (mobileDot) mobileDot.classList.toggle('hidden', count === 0);
}

/** Abre uma conversa no painel direito. */
async function openConversation(userId, name) {
  _activeChatUserId = userId;
  _activeChatName   = name;

  document.getElementById('msg-empty-state').classList.add('hidden');
  const active = document.getElementById('msg-active');
  active.classList.remove('hidden');
  active.style.display = 'flex';

  // Mobile: mostrar painel de chat
  if (window.innerWidth < 768) {
    document.getElementById('s-messages')?.classList.add('msg-chat-active');
  }

  document.getElementById('msg-chat-name').textContent = name || '';
  document.getElementById('msg-chat-sub').textContent  = 'Conectado';

  // Avatar: tenta _convData primeiro, depois _connData
  const conv = _convData.find(c => c.userId == userId)
            || _connData.find(c => c.id == userId);
  const headerAvatar = document.getElementById('msg-chat-avatar');
  if (conv) {
    const bg      = conv.corAvatar || 'linear-gradient(135deg,#6d28d9,#8b5cf6)';
    const inicial = conv.inicial || (conv.nome ? conv.nome[0].toUpperCase() : '?');
    const photo   = conv.avatarBase64;
    headerAvatar.style.background = bg;
    headerAvatar.innerHTML = photo
      ? `<img src="${photo}" class="w-full h-full object-cover" alt=""/>`
      : `<span class="text-sm font-bold text-white">${_escapeHtml(inicial)}</span>`;
  }

  // Marca item ativo
  document.querySelectorAll('.conv-item').forEach(el => {
    el.classList.toggle('active', el.dataset.userId == userId);
  });

  await _loadMessages(userId);
  document.getElementById('msg-input')?.focus();

  clearInterval(_msgPollTimer);
  _msgPollTimer = setInterval(() => _loadMessages(userId, true), 5000);
}

async function _loadMessages(userId, silent = false) {
  try {
    const token = localStorage.getItem('firstech_access_token');
    const res = await fetch(`/api/messages/${userId}`, {
      headers: token ? { 'Authorization': 'Bearer ' + token } : {}
    });
    if (!res.ok) throw new Error();
    const messages = await res.json();
    _renderMessages(messages);
    if (!silent) loadConversations();
  } catch {
    if (!silent) console.error('Falha ao carregar mensagens.');
  }
}

function _renderMessages(messages) {
  const list = document.getElementById('msg-messages-list');
  if (!list) return;
  const isNearBottom = list.scrollHeight - list.scrollTop - list.clientHeight < 80;

  list.innerHTML = messages.map(m => {
    const align     = m.mine ? 'items-end' : 'items-start';
    const bubbleCls = m.mine ? 'msg-bubble-mine text-text1' : 'msg-bubble-other text-text2';
    const readIcon  = m.mine
      ? `<i class="ti ti-check${m.read ? 's text-purple2' : ' text-text3'} text-[10px]"></i>` : '';
    return `
      <div class="flex flex-col ${align} gap-[2px]">
        <div class="max-w-[75%] ${bubbleCls} px-[13px] py-[9px] text-[13px] leading-[1.65]">${_escapeHtml(m.content)}</div>
        <div class="flex items-center gap-1 text-[10px] text-text3 px-1">
          <span>${_escapeHtml(m.time)}</span>${readIcon}
        </div>
      </div>`;
  }).join('');

  if (isNearBottom || !messages.length) list.scrollTop = list.scrollHeight;
}

async function sendActiveMessage() {
  if (!_activeChatUserId) return;
  const input   = document.getElementById('msg-input');
  const content = input.value.trim();
  if (!content) return;

  input.value = '';
  input.style.height = 'auto';

  try {
    const token = localStorage.getItem('firstech_access_token');
    const res = await fetch(`/api/messages/${_activeChatUserId}`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json', ...(token ? { 'Authorization': 'Bearer ' + token } : {}) },
      body: JSON.stringify({ content })
    });
    if (!res.ok) throw new Error();
    await _loadMessages(_activeChatUserId);
    loadConversations();
  } catch {
    console.error('Falha ao enviar mensagem.');
  }
}

function handleMsgKey(e) {
  if (e.key === 'Enter' && !e.shiftKey) {
    e.preventDefault();
    sendActiveMessage();
  }
}

function _updateUnreadBadge(count) {
  const badge = document.getElementById('msg-unread-badge');
  if (!badge) return;
  if (count > 0) {
    badge.textContent = count > 99 ? '99+' : count;
    badge.classList.remove('hidden');
  } else {
    badge.classList.add('hidden');
  }
}

async function _pollUnreadCount() {
  try {
    const token = localStorage.getItem('firstech_access_token');
    if (!token) return;
    const [msgRes, reqRes] = await Promise.all([
      fetch('/api/messages/unread-count', { headers: { 'Authorization': 'Bearer ' + token } }),
      fetch('/api/connections/pending-received', { headers: { 'Authorization': 'Bearer ' + token } })
    ]);
    if (msgRes.ok) {
      const data = await msgRes.json();
      _updateUnreadBadge(data.count);
    }
    if (reqRes.ok) {
      const reqs = await reqRes.json();
      _updateRequestsBadge(reqs.length);
    }
  } catch {}
}

setInterval(_pollUnreadCount, 15000);
_pollUnreadCount();

/* ═══════════════════════════════════════════════════════════
   CONTADORES DE CARACTERES
   ═══════════════════════════════════════════════════════════ */

/**
 * Inicializa contadores de caracteres discretos em todos os campos
 * com o atributo [data-char-limit="N"].
 *
 * Para <textarea>: contador sobreposto no canto inferior-direito (estilo Twitter),
 *   com padding-bottom extra para o texto não ficar sob o contador.
 * Para <input>: contador abaixo do campo, dentro de um wrapper relativo.
 *
 * Deve ser chamado uma vez após o DOM estar pronto.
 */
function initCharCounters() {
  document.querySelectorAll('[data-char-limit]').forEach(el => {
    const max = parseInt(el.dataset.charLimit, 10);
    if (!max) return;

    const isTextarea = el.tagName === 'TEXTAREA';

    // Envolve o elemento num wrapper com position:relative
    const wrapper = document.createElement('div');
    wrapper.style.position = 'relative';
    if (!isTextarea) wrapper.style.paddingBottom = '16px';
    el.parentNode.insertBefore(wrapper, el);
    wrapper.appendChild(el);

    // Textarea precisa de padding-bottom para o texto não passar sob o contador
    if (isTextarea) el.style.paddingBottom = '22px';

    // Cria o span do contador
    const span = document.createElement('span');
    span.style.cssText =
      'position:absolute;font-size:10px;font-family:inherit;' +
      'pointer-events:none;user-select:none;transition:color .2s;line-height:1;' +
      (isTextarea ? 'bottom:7px;right:10px' : 'bottom:1px;right:2px');

    // Guarda referência no elemento para sync programático posterior
    el._charCounter    = span;
    el._charCounterMax = max;

    _updateCharCounter(span, el.value.length, max);
    wrapper.appendChild(span);

    el.addEventListener('input', () => _updateCharCounter(span, el.value.length, max));
  });
}

/**
 * Re-sincroniza todos os contadores (chamado após preenchimento programático
 * de campos com .value = '...', que não dispara o evento 'input').
 */
function syncCharCounters() {
  document.querySelectorAll('[data-char-limit]').forEach(el => {
    if (el._charCounter && el._charCounterMax) {
      _updateCharCounter(el._charCounter, el.value.length, el._charCounterMax);
    }
  });
}

function _updateCharCounter(span, len, max) {
  span.textContent = len + ' / ' + max;
  const ratio = len / max;
  if      (ratio >= 0.95) span.style.color = 'var(--red)';
  else if (ratio >= 0.80) span.style.color = 'var(--amber)';
  else                    span.style.color = 'var(--text3)';
}

// Inicializa ao carregar a página (script é carregado no final do <body>)
initCharCounters();

/* ── URL params: ?tab=xxx  |  ?openChat=id&chatName=nome ── */
(function () {
  const p = new URLSearchParams(window.location.search);
  const tab        = p.get('tab');
  const openChatId = p.get('openChat');
  const chatName   = p.get('chatName') || '';

  if (tab) {
    go(tab);
    if (tab === 'messages') loadConversations();
  }

  if (openChatId) {
    go('messages');
    loadConversations();
    // Aguarda o painel carregar antes de abrir a conversa
    setTimeout(() => {
      switchMsgTab('convs');
      openConversation(parseInt(openChatId, 10), decodeURIComponent(chatName));
    }, 600);
  }

  // Limpa os params da URL sem recarregar (deixa a URL limpa)
  if (tab || openChatId) {
    history.replaceState({}, '', window.location.pathname);
  }
})();
