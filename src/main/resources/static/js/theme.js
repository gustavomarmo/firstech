/**
 * theme.js — Firstech
 * Carregado no <head> de todas as páginas.
 * IIFE aplica o tema salvo antes da primeira renderização (evita FOUC).
 */
(function () {
  const saved = localStorage.getItem('firstech_theme') || 'dark';
  document.documentElement.setAttribute('data-theme', saved);
})();

function _themeUpdateUI(t) {
  const icon  = document.getElementById('theme-icon');
  const label = document.getElementById('theme-label');
  if (icon)  icon.className  = t === 'light'
    ? 'ti ti-moon text-[16px] text-text3'
    : 'ti ti-sun  text-[16px] text-text3';
  if (label) label.textContent = t === 'light' ? 'Tema escuro' : 'Tema claro';
}

function toggleTheme() {
  const cur = document.documentElement.getAttribute('data-theme') || 'dark';
  const nxt = cur === 'dark' ? 'light' : 'dark';
  document.documentElement.setAttribute('data-theme', nxt);
  localStorage.setItem('firstech_theme', nxt);
  _themeUpdateUI(nxt);
}

/* Atualiza ícone assim que o DOM estiver pronto */
document.addEventListener('DOMContentLoaded', function () {
  _themeUpdateUI(document.documentElement.getAttribute('data-theme') || 'dark');
});
