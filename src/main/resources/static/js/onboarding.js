/**
 * onboarding.js
 * ─────────────────────────────────────────────────────────────
 * Gerencia o fluxo multi-step de cadastro da plataforma Firstech.
 *
 * Expõe um único namespace global `Onboarding` para não conflitar
 * com o auth.js já existente (que usa tokenStorage, post(), etc.).
 *
 * Fluxo:
 *   Talent   → screen-role → t1 → t2 → t3 → t4 → POST /api/auth/register
 *   Recruiter → screen-role → r1 → r2 → r3 → r4 → POST /api/auth/register
 *
 * Todos os dados coletados são enviados em um único POST para
 * /api/auth/register e persistidos diretamente no banco de dados.
 * ─────────────────────────────────────────────────────────────
 */

const Onboarding = (() => {

  // ─── Estado ────────────────────────────────────────────────
  let role     = null;   // 'talent' | 'recruiter'
  let workModel = null;  // 'remote' | 'hybrid' | 'onsite'
  let tags      = [];    // tecnologias do talento

  // Dados coletados ao longo dos passos (persistidos no localStorage)
  const profile = {};

  // ─── Flows de progresso ────────────────────────────────────
  const TALENT_FLOW    = ['screen-t1','screen-t2','screen-t3','screen-t4'];
  const RECRUITER_FLOW = ['screen-r1','screen-r2','screen-r3','screen-r4'];

  const STEP_LABELS = {
    'screen-t1': 'Credenciais',      'screen-t2': 'Contatos & Links',
    'screen-t3': 'Perfil',           'screen-t4': 'Habilidades',
    'screen-r1': 'Credenciais',      'screen-r2': 'Dados da Empresa',
    'screen-r3': 'Cultura',          'screen-r4': 'Objetivos',
  };

  const TECH_SUGGESTIONS = [
    'Java','Python','JavaScript','TypeScript','React','Vue','Angular',
    'Spring Boot','Node.js','Django','FastAPI','.NET','Go','Kotlin',
    'Swift','Docker','Kubernetes','AWS','SQL','PostgreSQL','MongoDB',
    'Redis','Git','Linux','Flutter','Rust','PHP','Laravel','Next.js',
  ];

  const CULTURE_OPTIONS = [
    'Autonomia','Mentoria','Foco em Resultados','Diversidade',
    'Inovação','Trabalho em Equipe','Work-Life Balance',
    'Crescimento Rápido','Open Source','Transparência',
  ];

  const GOALS = [
    { id:'brand',  icon:'📢', title:'Divulgar a Empresa',
      desc:'Aumentar a visibilidade da marca empregadora' },
    { id:'talent', icon:'🌟', title:'Resgatar Jovens Talentos',
      desc:'Conectar com estagiários e desenvolvedores júnior' },
    { id:'jobs',   icon:'📋', title:'Postar Vagas',
      desc:'Publicar e gerenciar oportunidades de trabalho' },
  ];

  // ─── Navegação de telas ────────────────────────────────────
  function go(targetId) {
    document.querySelectorAll('.onb-screen').forEach(s => s.classList.remove('active'));
    const el = document.getElementById(targetId);
    if (!el) return;
    el.style.animation = 'none';
    requestAnimationFrame(() => { el.style.animation = ''; el.classList.add('active'); });
    updateProgress(targetId);
    document.querySelector('.scroll-wrap')?.scrollTo({ top: 0, behavior: 'smooth' });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  }

  function updateProgress(screenId) {
    const wrap  = document.getElementById('onbProgress');
    const fill  = document.getElementById('onbFill');
    const lbl   = document.getElementById('onbLabel');
    const cnt   = document.getElementById('onbCounter');
    const dots  = document.getElementById('onbDots');

    if (screenId === 'screen-role' || screenId === 'screen-success') {
      wrap.style.display = 'none'; return;
    }

    const flow = role === 'talent' ? TALENT_FLOW : RECRUITER_FLOW;
    const idx  = flow.indexOf(screenId);
    if (idx < 0) { wrap.style.display = 'none'; return; }

    wrap.style.display = 'block';
    const pct = Math.round(((idx + 1) / flow.length) * 100);
    fill.style.width = pct + '%';
    lbl.textContent  = STEP_LABELS[screenId] || '';
    cnt.textContent  = (idx + 1) + ' / ' + flow.length;

    dots.innerHTML = '';
    flow.forEach((_, i) => {
      const d = document.createElement('div');
      d.className = 'onb-dot' + (i === idx ? ' active' : i < idx ? ' done' : '');
      dots.appendChild(d);
    });
  }

  // ─── Escolha de papel ──────────────────────────────────────
  function chooseRole(r) {
    role = r;
    if (r === 'talent') {
      go('screen-t1');
    } else {
      _buildCultureGrid();
      _buildGoalCards();
      go('screen-r1');
    }
  }

  // ─── Utilitários de validação ──────────────────────────────
  function val(id)    { const e = document.getElementById(id); return e ? e.value.trim() : ''; }
  function isEmail(s) { return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(s); }
  function isPhone(s) { return s.replace(/\D/g,'').length >= 10; }

  function setErr(id, show) {
    const el = document.getElementById('err-' + id);
    if (el) el.classList.toggle('show', show);
    const inp = document.getElementById(id);
    if (inp) inp.classList.toggle('error', show);
  }
  function clearErrs(ids) { ids.forEach(id => setErr(id, false)); }

  // ─── Toggle password ───────────────────────────────────────
  function togglePw(inputId, btn) {
    const inp  = document.getElementById(inputId);
    const icon = btn.querySelector('.material-symbols-outlined');
    if (inp.type === 'password') { inp.type = 'text';     icon.textContent = 'visibility_off'; }
    else                         { inp.type = 'password'; icon.textContent = 'visibility'; }
  }

  // ─── Char counter ──────────────────────────────────────────
  function charCount(input, counterId, max) {
    const el = document.getElementById(counterId);
    if (!el) return;
    el.textContent = input.value.length + '/' + max;
    el.style.color = input.value.length >= max * .85
      ? '#f0c060' : 'var(--color-on-surface-variant)';
  }

  // ─── Work model radio ──────────────────────────────────────
  function pickWork(el, value) {
    document.querySelectorAll('#workModelGroup .onb-radio')
            .forEach(r => r.classList.remove('selected'));
    el.classList.add('selected');
    workModel = value;
    const cf = document.getElementById('cityField');
    if (value === 'hybrid' || value === 'onsite') cf.style.display = 'flex';
    else cf.style.display = 'none';
  }

  // ─── File upload ───────────────────────────────────────────
  function handleFile(input) {
    const zone = document.getElementById('uploadZone');
    if (input.files && input.files[0]) {
      const f = input.files[0];
      zone.classList.add('has-file');
      zone.querySelector('.onb-upload__text').textContent = '✓ ' + f.name;
      zone.querySelector('.onb-upload__sub').textContent =
        (f.size / 1024 / 1024).toFixed(2) + ' MB';
    }
  }

  // ─── Tags ──────────────────────────────────────────────────
  function handleTagKey(e) {
    const inp = e.target;
    if (e.key === 'Enter' || e.key === ',') {
      e.preventDefault();
      _addTag(inp.value.replace(',',''));
    } else if (e.key === 'Backspace' && !inp.value && tags.length) {
      _removeTag(tags[tags.length - 1]);
    }
  }

  function _addTag(raw) {
    const v = raw.trim();
    if (!v || tags.includes(v)) return;
    tags.push(v);
    _renderTags();
    _renderSuggestions('');
    _updateFinishBtn();
  }

  function _removeTag(v) {
    tags = tags.filter(t => t !== v);
    _renderTags();
    _renderSuggestions('');
    _updateFinishBtn();
  }

  function _renderTags() {
    const wrap = document.getElementById('tagsWrap');
    const inp  = document.getElementById('tagInput');
    wrap.innerHTML = '';
    tags.forEach(v => {
      const t = document.createElement('div');
      t.className = 'onb-tag';
      // safe: v is user input, escape it
      const safe = v.replace(/</g,'&lt;').replace(/>/g,'&gt;');
      t.innerHTML = `${safe} <button class="onb-tag-rm" type="button">✕</button>`;
      t.querySelector('.onb-tag-rm').onclick = () => _removeTag(v);
      wrap.appendChild(t);
    });
    wrap.appendChild(inp);
  }

  function filterSuggestions(query) {
    _renderSuggestions(query);
  }

  function _renderSuggestions(query) {
    const wrap = document.getElementById('suggestions');
    if (!wrap) return;
    wrap.innerHTML = '';
    const q = query.toLowerCase();
    const visible = TECH_SUGGESTIONS
      .filter(t => !tags.includes(t) && (!q || t.toLowerCase().includes(q)))
      .slice(0, 14);
    visible.forEach(t => {
      const chip = document.createElement('div');
      chip.className = 'onb-sug-chip';
      chip.textContent = t;
      chip.onclick = () => { _addTag(t); document.getElementById('tagInput').value = ''; };
      wrap.appendChild(chip);
    });
  }

  function _updateFinishBtn() {
    const btn  = document.getElementById('btnFinishTalent');
    const warn = document.getElementById('tagWarn');
    const ok   = tags.length >= 3;
    if (btn) btn.disabled = !ok;
    if (warn) warn.classList.toggle('show', tags.length > 0 && !ok);
  }

  // ─── Culture grid ──────────────────────────────────────────
  function _buildCultureGrid() {
    const grid = document.getElementById('cultureGrid');
    if (!grid || grid.children.length > 0) return;
    CULTURE_OPTIONS.forEach(label => {
      const t = document.createElement('div');
      t.className = 'onb-culture-tag';
      t.innerHTML = `<span class="material-symbols-outlined ck">check</span>${label}`;
      t.onclick = () => _toggleCulture(t);
      grid.appendChild(t);
    });
  }

  function _toggleCulture(el) {
    const selected = document.querySelectorAll('#cultureGrid .onb-culture-tag.selected');
    const warn = document.getElementById('cultureLimitWarn');
    if (!el.classList.contains('selected') && selected.length >= 3) {
      warn.style.display = 'block';
      setTimeout(() => { warn.style.display = 'none'; }, 2200);
      return;
    }
    el.classList.toggle('selected');
    warn.style.display = 'none';
  }

  // ─── Goal cards ────────────────────────────────────────────
  function _buildGoalCards() {
    const wrap = document.getElementById('goalCards');
    if (!wrap || wrap.children.length > 0) return;
    GOALS.forEach(g => {
      const c = document.createElement('div');
      c.className = 'onb-goal-card';
      c.dataset.id = g.id;
      c.innerHTML = `
        <div class="onb-goal-card__icon">${g.icon}</div>
        <div style="flex:1">
          <div class="onb-goal-card__title">${g.title}</div>
          <div class="onb-goal-card__desc">${g.desc}</div>
        </div>
        <div class="onb-goal-card__chk">
          <span class="material-symbols-outlined" style="font-size:13px">check</span>
        </div>`;
      c.onclick = () => c.classList.toggle('selected');
      wrap.appendChild(c);
    });
  }

  // ─── Toast ─────────────────────────────────────────────────
  function toast(type, title, msg, icon) {
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const t = document.createElement('div');
    t.className = `onb-toast onb-toast--${type}`;
    const safeMsg = (msg || '').replace(/</g,'&lt;');
    t.innerHTML = `
      <span class="material-symbols-outlined onb-toast__icon">${icon || 'info'}</span>
      <div>
        <div class="onb-toast__title">${title}</div>
        <div class="onb-toast__msg">${safeMsg}</div>
      </div>`;
    container.appendChild(t);
    setTimeout(() => {
      t.classList.add('removing');
      t.addEventListener('animationend', () => t.remove(), { once: true });
    }, 4500);
  }

  // ─── Loading state (reutiliza helper do auth.js) ───────────
  function setLoading(btnId, loading, loadingLabel = 'Aguarde…') {
    const btn = document.getElementById(btnId);
    if (!btn) return;
    btn.disabled = loading;
    const lbl  = btn.querySelector('span:first-child');
    const icon = btn.querySelector('.btn-icon');
    if (lbl)  btn.dataset._origLabel = btn.dataset._origLabel || lbl.textContent;
    if (lbl)  lbl.textContent = loading ? loadingLabel : (btn.dataset._origLabel || lbl.textContent);
    if (icon) icon.textContent = loading ? 'hourglass_empty' : (btn.dataset._origIcon || icon.textContent);
    if (!loading) delete btn.dataset._origLabel;
  }

  // ─── POST para o backend ───────────────────────────────────
  /**
   * Reutiliza a função `post` definida em auth.js (mesmo escopo global).
   * auth.js define: async function post(endpoint, body) { … }
   * Se não estiver disponível (ex: teste isolado), usa fetch diretamente.
   */
  async function apiPost(endpoint, body) {
    if (typeof post === 'function') {
      return post(endpoint, body);   // auth.js helper já trata erros e headers
    }
    const res = await fetch('/api/auth' + endpoint, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(body),
    });
    const data = await res.json().catch(() => ({}));
    if (!res.ok) {
      const err = new Error(data.message || 'Erro desconhecido.');
      err.status  = res.status;
      err.details = data.details;
      throw err;
    }
    return data;
  }

  /** Persiste tokens da mesma forma que auth.js */
  function saveTokens(data) {
    if (typeof tokenStorage !== 'undefined') {
      tokenStorage.set(data.accessToken, data.refreshToken);
    } else {
      localStorage.setItem('firstech_access_token',  data.accessToken);
      localStorage.setItem('firstech_refresh_token', data.refreshToken);
    }
  }


  // ══════════════════════════════════════════════════════════
  // TALENT STEPS
  // ══════════════════════════════════════════════════════════

  function nextT1() {
    const ids = ['t1-name','t1-email','t1-pass','t1-pass2'];
    clearErrs(ids);
    let ok = true;

    if (!val('t1-name'))              { setErr('t1-name', true);  ok = false; }
    if (!isEmail(val('t1-email')))    { setErr('t1-email', true); ok = false; }
    if (val('t1-pass').length < 8)   { setErr('t1-pass', true);  ok = false; }
    if (val('t1-pass') !== val('t1-pass2')) { setErr('t1-pass2', true); ok = false; }

    if (!ok) return;

    // Guarda credenciais no profile temporário
    profile.name     = val('t1-name');
    profile.email    = val('t1-email');
    profile.password = val('t1-pass');

    toast('info', 'Verifique seu e-mail',
          'Enviaremos um link de confirmação para ' + profile.email, 'mail');
    go('screen-t2');
  }

  function nextT2() {
    clearErrs(['t2-phone']);
    if (!isPhone(val('t2-phone'))) { setErr('t2-phone', true); return; }

    profile.phone    = val('t2-phone');
    profile.linkedin = val('t2-linkedin');
    profile.github   = val('t2-github');
    go('screen-t3');
  }

  function nextT3() {
    const ids = ['t3-career','t3-work','t3-city','t3-tagline'];
    clearErrs(ids);
    let ok = true;

    if (!val('t3-career'))    { setErr('t3-career', true); ok = false; }
    if (!workModel)           { setErr('t3-work',   true); ok = false; }
    if ((workModel === 'hybrid' || workModel === 'onsite') && !val('t3-city')) {
      setErr('t3-city', true); ok = false;
    }
    if (!val('t3-tagline'))   { setErr('t3-tagline', true); ok = false; }

    if (!ok) return;

    profile.careerMoment = val('t3-career');
    profile.workModel    = workModel;
    profile.city         = val('t3-city');
    profile.tagline      = val('t3-tagline');

    _renderSuggestions('');   // garante sugestões ao entrar no step 4
    go('screen-t4');
  }

  async function finishTalent() {
    if (tags.length < 3) {
      document.getElementById('tagWarn').classList.add('show');
      return;
    }

    profile.technologies = [...tags];
    profile.role         = 'CANDIDATO';

    const btnId = 'btnFinishTalent';
    setLoading(btnId, true);

    try {
      const data = await apiPost('/register', {
        name:         profile.name,
        email:        profile.email,
        password:     profile.password,
        roleType:     'CANDIDATO',
        phone:        profile.phone,
        linkedin:     profile.linkedin,
        github:       profile.github,
        careerMoment: profile.careerMoment,
        workModel:    profile.workModel,
        city:         profile.city,
        tagline:      profile.tagline,
        technologies: profile.technologies,
      });

      saveTokens(data);

      toast('success', 'Cadastro realizado!', 'Bem-vindo ao Firstech! 🚀', 'celebration');
      go('screen-success');
      document.getElementById('successTitle').textContent = 'Bem-vindo, talento! 🚀';
      document.getElementById('successSub').innerHTML =
        'Seu perfil foi criado com sucesso.<br/>Confirme seu e-mail para ativar a conta.';

    } catch (err) {
      const msg = err.status === 409
        ? 'Este e-mail já está em uso. Tente outro ou faça login.'
        : (err.message || 'Erro ao criar conta. Tente novamente.');
      toast('error', 'Erro no cadastro', msg, 'error');
    } finally {
      setLoading(btnId, false);
    }
  }

  // ══════════════════════════════════════════════════════════
  // RECRUITER STEPS
  // ══════════════════════════════════════════════════════════

  function nextR1() {
    const ids = ['r1-name','r1-email','r1-pass','r1-pass2'];
    clearErrs(ids);
    let ok = true;

    if (!val('r1-name'))              { setErr('r1-name', true);  ok = false; }
    if (!isEmail(val('r1-email')))    { setErr('r1-email', true); ok = false; }
    if (val('r1-pass').length < 8)   { setErr('r1-pass', true);  ok = false; }
    if (val('r1-pass') !== val('r1-pass2')) { setErr('r1-pass2', true); ok = false; }

    if (!ok) return;

    profile.name     = val('r1-name');
    profile.email    = val('r1-email');
    profile.password = val('r1-pass');

    toast('info', 'Verifique seu e-mail',
          'Enviaremos um link de confirmação para ' + profile.email, 'mail');
    go('screen-r2');
  }

  function nextR2() {
    const ids = ['r2-company','r2-size','r2-phone'];
    clearErrs(ids);
    let ok = true;

    if (!val('r2-company'))         { setErr('r2-company', true); ok = false; }
    if (!val('r2-size'))            { setErr('r2-size', true);    ok = false; }
    if (!isPhone(val('r2-phone')))  { setErr('r2-phone', true);   ok = false; }

    if (!ok) return;

    profile.company = val('r2-company');
    profile.size    = val('r2-size');
    profile.phone   = val('r2-phone');
    profile.website = val('r2-site');
    go('screen-r3');
  }

  function nextR3() {
    clearErrs(['r3-culture']);
    const selected = document.querySelectorAll('#cultureGrid .onb-culture-tag.selected');
    if (selected.length === 0) { setErr('r3-culture', true); return; }

    profile.cultureTags = [...selected].map(el => el.textContent.trim());
    go('screen-r4');
  }

  async function finishRecruiter() {
    clearErrs(['r4-goals']);
    const goals = document.querySelectorAll('#goalCards .onb-goal-card.selected');
    if (goals.length === 0) { setErr('r4-goals', true); return; }

    profile.goals = [...goals].map(c => c.dataset.id);
    profile.role  = 'RECRUTADOR';

    const btnId = 'btnFinishRecruiter';
    setLoading(btnId, true);

    try {
      const data = await apiPost('/register', {
        name:        profile.name,
        email:       profile.email,
        password:    profile.password,
        roleType:    'RECRUTADOR',
        phone:       profile.phone,
        company:     profile.company,
        companySize: profile.size,
        website:     profile.website,
        cultureTags: profile.cultureTags,
        goals:       profile.goals,
      });

      saveTokens(data);

      toast('success', 'Conta criada!', 'Seu perfil de recrutador está pronto! 🎉', 'celebration');
      go('screen-success');
      document.getElementById('successTitle').textContent = 'Conta criada! 🎉';
      document.getElementById('successSub').innerHTML =
        'Seu perfil de recrutador foi configurado.<br/>Verifique seu e-mail para ativar e começar.';

    } catch (err) {
      const msg = err.status === 409
        ? 'Este e-mail já está em uso. Tente outro ou faça login.'
        : (err.message || 'Erro ao criar conta. Tente novamente.');
      toast('error', 'Erro no cadastro', msg, 'error');
    } finally {
      setLoading(btnId, false);
    }
  }

  // ─── Init ──────────────────────────────────────────────────
  function init() {
    _renderSuggestions('');
  }

  document.addEventListener('DOMContentLoaded', init);

  // ─── API pública ───────────────────────────────────────────
  return {
    go, chooseRole,
    togglePw, charCount, pickWork, handleFile,
    handleTagKey, filterSuggestions,
    nextT1, nextT2, nextT3, finishTalent,
    nextR1, nextR2, nextR3, finishRecruiter,
  };

})();
