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


