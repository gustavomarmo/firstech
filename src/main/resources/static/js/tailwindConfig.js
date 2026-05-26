tailwind.config = {
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        // ── Main app (firstech) ──────────────────────────
        bg:       '#0d1117',
        bg2:      '#151b24',
        bg3:      '#1c2535',
        bg4:      '#222d3f',
        purple:   '#8b5cf6',
        purple2:  '#a78bfa',
        purple3:  '#6d28d9',
        purplebg: 'rgba(139,92,246,0.12)',
        border:   'rgba(139,92,246,0.22)',
        border2:  'rgba(255,255,255,0.07)',
        text1:    '#f0f4ff',
        text2:    '#94a3b8',
        text3:    '#64748b',
        green:    '#34d399',
        red:      '#f87171',
        amber:    '#fbbf24',
        // ── Auth ────────────────────────────────────────
        'surface-dim':    '#081425',
        'surface-low':    '#040e1f',
        'outline-var':    '#464554',
        outline:          '#908fa0',
        'on-surface-var': '#c7c4d7',
        primary:          '#8a60c2',
        'primary-fixed':  '#eaddff',
        tertiary:         '#7bd0ff',
        error:            '#ffb4ab',
      },
      fontFamily: {
        sans:    ['DM Sans', 'sans-serif'],
        display: ['Space Grotesk', 'sans-serif'],
        base:    ['Plus Jakarta Sans', 'sans-serif'],
      },
      keyframes: {
        modalIn: {
          from: { opacity: '0', transform: 'scale(.94) translateY(12px)' },
          to:   { opacity: '1', transform: 'scale(1) translateY(0)' },
        },
        successPop: {
          from: { opacity: '0', transform: 'scale(0)' },
          to:   { opacity: '1', transform: 'scale(1)' },
        },
      },
      animation: {
        'modal-in':    'modalIn .22s cubic-bezier(.34,1.26,.64,1)',
        'success-pop': 'successPop .5s cubic-bezier(.34,1.56,.64,1)',
      },
    }
  }
}
