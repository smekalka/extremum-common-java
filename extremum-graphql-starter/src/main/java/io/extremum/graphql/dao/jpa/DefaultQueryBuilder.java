package io.extremum.graphql.dao.jpa;

import com.google.common.base.CaseFormat;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.NestedModel;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.AllArgsConstructor;
import lombok.SneakyThrows;
import org.hibernate.boot.model.naming.Identifier;
import org.hibernate.proxy.HibernateProxy;
import org.springframework.data.domain.Pageable;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.stream.Collectors;

@AllArgsConstructor
public class DefaultQueryBuilder implements AdvancedQueryBuilder {

    private final EntityManager entityManager;

    @Override
    public <Folder extends BasicModel<?>, Item> Query composeQueryForCollectionIds(Folder folder, Class<Item> nestedClass, String collectionName, Pageable paging) {
        if (BasicModel.class.isAssignableFrom(nestedClass) || Descriptor.class.isAssignableFrom(nestedClass)) {
            return composeQueryForCollectionIds(folder, nestedClass, collectionName, paging, getClass(folder).getSimpleName().toLowerCase() + "_" + nestedClass.getSimpleName().toLowerCase(), "id");
        } else {
            return composeQueryForCollectionIds(folder, nestedClass, collectionName, paging, getClass(folder).getSimpleName().toLowerCase() + "_" + collectionName, "id");
        }
    }

    @Override
    public <Folder extends BasicModel<?>, Item> Query composeQueryForCollectionIds(Folder folder, Class<Item> nestedClass, String collectionName, Pageable paging, String joinTable, String reverseId) {
        StringBuilder sb = new StringBuilder();
        String nested_id_column = collectionName + "_id";
        String event_id_column = getClass(folder).getSimpleName().toLowerCase() + "_id";
        String nested_table = nestedClass.getSimpleName().toLowerCase();
        Query query;
        if (BasicModel.class.isAssignableFrom(nestedClass) || Descriptor.class.isAssignableFrom(nestedClass)) {
            sb
                    .append("select " + "    cast(")
                    .append(nested_id_column)
                    .append(" as varchar)  as nested_id, ")
                    .append("    cast(").append("jt.").append(event_id_column)
                    .append(" as varchar)  as model_id ")
                    .append("from ")
                    .append(joinTable)
                    .append(" jt")
                    .append("         inner join ")
                    .append(nested_table)
                    .append(" r on ")
                    .append(nested_id_column)
                    .append(" = r.")
                    .append(reverseId)
                    .append(" where ")
                    .append("jt.")
                    .append(event_id_column)
                    .append("=:id");
        } else {
            String modelTable = getClass(folder).getSimpleName().toLowerCase();
            sb
                    .append("select\n" + "    cast(")
                    .append(collectionName)
                    .append(" as varchar)  as nested_id,\n")
                    .append("    cast(")
                    .append(event_id_column)
                    .append(" as varchar)  as model_id\n")
                    .append("from ")
                    .append(joinTable)
                    .append("         inner join ")
                    .append(modelTable)
                    .append(" r on ")
                    .append(event_id_column)
                    .append(" = r.id\n")
                    .append("where ")
                    .append(event_id_column)
                    .append("=:id");
        }

        if (!paging.getSort().isEmpty()) {
            sb.append(" order by ");
            List<String> collect = paging.getSort().get().map(sortOrder -> sortOrder.getProperty() + " " + sortOrder.getDirection()).collect(Collectors.toList());
            sb.append(String.join(",", collect));
        }

        query = entityManager.createNativeQuery(sb.toString())
                .setParameter("id", UUID.fromString(folder.getUuid().getInternalId()));

        return query
                .setFirstResult((int) paging.getOffset())
                .setMaxResults(paging.getPageSize());
    }

    private static Class<?> getClass(Object proxy) {
        if (proxy instanceof HibernateProxy) {
            return ((HibernateProxy) proxy).getHibernateLazyInitializer()
                    .getImplementation()
                    .getClass();
        } else {
            return proxy.getClass();
        }
    }

