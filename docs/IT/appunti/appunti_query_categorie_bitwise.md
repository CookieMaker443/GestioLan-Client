# GUIDA INTEGRALE: SISTEMA CATEGORIE CON BITMASK (SQL + EF CORE)

Questo sistema utilizza la logica binaria per gestire categorie multiple in un unico campo intero, ottimizzando le prestazioni ed eliminando la necessità di tabelle di giunzione (many-to-many).

---

## 1. LOGICA DEI DATI E DATABASE

Per mappare fino a 32 categorie indipendenti, ogni categoria deve corrispondere a un singolo bit "acceso". Questo significa che gli ID devono essere potenze di 2.

### Definizione dei tipi
* **Database**: Usa `INT UNSIGNED` (32 bit).
* **C# / Entity Framework**: Usa `uint`.
* **Capacità**: Esattamente 32 categorie (da $2^0$ a $2^{31}$).

### Tabella `Categorie`
Contiene le definizioni base.

| ID (Potenza di 2) | Valore Binario (Semplificato) | Nome Categoria |
| :--- | :--- | :--- |
| 1 | 00000001 | Cibo |
| 2 | 00000010 | Bevande |
| 4 | 00000100 | Elettrodomestici |
| 8 | 00001000 | Frutta |

### Tabella `Items`
La colonna `Categoria` conterrà la "maschera" (la somma dei bit).
* *Esempio*: Un prodotto che è sia Cibo (1) che Frutta (8) avrà il valore **9** ($1 + 8$).



---

## 2. IMPLEMENTAZIONE IN ENTITY FRAMEWORK CORE

### Il Modello C#
Assicurati di usare `uint` per gestire correttamente tutti i 32 bit senza il bit di segno.

```csharp
public class Item {
    public int Id { get; set; }
    public string Nome { get; set; }
    public uint Categoria { get; set; } // Questa è la nostra Bitmask
}
```

### Creazione di un Record (Write)
Ricevendo una lista di ID dal client, si usa l'operatore bitwise OR (`|`) per generare la maschera.

```csharp
public async Task SaveItem(string nome, List<uint> selectedIds) {
    uint mask = 0;
    foreach (var id in selectedIds) {
        mask |= id; // Accende il bit corrispondente all'ID
    }

    var newItem = new Item { Nome = nome, Categoria = mask };
    _context.Items.Add(newItem);
    await _context.SaveChangesAsync();
}
```

---

## 3. FILTRAGGIO E QUERY (LINQ)

Grazie al supporto di EF Core per gli operatori bitwise, le query sono estremamente efficienti.

### Filtro "OR" (Almeno una categoria)
Trova gli item che appartengono ad almeno una delle categorie fornite.

```csharp
uint searchMask = 0;
inputIds.ForEach(id => searchMask |= id);

var query = _context.Items.Where(i => (i.Categoria & searchMask) != 0);
```

### Filtro "AND" (Tutte le categorie)
Trova gli item che possiedono contemporaneamente tutte le categorie specificate.

```csharp
var query = _context.Items.Where(i => (i.Categoria & searchMask) == searchMask);
```

---

## 4. DECODIFICA (READ)

Per mostrare all'utente i nomi delle categorie partendo dal numero `9`:

```csharp
public async Task<List<string>> GetCategoryNames(uint itemMask) {
    // Recuperiamo le categorie base dal DB
    var allCategories = await _context.Categorie.ToListAsync();
    
    // Confrontiamo la maschera dell'item con ogni ID (potenza di 2)
    return allCategories
        .Where(c => (itemMask & c.Id) != 0)
        .Select(c => c.Nome)
        .ToList();
}
```

---

## NOTE IMPORTANTI E LIMITI

1.  **Limite dei 32 record**: Essendo un `INT` a 32 bit, non puoi avere una 33esima categoria. Se prevedi una crescita futura, usa `BIGINT` (SQL) e `ulong` (C#) per arrivare a 64 categorie.
2.  **Vincoli di Integrità**: Non usare `FOREIGN KEY` sulla colonna `Items.Categoria`. Poiché contiene valori combinati (come 9), il database darebbe errore non trovando l'ID 9 nella tabella categorie.
3.  **Vantaggio**: Questa struttura permette di aggiungere o rimuovere categorie a un oggetto con una singola operazione matematica, senza cancellare o inserire righe in tabelle di associazione.

---