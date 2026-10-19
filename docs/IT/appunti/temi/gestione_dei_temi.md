i temi vengono gestiti tramite il `ThemeManager`

il `ThemeManager` usa `AppPaths`, grazie ad esso usa la cartella dei temi, gestisce 
- l'inserimento dei temi nupvi
- la validazione dei temi nuovi (tramite un manifest)
- l'eliminazione dei temi (anche dal software)
- il download di un tema base (come template)


come è strutturato un tema?
Abbiamo una cartella (es: `Fruitiger Aero) ed essa conterrà:
```bash
~/.config/gestiolan/themes/frutiger_aero/
├── theme.json          ← manifest: segna la cartella come tema valido
├── theme.css           ← regole che si sovrappongono al tema base
├── dashboard_bg.png
├── login_bg.png
└── preview.png         ← opzionale, per l'anteprima nelle impostazioni
```

dove il theme.json è il file che funge da manifest
```json
{
  "name": "Frutiger Aero",
  "author": "Cookie",
  "version": "1.0",
  "description": "Vetro, bolle e cielo azzurro",
  "css": "theme.css"
}
```
