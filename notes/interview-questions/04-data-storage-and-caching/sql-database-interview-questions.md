# Database & SQL Interview Questions (10-Year Java Lead)

Queries are standard SQL, PostgreSQL-flavoured, with notes where MySQL, SQL Server, or Oracle differ. Test the queries on your own data before relying on them.

## Table of Contents

- [Sample Tables Used in the Query Practice](#sample-tables-used-in-the-query-practice)
- [1. Fundamentals](#1-fundamentals)
- [2. Indexing and Query Optimisation](#2-indexing-and-query-optimisation)
- [3. Transactions, Isolation and Locking](#3-transactions-isolation-and-locking)
- [4. Design and Scaling](#4-design-and-scaling)
- [5. SQL Query Practice (with Solutions)](#5-sql-query-practice-with-solutions)
- [6. Database Scenario Questions](#6-database-scenario-questions)

---

## Sample Tables Used in the Query Practice

```sql
employees(id, name, salary, dept_id, manager_id)
departments(id, name)
customers(id, name, email)
orders(id, customer_id, total, status, created_at)
order_items(id, order_id, product_id, qty, price)
products(id, name, category, price)
accounts(id, balance, version)
```

## 1. Fundamentals

1. Explain ACID. Which property does each mechanism (WAL, locks, MVCC) support?
2. SQL vs NoSQL: how do you choose for a given workload?
3. What are CAP and PACELC? How do real databases position themselves?
4. Normalisation (1NF to BCNF) vs denormalisation: when do you deliberately denormalise?
5. Primary key vs unique key vs foreign key. Natural vs surrogate keys. UUID vs auto-increment trade-offs.
6. Explain INNER, LEFT, RIGHT, FULL, CROSS, and SELF joins with a use case for each.
7. `WHERE` vs `HAVING`. `UNION` vs `UNION ALL`. `DELETE` vs `TRUNCATE` vs `DROP`.
8. What are views, materialised views, stored procedures, and triggers? What are their pros and cons in microservices?
9. What is a CTE, and when does it help over subqueries? What is a recursive CTE?
10. What are window functions, and how do `ROW_NUMBER`, `RANK`, and `DENSE_RANK` differ?
11. `NULL` handling: `NULL = NULL`, `NOT IN` with NULLs, `COUNT(*)` vs `COUNT(col)`.
12. What are OLTP vs OLAP systems? Row store vs column store?

## 2. Indexing and Query Optimisation

1. How does a B-tree index work? What about hash, GiST/GIN, and bitmap indexes?
2. Clustered vs non-clustered index. How does this affect primary key choice (for example, InnoDB)?
3. Composite index: why does column order matter? What is the leftmost prefix rule?
4. What is a covering index, and what is an index-only scan?
5. When does the optimiser ignore an index (function on column, implicit cast, low selectivity, `LIKE '%x'`, stale statistics)?
6. How do you read an execution plan (`EXPLAIN`, `EXPLAIN ANALYZE`)? Seq scan vs index scan vs index-only scan, nested loop vs hash join vs merge join.
7. What are the costs of over-indexing on write-heavy tables?
8. Why is `OFFSET` pagination slow on large tables, and what is keyset pagination?
9. What is the N+1 query problem in JPA/Hibernate, and how do you fix it (fetch join, `@EntityGraph`, batch size)?
10. What is the difference between `COUNT(*)` on a huge table and an approximate count? How do you speed up counts?
11. What are connection pools (HikariCP)? How do you size them, and what goes wrong when the pool is too big?
12. How do you diagnose a slow query in production (slow query log, `pg_stat_statements`, AWR, query store)?

## 3. Transactions, Isolation and Locking

1. Explain the isolation levels (Read Uncommitted, Read Committed, Repeatable Read, Serializable) and the anomalies each prevents (dirty read, non-repeatable read, phantom read, write skew).
2. What is MVCC, and how does it let readers avoid blocking writers?
3. Pessimistic vs optimistic locking. When do you choose each? How does JPA `@Version` work?
4. What is a deadlock? How do you detect, prevent, and retry?
5. `SELECT ... FOR UPDATE`, `FOR SHARE`, `SKIP LOCKED`, `NOWAIT`: use cases.
6. Spring `@Transactional`: propagation levels, isolation, rollback rules, and why self-invocation does not start a transaction.
7. What is a distributed transaction (2PC, XA)? Why is it avoided in microservices?
8. What is a long-running transaction, and what damage does it do (bloat, lock holding, replication lag)?
9. How do you make a payment or transfer operation idempotent at the database level?

## 4. Design and Scaling

1. How do you scale reads (replicas, caching) and writes (sharding, partitioning)?
2. Partitioning vs sharding: differences, strategies (range, hash, list), and pitfalls.
3. How do you handle replication lag in application logic (read-your-writes)?
4. How do you run zero-downtime schema migrations (expand/contract, backfills, Flyway/Liquibase)?
5. How do you design for multi-tenancy (shared schema, schema per tenant, DB per tenant)?
6. How do you design soft deletes, audit trails, and temporal data?
7. How do you design a search or reporting workload without hurting OLTP (CQRS, read replica, warehouse, CDC)?
8. Caching strategies: cache-aside, write-through, write-behind. How do you handle invalidation and stampedes?
9. How do you store money (DECIMAL vs float), time (UTC, timezone), and large objects?
10. Backup, point-in-time recovery, RPO and RTO: how do you test them?
11. How do you choose between Postgres, MySQL, Oracle, SQL Server, MongoDB, Redis, Cassandra, and DynamoDB for a workload?

## 5. SQL Query Practice (with Solutions)

### Q1. Second highest salary.

```sql
SELECT MAX(salary) AS second_highest
FROM employees
WHERE salary < (SELECT MAX(salary) FROM employees);
```

### Q2. Top 2 salaries in each department (handles ties).

```sql
SELECT *
FROM (
  SELECT e.*,
         DENSE_RANK() OVER (PARTITION BY dept_id ORDER BY salary DESC) AS rnk
  FROM employees e
) t
WHERE rnk <= 2;
```

### Q3. Find duplicate emails.

```sql
SELECT email, COUNT(*) AS cnt
FROM customers
GROUP BY email
HAVING COUNT(*) > 1;
```

### Q4. Delete duplicates, keeping the row with the lowest id (PostgreSQL / MySQL 8).

```sql
WITH d AS (
  SELECT id,
         ROW_NUMBER() OVER (PARTITION BY email ORDER BY id) AS rn
  FROM customers
)
DELETE FROM customers
WHERE id IN (SELECT id FROM d WHERE rn > 1);
```

### Q5. Employees who earn more than their manager.

```sql
SELECT e.name
FROM employees e
JOIN employees m ON e.manager_id = m.id
WHERE e.salary > m.salary;
```

### Q6. Customers who have never placed an order.

```sql
SELECT c.*
FROM customers c
WHERE NOT EXISTS (
  SELECT 1 FROM orders o WHERE o.customer_id = c.id
);
```

Follow-up: why is `NOT IN (SELECT customer_id FROM orders)` risky? (If the subquery returns a NULL, the result is empty.)

### Q7. Departments with more than 5 employees.

```sql
SELECT dept_id, COUNT(*) AS emp_count
FROM employees
GROUP BY dept_id
HAVING COUNT(*) > 5;
```

### Q8. Department with the highest average salary.

```sql
SELECT dept_id, AVG(salary) AS avg_salary
FROM employees
GROUP BY dept_id
ORDER BY avg_salary DESC
LIMIT 1;           -- SQL Server: SELECT TOP 1 ...; Oracle: FETCH FIRST 1 ROW ONLY
```

### Q9. Running total of order amount per customer.

```sql
SELECT id, customer_id, created_at, total,
       SUM(total) OVER (PARTITION BY customer_id ORDER BY created_at) AS running_total
FROM orders;
```

### Q10. Month-over-month revenue change (PostgreSQL).

```sql
WITH m AS (
  SELECT DATE_TRUNC('month', created_at) AS month, SUM(total) AS revenue
  FROM orders
  WHERE status = 'COMPLETED'
  GROUP BY 1
)
SELECT month,
       revenue,
       revenue - LAG(revenue) OVER (ORDER BY month) AS change_from_prev
FROM m
ORDER BY month;
```

### Q11. Top 3 products by revenue in each category.

```sql
SELECT *
FROM (
  SELECT p.category, p.name,
         SUM(oi.qty * oi.price) AS revenue,
         ROW_NUMBER() OVER (PARTITION BY p.category ORDER BY SUM(oi.qty * oi.price) DESC) AS rn
  FROM order_items oi
  JOIN products p ON p.id = oi.product_id
  GROUP BY p.category, p.name
) t
WHERE rn <= 3;
```

### Q12. Latest order for each customer.

```sql
SELECT *
FROM (
  SELECT o.*,
         ROW_NUMBER() OVER (PARTITION BY customer_id ORDER BY created_at DESC) AS rn
  FROM orders o
) t
WHERE rn = 1;
```

### Q13. Customers whose total spend is above the average customer spend.

```sql
WITH spend AS (
  SELECT customer_id, SUM(total) AS total_spent
  FROM orders
  GROUP BY customer_id
)
SELECT *
FROM spend
WHERE total_spent > (SELECT AVG(total_spent) FROM spend);
```

### Q14. Keyset (seek) pagination.

```sql
SELECT *
FROM orders
WHERE (created_at, id) < (:lastCreatedAt, :lastId)
ORDER BY created_at DESC, id DESC
LIMIT 20;
```

### Q15. Create a composite index and check that it is used.

```sql
CREATE INDEX idx_orders_customer_created
  ON orders (customer_id, created_at DESC);

EXPLAIN ANALYZE
SELECT *
FROM orders
WHERE customer_id = 42
ORDER BY created_at DESC
LIMIT 20;
-- Look for an Index Scan (not Seq Scan) and no separate Sort node.
```

### Q16. Money transfer inside a transaction with a balance check.

```sql
BEGIN;

UPDATE accounts
SET balance = balance - 100
WHERE id = 1 AND balance >= 100;
-- Check affected rows = 1 in the application, otherwise ROLLBACK.

UPDATE accounts
SET balance = balance + 100
WHERE id = 2;

COMMIT;
```

### Q17. Pessimistic lock on a row.

```sql
BEGIN;
SELECT * FROM accounts WHERE id = 1 FOR UPDATE;
-- other transactions updating this row now wait
UPDATE accounts SET balance = balance - 50 WHERE id = 1;
COMMIT;
```

### Q18. Job queue: claim 10 jobs without blocking other workers.

```sql
SELECT id
FROM jobs
WHERE status = 'PENDING'
ORDER BY id
LIMIT 10
FOR UPDATE SKIP LOCKED;
```

### Q19. Optimistic locking with a version column.

```sql
UPDATE accounts
SET balance = :newBalance,
    version = version + 1
WHERE id = :id AND version = :expectedVersion;
-- 0 rows updated means a concurrent change: reload and retry or fail.
```

### Q20. Idempotent insert (ignore duplicate request).

```sql
-- PostgreSQL
INSERT INTO payments (idempotency_key, order_id, amount)
VALUES (:key, :orderId, :amount)
ON CONFLICT (idempotency_key) DO NOTHING;

-- MySQL
INSERT IGNORE INTO payments (idempotency_key, order_id, amount)
VALUES (:key, :orderId, :amount);
```

### Q21. Find employees hierarchy (recursive CTE).

```sql
WITH RECURSIVE org AS (
  SELECT id, name, manager_id, 1 AS level
  FROM employees
  WHERE manager_id IS NULL
  UNION ALL
  SELECT e.id, e.name, e.manager_id, o.level + 1
  FROM employees e
  JOIN org o ON e.manager_id = o.id
)
SELECT * FROM org ORDER BY level, name;
```

### Q22. Pivot-style report: order count per status per month.

```sql
SELECT DATE_TRUNC('month', created_at) AS month,
       COUNT(*) FILTER (WHERE status = 'NEW')       AS new_orders,
       COUNT(*) FILTER (WHERE status = 'COMPLETED') AS completed_orders,
       COUNT(*) FILTER (WHERE status = 'CANCELLED') AS cancelled_orders
FROM orders
GROUP BY 1
ORDER BY 1;
-- MySQL / SQL Server: use SUM(CASE WHEN status = 'NEW' THEN 1 ELSE 0 END)
```

## 6. Database Scenario Questions

1. A query that took 50 ms now takes 20 seconds after data growth. Walk through diagnosis and fixes.
2. Your service shows intermittent deadlocks under load. How do you find the cause and fix it?
3. Users see their own update missing right after saving (replica lag). How do you fix the read path?
4. You must add a NOT NULL column to a 1-billion-row table with no downtime. What are the steps?
5. A report query locks tables and slows down checkout. How do you separate the workloads?
6. Two requests withdraw from the same account at the same time and the balance goes negative. How do you prevent it at the database level?
7. The connection pool is exhausted during traffic spikes. What do you check and change (pool size, slow queries, long transactions, leaked connections)?
8. You need to migrate a large table to a new schema or a new database engine while traffic is live. What is your plan?
9. Which database would you choose for each: a payments ledger, a product catalog, a chat history, a session store, a leaderboard, and analytics? Why?
10. A table has grown to 2 TB and backups, vacuuming, and index maintenance are painful. What are your options (partitioning, archiving, sharding, tiering)?
11. How do you design a database strategy for 30 microservices owned by different teams (ownership, polyglot persistence, migrations, backups, access control)?
12. Your JPA application issues 2,000 queries to render one page. How do you detect and fix it?
