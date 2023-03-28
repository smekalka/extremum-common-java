package io.extremum.jpa.dao;

import io.extremum.jpa.TestWithServices;
import io.extremum.jpa.model.Parent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;
import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.util.UUID;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNull;


@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
public class JpaFieldsInReferencesBehaviorTest extends TestWithServices {
    @Autowired
    private EntityManager entityManager;
    @Autowired
    private DataSource dataSource;

    private UUID parentId;

    @BeforeEach
    void populateDatabase() {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(dataSource);

        parentId = UUID.randomUUID();

        addFather(jdbcTemplate);

        addChild(jdbcTemplate, "Tim");
        addChild(jdbcTemplate, "Ann");
    }

    private void addFather(JdbcTemplate jdbcTemplate) {
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement("insert into parent (name, id) values (?, ?)");
            statement.setString(1, "Joe");
            statement.setObject(2, parentId);
            return statement;
        });
    }

    private void addChild(JdbcTemplate jdbcTemplate, String childName) {
        jdbcTemplate.update(con -> {
            PreparedStatement statement = con.prepareStatement("insert into child (name, parent_id, id) values (?, ?, ?)");
            statement.setString(1, childName);
            statement.setObject(2, parentId);
            statement.setObject(3, UUID.randomUUID());
            return statement;
        });
    }

    @Test
    @Transactional
    public void testWithFind() {
        Parent parent = entityManager.find(Parent.class, parentId);

        assertThat(parent.name, is("Joe"));
        assertThat(parent.children, hasSize(2));
    }

    @Test
    @Transactional
    public void testWithGetReference() {
        Parent parent = entityManager.getReference(Parent.class, parentId);

        assertThat(parent.getName(), is("Joe"));
        assertThat(parent.getChildren(), hasSize(2));
    }

    @Test
    @Transactional
    public void testThatFieldsInAProxyObtainedViaReferenceNeverChange() {
        Parent parent = entityManager.getReference(Parent.class, parentId);

        assertNull(parent.name);
        assertThat(parent.children, hasSize(0));

        assertThat(parent.getName(), is("Joe"));
        assertThat(parent.getChildren(), hasSize(2));

        // ... but still ...
        assertNull(parent.name);
        assertThat(parent.children, hasSize(0));
    }
}
