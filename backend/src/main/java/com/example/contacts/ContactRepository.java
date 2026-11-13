package com.example.contacts;

import com.example.contacts.ContactController.Contact;
import com.example.contacts.ContactController.ContactInput;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Statement;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Repository
public class ContactRepository {
    private final JdbcTemplate jdbc;
    private static final RowMapper<Contact> MAPPER = (rs, rowNum) -> new Contact(
            rs.getLong("id"), rs.getString("name"), rs.getString("email"),
            rs.getString("phone"), rs.getString("note"));

    public ContactRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public List<Contact> findAll(String keyword) {
        // LOCATE 按字面搜索，输入 % 或 _ 不会变成 SQL 通配符。
        return jdbc.query("""
                SELECT id, name, email, phone, note FROM contacts
                WHERE ? = '' OR LOCATE(?, CONCAT_WS(' ', name, email, phone, note)) > 0
                ORDER BY id DESC
                """, MAPPER, keyword, keyword);
    }

    public Optional<Contact> findById(long id) {
        return jdbc.query("SELECT id, name, email, phone, note FROM contacts WHERE id = ?",
                MAPPER, id).stream().findFirst();
    }

    public Contact create(ContactInput input) {
        Contact value = normalized(0, input);
        GeneratedKeyHolder key = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            var statement = connection.prepareStatement(
                    "INSERT INTO contacts (name, email, phone, note) VALUES (?, ?, ?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            statement.setString(1, value.name());
            statement.setString(2, value.email());
            statement.setString(3, value.phone());
            statement.setString(4, value.note());
            return statement;
        }, key);
        return normalized(Objects.requireNonNull(key.getKey()).longValue(), input);
    }

    public Optional<Contact> update(long id, ContactInput input) {
        Contact value = normalized(id, input);
        int count = jdbc.update("UPDATE contacts SET name = ?, email = ?, phone = ?, note = ? WHERE id = ?",
                value.name(), value.email(), value.phone(), value.note(), id);
        return count > 0 ? Optional.of(value) : Optional.empty();
    }

    public boolean delete(long id) {
        return jdbc.update("DELETE FROM contacts WHERE id = ?", id) > 0;
    }

    private Contact normalized(long id, ContactInput input) {
        return new Contact(id, input.name().strip(), input.email().strip(), input.phone().strip(),
                input.note() == null ? "" : input.note().strip());
    }
}
