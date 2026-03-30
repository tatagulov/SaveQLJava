# SafeQL v0.1 — Language Specification

> Root-oriented, structurally safe DSL over SQL

---

## 0. Goals and Scope

SafeQL is a read-only query DSL designed to remove common SQL error classes:

- implicit duplicates from JOINs
- manual GROUP BY / HAVING
- ambiguous query semantics

SafeQL makes relationships explicit and queries structurally verifiable.

### In Scope (v0.1)

- SELECT queries
- JOIN (many -> one)
- LEFT JOIN (many -> zero/one)
- WHERE
- ORDER BY / LIMIT
- Aggregation via `AggView` + JOIN

### Out of Scope (v0.1)

- WALK / recursive traversal
- NEST
- WHERE EXISTS as standalone section
- INSERT / UPDATE / DELETE
- UNION / UNION ALL
- Window functions
- Arbitrary nested SELECTs

---

## 1. Root-Oriented Model

Every SafeQL query has a single **root table** defined by `FROM`.

The compiler validates that JOIN targets are key-safe (PK-based in current API), so join semantics remain deterministic.

---

## 2. Section Order (Pipeline)

Strict order:

1. `FROM <table> <alias?>`
2. `{ JOIN ... | LEFT JOIN ... }*`
3. `SELECT ...`
4. `[WHERE ...]`
5. `[ORDER BY ...]`
6. `[LIMIT ... [OFFSET ...]]`

Rules:

- `JOIN` and `LEFT JOIN` may appear multiple times.
- `WHERE` appears after `SELECT`.

---

## 3. Identifiers and Aliases

- Table aliases are optional, but recommended for readability.
- Column references use `<qualifier>.<column>`.
- Expression aliases use `AS <name>`.

---

## 4. FROM

### Syntax

```
FROM <table_name> [alias]
```

Defines the root table.

---

## 5. JOIN / LEFT JOIN

JOIN does not introduce uncontrolled multiplicity in this DSL model; joins are key-based.

### Syntax (PK join)

```
JOIN <table> <alias?> BY <left_alias>.<fk_column>
LEFT JOIN <table> <alias?> BY <left_alias>.<fk_column>
```

FK must reference PK of right table.

---

## 6. Aggregation via AggView + JOIN

Instead of `NEST`, SafeQL uses aggregated views joined by keys.

Conceptually:

1. define aggregate projections in `AggView`
2. join view to root by PK/FK-compatible keys
3. select/filter by aggregated columns

This keeps result cardinality explicit and predictable.

---

## 7. SELECT

Standard projection:

```
SELECT
  <expr> [AS <alias>],
  ...
```

### Automatic Grouping

SafeQL has no explicit `GROUP BY` or `HAVING` in DSL surface.

If aggregates appear:

- compiler groups by all non-aggregate SELECT expressions
- aggregate predicates in `WHERE` are compiled to SQL `HAVING`

---

## 8. WHERE

Appears after SELECT.

Compiler separates predicates into SQL `WHERE` vs `HAVING` when needed.

---

## 9. ORDER BY / LIMIT

Unchanged from SQL:

```
ORDER BY ...
LIMIT n [OFFSET m]
```

Final pipeline stage.

---

## 10. Error Conditions

Compiler must fail on:

1. Invalid section order
2. Unknown aliases or columns
3. Invalid/duplicate table qualifiers in one query
4. Invalid key mapping in JOIN

---

## 11. Reference Compilation (PostgreSQL)

SafeQL maps to standard SQL:

- JOIN / LEFT JOIN -> SQL JOIN / LEFT JOIN
- Aggregation via AggView -> derived table with GROUP BY + JOIN
- WHERE with aggregates -> WHERE/HAVING split

No database extensions required.

---

## Appendix A — Canonical Example

```sql
FROM users u

LEFT JOIN orders o BY u.id
JOIN order_stats os BY u.id

SELECT
  u.id,
  u.name,
  os.totalPrice AS totalPrice

WHERE u.name ILIKE 'A%'
  AND os.totalPrice > 1000

ORDER BY os.totalPrice DESC
LIMIT 20
```
