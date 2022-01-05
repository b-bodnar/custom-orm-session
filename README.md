# custom-orm-session

**Persistence Context. Session.**
1. Create a new project `custom-orm-session`, and add JDBC Driver dependency
2. Create a custom `SessionFactory` class that accepts `DataSource` and has a `createSession` method
3. Create a custom `Session` class that accepts `DataSource` and has methods `find(Class<T> entityType, Object id)` and `close()`
4. Create custom annotations `Table`, `Column`
5. Implement method find using `JDBC API`
6. Introduce session cache
    a. Store loaded entities to the map when it’s loaded
    b. Don’t call the DB if entity with given `id` is already loaded, return value from the map instead
7. Introduce update mechanism
    a. Create another map that will store an entity snapshot copy (initial field values)
    b. On session close, compare entity with its copy and if at least one field has changed, perform UPDATE statement
8. Publish your code on GitHub, and post a list as a response to this message

**Action Queue. Flush**
1. Create/Update `custom-orm-session`  project with a custom `Session` class (see description above if you don’t have one)
2. **Open transaction** when the session is created, and commit/rollback it when it gets closed
3. Add two methods to the session `persist`, `remove`
4. Create two new classes that represent **corresponding actions** `InsertAction`, `DeleteAction`
5. Implement methods in a such way, that they **create corresponding actions** and **add them to the queue** called `actionQueue`. Make that queue an ordered one, so **insert actions should be performed before delete actions**.
6. Implement method `flush` that goes through all actions and **performs corresponding SQL statements**.
7. Update session so it calls `flush` before closing
* Refactor your dirty checking mechanism so it creates `UpdateAction` and adds it to the queue
