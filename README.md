# custom-orm-session

1. Create/Update `custom-orm-session`  project with a custom `Session` class (see description above if you don’t have one)
2. **Open transaction** when the session is created, and commit/rollback it when it gets closed
3. Add two methods to the session `persist`, `remove`
4. Create two new classes that represent **corresponding actions** `InsertAction`, `DeleteAction`
5. Implement methods in a such way, that they **create corresponding actions** and **add them to the queue** called `actionQueue`. Make that queue an ordered one, so **insert actions should be performed before delete actions**.
6. Implement method `flush` that goes through all actions and **performs corresponding SQL statements**.
7. Update session so it calls `flush` before closing
* Refactor your dirty checking mechanism so it creates `UpdateAction` and adds it to the queue
