# MongoDB Interview Questions (10-Year Java Lead)

Queries are in `mongosh` syntax. `$setWindowFields` needs MongoDB 5.0+. Test the queries on your own data before relying on them.

## Table of Contents

- [Sample Data Used in the Query Practice](#sample-data-used-in-the-query-practice)
- [1. Fundamentals and Data Modeling](#1-fundamentals-and-data-modeling)
- [2. Indexing and Query Performance](#2-indexing-and-query-performance)
- [3. Replication, Sharding and Consistency](#3-replication-sharding-and-consistency)
- [4. Transactions and Concurrency](#4-transactions-and-concurrency)
- [5. Spring Data MongoDB](#5-spring-data-mongodb)
- [6. MongoDB Query Practice (with Solutions)](#6-mongodb-query-practice-with-solutions)
- [7. MongoDB Scenario Questions](#7-mongodb-scenario-questions)

---

## Sample Data Used in the Query Practice

```js
// users
{ _id: 1, name: "Asha", email: "asha@x.com", age: 29, status: "ACTIVE",
  address: { city: "Delhi", pin: "110001" }, tags: ["java", "kafka"], salary: 90000, dept: "ENG" }

// products
{ _id: "SKU-1", name: "Keyboard", category: "ACCESSORIES", price: 1500, stock: 40 }

// orders
{ _id: 101, userId: 1, status: "COMPLETED", total: 4500, createdAt: ISODate("2026-09-15T10:00:00Z"),
  items: [ { sku: "SKU-1", qty: 2, price: 1500 }, { sku: "SKU-2", qty: 1, price: 1500 } ] }
```

## 1. Fundamentals and Data Modeling

1. When would you choose MongoDB over a relational database, and when would you not?
2. Explain BSON vs JSON. Why does BSON matter (types, ordering, size limit of 16 MB)?
3. Embedding vs referencing: how do you decide? Give examples for one-to-few, one-to-many, and many-to-many.
4. What is the unbounded array anti-pattern, and how do you avoid it?
5. What are the bucket, subset, extended reference, and computed patterns? Where have you used them?
6. How do you handle schema evolution in a schemaless database (schema versioning, `$jsonSchema` validation)?
7. How do you model a hierarchy (comments, categories, org chart)? Compare parent reference, child reference, materialised path.
8. What is the difference between `_id` types (ObjectId, UUID, custom)? What are the trade-offs for sharding and index size?
9. How does `$lookup` work, and why is it not a replacement for relational joins at scale?
10. How are documents stored (WiredTiger, document-level locking, compression, journaling)?
11. What are write concern, read concern, and read preference? How do they combine?
12. What is the difference between `updateOne`, `replaceOne`, and `findOneAndUpdate`?

## 2. Indexing and Query Performance

1. Which index types exist (single, compound, multikey, text, geospatial, hashed, TTL, partial, sparse, wildcard)? When do you use each?
2. Explain the ESR rule (Equality, Sort, Range) for compound index design.
3. What is a covered query, and how do you verify one?
4. How do you read `explain("executionStats")`? What do `COLLSCAN`, `IXSCAN`, `totalDocsExamined`, and `nReturned` tell you?
5. Why can an index not be used for a query? Name several cases (type mismatch, regex without prefix, `$ne`/`$nin`, wrong field order, function on field).
6. What is the cost of too many indexes?
7. How do you find unused or redundant indexes in production (`$indexStats`)?
8. How do you build an index on a large production collection without downtime?
9. Why is `skip` + `limit` slow for deep pagination, and what is the alternative?
10. How do you find slow queries (profiler, slow query log, Atlas Performance Advisor)?
11. How does the working set vs RAM affect performance?
12. Multikey index limitations: why can't you have a compound index on two array fields?

## 3. Replication, Sharding and Consistency

1. How does a replica set work (primary, secondaries, oplog, election)? What triggers an election?
2. What is replication lag and how does it affect reads from secondaries?
3. Explain write concern `w:1` vs `w:"majority"` and the risk of rollback.
4. What are read concern levels (`local`, `majority`, `snapshot`, `linearizable`)?
5. When do you shard? How do you choose a shard key (cardinality, frequency, monotonic growth)?
6. Hashed vs ranged sharding: trade-offs.
7. What is a jumbo chunk, and how do you avoid hot shards?
8. What happens to queries that don't include the shard key (scatter-gather)?
9. How does MongoDB fit into the CAP theorem?
10. How do you design backup and restore (snapshots, point-in-time recovery, oplog)?
11. How do you migrate data between clusters or versions with minimal downtime?

## 4. Transactions and Concurrency

1. Are MongoDB operations atomic? What is guaranteed at the single-document level?
2. When do you need multi-document transactions, and what are their costs and limits?
3. How can good data modeling reduce the need for transactions?
4. How do you implement optimistic locking (version field)?
5. How do you implement atomic counters, inventory reservation, or "claim a job" without transactions?
6. What are change streams? What are their requirements and use cases (CDC, outbox, cache invalidation)?
7. How do you deal with duplicate key errors in concurrent upserts?
8. What are write conflicts in transactions, and how do you retry (`TransientTransactionError`)?
9. How do you implement idempotency with MongoDB (unique index on idempotency key)?

## 5. Spring Data MongoDB

1. `MongoRepository` vs `MongoTemplate`: when do you use each?
2. How do you write aggregation pipelines in Spring (`Aggregation`, `@Aggregation`)?
3. How do you enable transactions (`MongoTransactionManager`, replica set requirement, `@Transactional`)?
4. How do you define indexes in code (`@Indexed`, `@CompoundIndex`), and why is auto-index creation risky in production?
5. How do you implement optimistic locking (`@Version`)?
6. How do you use projections to avoid loading large documents?
7. How do you handle `@DBRef` vs manual references, and why is `@DBRef` discouraged?
8. How do you configure connection pool, timeouts, read preference, and write concern in Spring Boot?
9. How do you do auditing (`@CreatedDate`, `@LastModifiedDate`)?
10. How do you do bulk operations with `BulkOperations`?
11. How do you test with Testcontainers for MongoDB?
12. How do you use change streams in Spring (`MessageListenerContainer`, reactive `changeStream`)?

## 6. MongoDB Query Practice (with Solutions)

### Q1. Find active users in Delhi aged 25 or above. Return only name, email, age, sorted by age descending, top 10.

```js
db.users.find(
  { status: "ACTIVE", "address.city": "Delhi", age: { $gte: 25 } },
  { _id: 0, name: 1, email: 1, age: 1 }
).sort({ age: -1 }).limit(10)
```

### Q2. Find users who have the tag "java" or "kafka", excluding INACTIVE users.

```js
db.users.find({
  status: { $ne: "INACTIVE" },
  tags: { $in: ["java", "kafka"] }
})
```

### Q3. Find orders that contain an item with qty >= 2 and price > 1000 (same array element).

```js
db.orders.find({
  items: { $elemMatch: { qty: { $gte: 2 }, price: { $gt: 1000 } } }
})
```

Follow-up: what goes wrong if you write `"items.qty": {$gte: 2}, "items.price": {$gt: 1000}` without `$elemMatch`? (Conditions can be satisfied by different array elements.)

### Q4. Increment stock, upsert a product if it does not exist.

```js
db.products.updateOne(
  { _id: "SKU-9" },
  { $inc: { stock: 10 }, $setOnInsert: { name: "Mouse", price: 800 } },
  { upsert: true }
)
```

### Q5. Update one array element: set qty to 5 for SKU-1 in order 101.

```js
db.orders.updateOne(
  { _id: 101, "items.sku": "SKU-1" },
  { $set: { "items.$.qty": 5 } }
)
```

### Q6. Apply a 10% discount to all items priced above 1000 in NEW orders (multiple array elements).

```js
db.orders.updateMany(
  { status: "NEW" },
  { $set: { "items.$[i].discount": 10 } },
  { arrayFilters: [ { "i.price": { $gt: 1000 } } ] }
)
```

### Q7. Keep only the last 10 searches in an array.

```js
db.users.updateOne(
  { _id: 1 },
  { $push: { recentSearches: { $each: ["spring boot"], $slice: -10 } } }
)
```

### Q8. Top 5 customers by total spend (completed orders only).

```js
db.orders.aggregate([
  { $match: { status: "COMPLETED" } },
  { $group: { _id: "$userId", totalSpent: { $sum: "$total" }, orders: { $sum: 1 } } },
  { $sort: { totalSpent: -1 } },
  { $limit: 5 }
])
```

### Q9. Join orders with users and return order total with user name and email.

```js
db.orders.aggregate([
  { $lookup: { from: "users", localField: "userId", foreignField: "_id", as: "user" } },
  { $unwind: "$user" },
  { $project: { _id: 1, total: 1, "user.name": 1, "user.email": 1 } }
])
```

### Q10. Top 10 selling products by units sold, with revenue.

```js
db.orders.aggregate([
  { $match: { status: "COMPLETED" } },
  { $unwind: "$items" },
  { $group: {
      _id: "$items.sku",
      unitsSold: { $sum: "$items.qty" },
      revenue: { $sum: { $multiply: ["$items.qty", "$items.price"] } }
  } },
  { $sort: { unitsSold: -1 } },
  { $limit: 10 }
])
```

### Q11. Monthly revenue report.

```js
db.orders.aggregate([
  { $match: { status: "COMPLETED" } },
  { $group: {
      _id: { $dateToString: { format: "%Y-%m", date: "$createdAt" } },
      revenue: { $sum: "$total" },
      orders: { $sum: 1 }
  } },
  { $sort: { _id: 1 } }
])
```

### Q12. Paginated order list with total count in a single query.

```js
db.orders.aggregate([
  { $match: { status: "COMPLETED" } },
  { $sort: { createdAt: -1, _id: -1 } },
  { $facet: {
      data: [ { $skip: 20 }, { $limit: 10 } ],
      meta: [ { $count: "total" } ]
  } }
])
```

### Q13. Find duplicate emails in users.

```js
db.users.aggregate([
  { $group: { _id: "$email", count: { $sum: 1 }, ids: { $push: "$_id" } } },
  { $match: { count: { $gt: 1 } } }
])
```

### Q14. Get the latest order for every user.

```js
db.orders.aggregate([
  { $sort: { userId: 1, createdAt: -1 } },
  { $group: { _id: "$userId", latestOrder: { $first: "$$ROOT" } } }
])
```

### Q15. Highest-paid employee in each department.

```js
db.users.aggregate([
  { $sort: { salary: -1 } },
  { $group: { _id: "$dept", name: { $first: "$name" }, salary: { $first: "$salary" } } }
])
```

### Q16. Second highest salary overall.

```js
db.users.find({}, { _id: 0, name: 1, salary: 1 })
  .sort({ salary: -1 })
  .skip(1)
  .limit(1)
```

Follow-up: how does this behave when two people share the top salary? (It returns the second row, not the second distinct value. Use `$group` by salary first for distinct values.)

### Q17. Running total of order amount per user (MongoDB 5.0+).

```js
db.orders.aggregate([
  { $setWindowFields: {
      partitionBy: "$userId",
      sortBy: { createdAt: 1 },
      output: {
        runningTotal: {
          $sum: "$total",
          window: { documents: ["unbounded", "current"] }
        }
      }
  } }
])
```

### Q18. Create the right index for this query and prove it is used.

```js
// Query
db.orders.find({ userId: 1, status: "COMPLETED" }).sort({ createdAt: -1 })

// Index (equality, equality, sort)
db.orders.createIndex({ userId: 1, status: 1, createdAt: -1 })

// Verify: look for IXSCAN, no in-memory SORT stage, docsExamined close to nReturned
db.orders.find({ userId: 1, status: "COMPLETED" })
  .sort({ createdAt: -1 })
  .explain("executionStats")
```

### Q19. Create unique, TTL, and partial indexes.

```js
// Unique
db.users.createIndex({ email: 1 }, { unique: true })

// TTL: delete session documents 1 hour after createdAt
db.sessions.createIndex({ createdAt: 1 }, { expireAfterSeconds: 3600 })

// Partial: index only documents that are still NEW
db.orders.createIndex(
  { createdAt: -1 },
  { partialFilterExpression: { status: "NEW" } }
)
```

### Q20. Reserve inventory atomically without overselling.

```js
db.products.findOneAndUpdate(
  { _id: "SKU-1", stock: { $gte: 2 } },
  { $inc: { stock: -2 } },
  { returnDocument: "after" }
)
// null result means insufficient stock
```

### Q21. Optimistic locking with a version field.

```js
const res = db.accounts.updateOne(
  { _id: 7, version: 3 },
  { $set: { balance: 900 }, $inc: { version: 1 } }
)
if (res.modifiedCount === 0) {
  // someone else updated the document: reload and retry, or fail
}
```

### Q22. Multi-document transaction (transfer money between two accounts).

```js
const session = db.getMongo().startSession();
session.startTransaction({
  readConcern: { level: "snapshot" },
  writeConcern: { w: "majority" }
});
try {
  const accounts = session.getDatabase("bank").accounts;
  const debit = accounts.updateOne(
    { _id: 1, balance: { $gte: 100 } },
    { $inc: { balance: -100 } }
  );
  if (debit.modifiedCount !== 1) throw new Error("Insufficient funds");
  accounts.updateOne({ _id: 2 }, { $inc: { balance: 100 } });
  session.commitTransaction();
} catch (e) {
  session.abortTransaction();
  throw e;
} finally {
  session.endSession();
}
```

### Q23. Bulk write with mixed operations.

```js
db.products.bulkWrite([
  { updateOne: { filter: { _id: "SKU-1" }, update: { $inc: { stock: -1 } } } },
  { insertOne: { document: { _id: "SKU-3", name: "Monitor", price: 12000, stock: 5 } } },
  { deleteOne: { filter: { _id: "SKU-OLD" } } }
], { ordered: false })
```

### Q24. Watch for new orders with a change stream.

```js
const cs = db.orders.watch([
  { $match: { operationType: "insert" } }
]);
while (!cs.isClosed()) {
  if (cs.hasNext()) printjson(cs.next());
}
```

### Q25. Add schema validation to a collection.

```js
db.createCollection("customers", {
  validator: {
    $jsonSchema: {
      bsonType: "object",
      required: ["name", "email"],
      properties: {
        name:  { bsonType: "string" },
        email: { bsonType: "string", pattern: "^.+@.+$" },
        age:   { bsonType: "int", minimum: 18 }
      }
    }
  },
  validationLevel: "strict",
  validationAction: "error"
})
```

### Q26. Range-based pagination (avoid skip).

```js
// First page
db.orders.find().sort({ createdAt: -1, _id: -1 }).limit(20)

// Next page: pass the last seen createdAt and _id
db.orders.find({
  $or: [
    { createdAt: { $lt: lastCreatedAt } },
    { createdAt: lastCreatedAt, _id: { $lt: lastId } }
  ]
}).sort({ createdAt: -1, _id: -1 }).limit(20)
```

### Q27. Count orders per status and the average order value, in one pass.

```js
db.orders.aggregate([
  { $group: { _id: "$status", count: { $sum: 1 }, avgTotal: { $avg: "$total" } } }
])
```

### Q28. Find users who have never placed an order.

```js
db.users.aggregate([
  { $lookup: { from: "orders", localField: "_id", foreignField: "userId", as: "o" } },
  { $match: { o: { $size: 0 } } },
  { $project: { name: 1, email: 1 } }
])
```

### Q29. Find which stage of this query is slow.

```js
db.orders.find({ status: "NEW", total: { $gt: 1000 } }).explain("executionStats")
// Check: winningPlan.stage (COLLSCAN vs IXSCAN), totalKeysExamined,
// totalDocsExamined, nReturned, executionTimeMillis, and any SORT stage.
```

## 7. MongoDB Scenario Questions

1. An orders collection has grown to 500 million documents and queries by `userId` and date range are slow. Walk through your investigation and fixes.
2. A product document has a `reviews` array that grows without limit and documents are approaching 16 MB. How do you remodel it?
3. You need to show an order with customer details and product details on one screen. Do you embed, reference, or duplicate fields? How do you keep duplicates in sync?
4. Writes succeeded but a secondary read returns stale data right after. Why, and what do you change?
5. After a primary failover some acknowledged writes vanished. What happened, and how do you prevent it?
6. Your chosen shard key is `createdAt` and one shard takes all the writes. What is wrong and how do you fix it?
7. A deployment created an index in the foreground on a large collection and the app timed out. What should the process have been?
8. Two concurrent requests both created the same user with the same email. How do you prevent it?
9. You need exactly-once-like processing for events stored in MongoDB. How do you design idempotency and the outbox?
10. You must migrate from MongoDB to a relational database (or the reverse) with zero downtime. What is your approach?
11. A team uses MongoDB transactions everywhere. When do you push back, and what do you suggest instead?
12. Atlas costs have doubled. How do you find out why and reduce them?
13. A report aggregation takes 40 seconds and blocks the API. How do you optimise it (indexes, pipeline order, `allowDiskUse`, pre-aggregation, read replicas, separate analytics store)?
14. How do you implement soft delete, audit history, and GDPR deletion in MongoDB?
