package ru.javaops.masterjava.persist.dao;

import com.bertoncelj.jdbi.entitymapper.EntityMapperFactory;
import one.util.streamex.StreamEx;
import org.skife.jdbi.v2.sqlobject.*;
import org.skife.jdbi.v2.sqlobject.customizers.RegisterMapperFactory;
import ru.javaops.masterjava.persist.model.City;
import ru.javaops.masterjava.persist.model.EmailResult;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@RegisterMapperFactory(EntityMapperFactory.class)
public abstract class EmailDao implements AbstractDao {

    @SqlUpdate("TRUNCATE email_result CASCADE ")
    @Override
    public abstract void clean();

    @SqlQuery("SELECT * FROM email_result ORDER BY datetime")
    public abstract List<EmailResult> getAll();

    @SqlUpdate("INSERT INTO email_result (name, datetime) VALUES (:name, :dateTime)")
    @GetGeneratedKeys
    public abstract int insertGeneratedId(@BindBean EmailResult emailResult);

    public void insert(EmailResult emailResult) {
        int id = insertGeneratedId(emailResult);
        emailResult.setId(id);
    }

    @SqlBatch("INSERT INTO email_result (name, datetime) VALUES (:name, :dateTime)")
    public abstract void insertBatch(@BindBean Collection<EmailResult> emailResults);
}
