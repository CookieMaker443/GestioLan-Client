Themes are managed through `ThemeManager`.

`ThemeManager` uses `AppPaths` and, through its themes directory, manages:

- adding new themes;
- validating new themes through a manifest;
- removing themes, including from the application;
- downloading a base theme as a template.

## Theme structure

A theme is a folder, for example `frutiger_aero`, containing:

```bash
~/.config/gestiolan/themes/frutiger_aero/
├── theme.json          ← manifest: marks the folder as a valid theme
├── theme.css           ← rules that override the base theme
├── dashboard_bg.png
├── login_bg.png
└── preview.png         ← optional settings-preview image
```

`theme.json` acts as the manifest:

```json
{
  "name": "Frutiger Aero",
  "author": "Cookie",
  "version": "1.0",
  "description": "Glass, bubbles, and blue sky",
  "css": "theme.css"
}
```
