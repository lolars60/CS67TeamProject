---
name: Editorial Precision
colors:
  surface: '#fcf8f8'
  surface-dim: '#dcd9d9'
  surface-bright: '#fcf8f8'
  surface-container-lowest: '#ffffff'
  surface-container-low: '#f6f3f2'
  surface-container: '#f0edec'
  surface-container-high: '#ebe7e7'
  surface-container-highest: '#e5e2e1'
  on-surface: '#1c1b1b'
  on-surface-variant: '#434654'
  inverse-surface: '#313030'
  inverse-on-surface: '#f3f0ef'
  outline: '#747686'
  outline-variant: '#c3c5d6'
  surface-tint: '#1f54d2'
  primary: '#0038a2'
  on-primary: '#ffffff'
  primary-container: '#154ecd'
  on-primary-container: '#c3cfff'
  inverse-primary: '#b5c4ff'
  secondary: '#1e5bba'
  on-secondary: '#ffffff'
  secondary-container: '#6b9cff'
  on-secondary-container: '#003275'
  tertiary: '#792500'
  on-tertiary: '#ffffff'
  tertiary-container: '#a13400'
  on-tertiary-container: '#ffc3af'
  error: '#ba1a1a'
  on-error: '#ffffff'
  error-container: '#ffdad6'
  on-error-container: '#93000a'
  primary-fixed: '#dce1ff'
  primary-fixed-dim: '#b5c4ff'
  on-primary-fixed: '#00164d'
  on-primary-fixed-variant: '#003cad'
  secondary-fixed: '#d8e2ff'
  secondary-fixed-dim: '#aec6ff'
  on-secondary-fixed: '#001a43'
  on-secondary-fixed-variant: '#004397'
  tertiary-fixed: '#ffdbcf'
  tertiary-fixed-dim: '#ffb59b'
  on-tertiary-fixed: '#380d00'
  on-tertiary-fixed-variant: '#812800'
  background: '#fcf8f8'
  on-background: '#1c1b1b'
  surface-variant: '#e5e2e1'
typography:
  display-hero:
    fontFamily: Hanken Grotesk
    fontSize: 80px
    fontWeight: '600'
    lineHeight: 84px
    letterSpacing: -0.035em
  display-hero-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 44px
    fontWeight: '600'
    lineHeight: 48px
    letterSpacing: -0.025em
  headline-lg:
    fontFamily: Hanken Grotesk
    fontSize: 48px
    fontWeight: '600'
    lineHeight: 52px
    letterSpacing: -0.025em
  headline-lg-mobile:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '600'
    lineHeight: 36px
    letterSpacing: -0.02em
  headline-md:
    fontFamily: Hanken Grotesk
    fontSize: 32px
    fontWeight: '500'
    lineHeight: 38px
    letterSpacing: -0.02em
  headline-sm:
    fontFamily: Hanken Grotesk
    fontSize: 22px
    fontWeight: '500'
    lineHeight: 28px
    letterSpacing: -0.015em
  body-lg:
    fontFamily: Hanken Grotesk
    fontSize: 17px
    fontWeight: '400'
    lineHeight: 26px
    letterSpacing: -0.01em
  body-md:
    fontFamily: Hanken Grotesk
    fontSize: 15px
    fontWeight: '400'
    lineHeight: 22px
    letterSpacing: -0.005em
  body-sm:
    fontFamily: Hanken Grotesk
    fontSize: 13px
    fontWeight: '400'
    lineHeight: 18px
    letterSpacing: 0em
  label-md:
    fontFamily: Geist
    fontSize: 12px
    fontWeight: '500'
    lineHeight: 16px
    letterSpacing: 0.04em
  label-sm:
    fontFamily: Geist
    fontSize: 10px
    fontWeight: '600'
    lineHeight: 14px
    letterSpacing: 0.06em
rounded:
  sm: 0.125rem
  DEFAULT: 0.25rem
  md: 0.375rem
  lg: 0.5rem
  xl: 0.75rem
  full: 9999px
spacing:
  gutter: 1.5rem
  gutter-mobile: 0.75rem
  margin: 2.5rem
  margin-mobile: 1.25rem
  space-xs: 0.25rem
  space-sm: 0.5rem
  space-md: 1rem
  space-lg: 1.75rem
  space-xl: 3rem
---

## Brand & Style

This design system embodies high-editorial tech minimalism, pairing rigorous engineering discipline with refined typographic authority. It is designed for mission-critical infrastructure, AI safety, and advanced developer platforms that demand trust, precision, and zero visual clutter.

The aesthetic philosophy draws heavily on Swiss International Typographic Style fused with modern digital clarity:
- **High-Contrast Editorial Precision:** Generous negative space balances authoritative, large-scale sans-serif headlines.
- **Architectural Structure:** Subtle hairlines, fine grid divisions, and registration tick marks create a structured canvas inspired by technical blueprints and editorial publications.
- **Deliberate Accentuation:** Vibrant royal blue highlights puncture neutral ivory surfaces with intent, directing attention exclusively to focal messaging and key actions.

## Colors

The palette establishes an ultra-clean, clinical backdrop with deliberate bursts of electric royal blue.

