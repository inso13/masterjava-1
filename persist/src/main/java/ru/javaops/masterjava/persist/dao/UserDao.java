package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.BatchChunkSize;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * gkislin
 * 27.10.2016
 * <p>
 * <p>
 */
@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class UserDao implements AbstractDao {

    public List<User> insert(List<User> users) {
        List<User> newUsers = new ArrayList<>();
        List<User> oldUsers = new ArrayList<>();
        for (User user:users)
        if (user.isNew()) {
           newUsers.add(user);
        } else {
           oldUsers.add(user);
        }
        insertGeneratedId(newUsers);
        insertWitId(oldUsers);
        List<User> result = new ArrayList<>();
        result.addAll(newUsers); result.addAll(oldUsers);
        return result;
    }

    @SqlBatch("INSERT INTO users (full_name, email, flag) VALUES (:fullName, :email, CAST(:flag AS user_flag)) ")
    @GetGeneratedKeys
    @BatchChunkSize(5)
    abstract int insertGeneratedId(@BindBean List<User> users);

    @SqlBatch("INSERT INTO users (id, full_name, email, flag) VALUES (:id, :fullName, :email, CAST(:flag AS user_flag)) ")
    @BatchChunkSize(5)
    abstract void insertWitId(@BindBean List<User> users);

    @SqlQuery("SELECT * FROM users ORDER BY full_name, email LIMIT :it")
    public abstract List<User> getWithLimit(@Bind int limit);

    //   http://stackoverflow.com/questions/13223820/postgresql-delete-all-content
    @SqlUpdate("TRUNCATE users")
    @Override
    public abstract void clean();
}