    public <Folder, Item> Query deleteNestedElementCollectionQuery(String collectionName, Folder model, Item item) {
        String join_table = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getClass(model).getSimpleName()) + "_" + collectionName;
        if (item instanceof NestedModel) {
            return entityManager.
                    createNativeQuery("delete  from " + join_table + " where nested_id" + "=:value")
                    .setParameter("value", ((NestedModel) item).getNestedId());
        }
        if (item instanceof Descriptor) {
            return entityManager.
                    createNativeQuery("delete  from " + join_table + " where " + collectionName + "_id=:value")
                    .setParameter("value", ((Descriptor) item).getExternalId());
        }
        return entityManager.
                createNativeQuery("delete  from " + join_table + " where " + collectionName + "=:value")
                .setParameter("value", item);
    }

    public <Folder, Item extends BasicModel<?>> Query deleteNestedModelCollectionQuery(String collectionName, Folder model, Item item) {
        String nestedTable = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, item.getClass().getSimpleName());
        String join_table = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getClass(model).getSimpleName()) + "_" + nestedTable;
        return entityManager.createNativeQuery("delete from " + join_table + " where " + collectionName + "_id=:id")
                .setParameter("id", UUID.fromString(item.getUuid().getInternalId()));
    }

    public <Folder extends BasicModel<?>> Query insertToNestedModelCollectionQuery(Folder model, String collectionName, Descriptor d) {
        String join_table = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getClass(model).getSimpleName()) + "_" + collectionName;
        return entityManager
                .createNativeQuery("insert into " + join_table + " values (:folder_id, :nested_id)")
                .setParameter("folder_id", UUID.fromString(model.getUuid().getInternalId()))
                .setParameter("nested_id", UUID.fromString(d.getInternalId()));
    }

    public <Folder extends BasicModel<?>, Item> Query insertToNestedElementCollectionQuery(Folder model, String collectionName, Item item) {
        String join_table = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, getClass(model).getSimpleName()) + "_" + collectionName;
        if (item instanceof NestedModel) {
            String query = getAddToNestedCollectionQuery(model, join_table, (NestedModel) item);
            return entityManager.createNativeQuery(query);
        }
        return entityManager
                .createNativeQuery("insert into " + join_table + " values (:folder_id, :value)")
                .setParameter("folder_id", UUID.fromString(model.getUuid().getInternalId()))
                .setParameter("value", item);
    }

    @SneakyThrows
    private <Folder extends BasicModel<?>, Item extends NestedModel> String getAddToNestedCollectionQuery(Folder model, String collectionName, Item item) {
        Field[] fields = item.getClass().getDeclaredFields();
        List<String> columns = new ArrayList<>();
        columns.add(getIdentifier(Identifier.toIdentifier(model.getClass().getSimpleName())).render() + "_id");
        List<Object> values = new ArrayList<>();
        values.add(model.getUuid().getInternalId());

        columns.add("nested_id");
        values.add(item.getNestedId());

        for (Field field : fields) {

            field.setAccessible(true);
            if (BasicModel.class.isAssignableFrom(field.getType())) {
                String name = field.getName();
                Object o = field.get(item);
                if (o == null) {
                    continue;
                }
                String value = ((BasicModel<?>) o).getUuid().getInternalId();
                columns.add(getIdentifier(Identifier.toIdentifier(name)).render() + "_id");
                values.add(value);
            } else {
                String name = field.getName();
                Object value = field.get(item);
                if (value == null) {
                    continue;
                }
                columns.add(getIdentifier(Identifier.toIdentifier(name)).render());
                values.add(value);
            }

        }
        String sql = "insert into " +
                collectionName +
                "(" +
                String.join(",", columns) +
                ")values(" +
                values.stream().map(o -> {
                    if (o instanceof String || o instanceof UUID) {
                        return "'" + o + "'";
                    } else {
                        return o.toString();
                    }
                }).collect(Collectors.joining(",")) +
                ");";
        System.out.println(sql);
        System.out.println();

        return sql;
    }

    private Identifier getIdentifier(final Identifier name) {
        if (name == null) {
            return null;
        }
        StringBuilder builder = new StringBuilder(name.getText().replace('.', '_'));
        for (int i = 1; i < builder.length() - 1; i++) {
            if (isUnderscoreRequired(builder.charAt(i - 1), builder.charAt(i), builder.charAt(i + 1))) {
                builder.insert(i++, '_');
            }
        }
        return getIdentifier(builder.toString(), name.isQuoted());
    }

    private Identifier getIdentifier(String name, final boolean quoted) {
        return new Identifier(name.toLowerCase(Locale.ROOT), quoted);
    }

    private boolean isUnderscoreRequired(final char before, final char current, final char after) {
        return Character.isLowerCase(before) && Character.isUpperCase(current) && Character.isLowerCase(after);
    }
}