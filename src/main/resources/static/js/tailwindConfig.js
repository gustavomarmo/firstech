tailwind.config = {
  theme: {
    extend: {
      colors: {
        /* ── App principal (firstech) — via CSS custom properties ── */
        bg:       'var(--bg)',
        bg2:      'var(--bg2)',
        bg3:      'var(--bg3)',
        bg4:      'var(--bg4)',
        purple:   'var(--purple)',
        purple2:  'var(--purple2)',
        purple3:  'var(--purple3)',
        purplebg: 'var(--purplebg)',
        border:   'var(--border)',
        border2:  'var(--border2)',
        text1:    'var(--text1)',
        text2:    'var(--text2)',
        text3:    'var(--text3)',
        green:    'var(--green)',
        red:      'var(--red)',
        amber:    'var(--amber)',
        /* ── Auth (hardcoded — não mudam com o tema) ── */
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