- **Canvas & Backgrounds:** The base canvas is anchored by Ivory White (`#FEFEFD`), avoiding harsh digital white in favor of a warm, paper-like editorial clarity. Secondary containers utilize subtle milky off-whites (`#F7F7F5` and `#EFEFEA`).
- **Typography & Structure:** Coal Black (`#080808`) provides maximum contrast for headlines and primary icons. Body copy, metadata, and structural dividers utilize disciplined neutral tones: Charcoal (`#2D2E30`), Slate Gray (`#65676B`), and Hairline Border Gray (`#E4E4E0`).
- **Accents:** Sharp Royal Blue (`#154ECD`) delivers authoritative energy for primary CTAs and critical text callouts, complemented by Cerulean Sky (`#5B8DEF`) for gradient transitions and interactive focus states.
- **Rules of Use:** Never tint structural borders with saturated hues. Rely on monochromatic structure and allow Royal Blue to act exclusively as an intentional beacon.

## Typography

The type system pairs **Hanken Grotesk** for editorial presence and narrative authority with **Geist** for technical precision and metadata:

- **Hanken Grotesk:** Crisp, geometric, and uncompromising. Tight letter spacing (`-0.035em` on large scales) combined with compact line-height delivers the hallmark European poster/editorial visual tension.
- **Geist:** Employed across technical labels, navigation, badges, and inline data indicators. Slightly increased tracking adds mechanical legibility.
- **Accent Highlighting:** Headlines frequently integrate two-tone color treatment where specific semantic nouns or phrases are emphasized in `primary` Royal Blue while surrounding words remain in `neutral` Coal Black.

## Layout & Spacing

The layout model is anchored by a structured 12-column grid bound within framed borders and subtle technical registration indicators (`+` markers at grid intersections).

- **Grid Architecture:** 
  - **Desktop (1024px+):** 12-column grid with a maximum content container width of 1280px, 24px gutters, and 40px outer canvas margins. Side margins feature persistent vertical rules that frame the document canvas.
  - **Tablet (768px - 1023px):** 8-column layout, 20px gutters, and 32px canvas margins.
  - **Mobile (<768px):** 4-column layout, 12px gutters, and 20px canvas margins.
- **Vertical Rhythm:** Generous vertical sections separated by clean 1px hairline rules (`#E4E4E0`). Hero banners provide ample breathing room around headlines to preserve editorial hierarchy.

## Elevation & Depth

This system avoids drop shadows, fuzzy blurs, and heavy tonal skeuomorphism, achieving hierarchy purely through architectural layering and hairline boundaries:

- **Zero-Shadow Philosophy:** Elements sit flush within the canvas. Depth is conveyed strictly via contrast tiers: `#FEFEFD` (Base Canvas), `#F7F7F5` (Elevated Card), and `#EFEFEA` (Hover/Active Surface).
- **Hairline Framing:** Cards, dropdowns, and grouped blocks are bounded by crisp `1px` borders in `#E4E4E0`.
- **Overlays & Modals:** Modals use backdrop filters with subtle opacity (`rgba(254, 254, 253, 0.85)` with `8px` blur) edged by sharp 1px borders, preserving clinical lightness without dark heavy shrouds.

## Shapes

The geometric identity relies on soft technical precision:

- **Corner Radii:** Buttons, input fields, tags, and small utility elements feature a minimal radius of `2px` to `4px` (`roundedness: 1`), softening sharp digital corners while sustaining architectural rigidity.
- **Structural Cards:** Enclosing containers, panels, and data surfaces use either completely square corners (`0px`) or subtle `4px` radii to align with framing hairlines.
- **Pill Exceptions:** Rounded pills are reserved strictly for circular status pips and toggle switches.

## Components

### Buttons
- **Primary Button:** Solid Royal Blue (`#154ECD`) background, white text (`#FFFFFF`), `4px` radius. Padding: `8px 16px` for standard, `6px 12px` for compact. Hover state transitions to `#113FA8` with zero elevation change.
- **Secondary Button:** Hairline border (`1px solid #080808`), transparent background, Coal Black text. Hover switches to `#080808` background with `#FEFEFD` text.
- **Ghost/Text Button:** Transparent background, Coal Black or Royal Blue text, paired with a rightward directional arrow (`→`) offset by `4px`.

### Badges & Chips
- Ultra-slim height (20px to 24px), `2px` corner radius, `1px` border in `#E4E4E0`, background `#F7F7F5`.
- Text styled in `label-sm` (Geist uppercase, tracking `0.06em`).

### Form Controls & Inputs
- **Text Inputs:** Height 38px, hairline border (`1px solid #D6D6D1`), background `#FFFFFF`, text `#080808`. Active/Focus state switches border to `1px solid #154ECD` with zero fuzzy glow rings.
- **Checkboxes & Radios:** Square `16x16px` checks with `2px` corners; radio is `16x16px` circle. Border `1px solid #080808`. Checked fill is Royal Blue (`#154ECD`) with crisp white interior marks.

### Cards & Grid Containers
- Flat white or off-white background (`#FEFEFD` / `#F7F7F5`), bounded by a `1px` border (`#E4E4E0`).
- Often embellished with subtle corner registration crosshairs (`+`) at panel vertices to evoke precision blueprint drafting.

### Navigation Bar
- Fixed top bar, height 64px, anchored by a bottom `1px` border (`#E4E4E0`) with `rgba(254, 254, 253, 0.9)` background blur.
- Minimalist typographic links in Geist medium with uppercase demonstration CTA button at top right.