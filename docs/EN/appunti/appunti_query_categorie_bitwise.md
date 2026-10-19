# COMPLETE GUIDE: BITMASK CATEGORY SYSTEM (SQL + EF CORE)

This system uses binary logic to manage multiple categories in one integer field, improving performance and removing the need for many-to-many join tables.

---

## 1. DATA AND DATABASE LOGIC

To map up to 32 independent categories, each category must correspond to one enabled bit. Therefore, IDs must be powers of two.

### Type definitions

* **Database:** use `INT UNSIGNED` (32 bits).
* **C# / Entity Framework:** use `uint`.
* **Capacity:** exactly 32 categories (from $2^0$ to $2^{31}$).

### `Categories` table

Contains the base definitions.

| ID (power of 2) | Binary value (simplified) | Category name |
| :--- | :--- | :--- |
| 1 | 00000001 | Food |
| 2 | 00000010 | Beverages |
| 4 | 00000100 | Appliances |
| 8 | 00001000 | Fruit |

### `Items` table

The `Category` column stores the mask (the sum of the bits). For example, a product that is both Food (1) and Fruit (8) has value **9** ($1 + 8$).

---

## 2. ENTITY FRAMEWORK CORE IMPLEMENTATION

### C# model

Use `uint` so that all 32 bits are handled correctly without a sign bit.

```csharp
public class Item {
    public int Id { get; set; }
    public string Name { get; set; }
    public uint Category { get; set; } // Our bitmask
}
```

### Creating a record (write)

When receiving a list of IDs from the client, use bitwise OR (`|`) to generate the mask.

```csharp
public async Task SaveItem(string name, List<uint> selectedIds) {
    uint mask = 0;
    foreach (var id in selectedIds) {
        mask |= id; // Enables the bit corresponding to this ID
    }

    var newItem = new Item { Name = name, Category = mask };
    _context.Items.Add(newItem);
    await _context.SaveChangesAsync();
}
```

---

## 3. FILTERING AND QUERIES (LINQ)

EF Core supports bitwise operators, making these queries efficient.

### OR filter (at least one category)

Find items that belong to at least one supplied category.

```csharp
uint searchMask = 0;
inputIds.ForEach(id => searchMask |= id);

var query = _context.Items.Where(i => (i.Category & searchMask) != 0);
```

### AND filter (all categories)

Find items that have every specified category.

```csharp
var query = _context.Items.Where(i => (i.Category & searchMask) == searchMask);
```

---

## 4. DECODING (READ)

To show category names for the number `9`:

```csharp
public async Task<List<string>> GetCategoryNames(uint itemMask) {
    // Retrieve base categories from the database
    var allCategories = await _context.Categories.ToListAsync();

    // Compare the item mask with each ID (a power of two)
    return allCategories
        .Where(c => (itemMask & c.Id) != 0)
        .Select(c => c.Name)
        .ToList();
}
```

---

## IMPORTANT NOTES AND LIMITS

1. **32-record limit:** an `INT` has 32 bits, so a 33rd category is not possible. If future growth is expected, use `BIGINT` (SQL) and `ulong` (C#) for 64 categories.
2. **Integrity constraints:** do not use a `FOREIGN KEY` on `Items.Category`. Since it contains combined values such as 9, the database would fail to find category ID 9.
3. **Benefit:** categories can be added to or removed from an object with a single mathematical operation, without deleting or inserting association rows.
