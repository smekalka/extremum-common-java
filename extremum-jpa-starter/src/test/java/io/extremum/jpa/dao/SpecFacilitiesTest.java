package io.extremum.jpa.dao;

import io.extremum.common.model.CollectionFilter;
import io.extremum.common.spring.data.OffsetBasedPageRequest;
import io.extremum.jpa.TestWithServices;
import io.extremum.jpa.model.AdvancedTestJpaModel;
import io.extremum.jpa.model.NestedModel;
import io.extremum.security.rules.service.SpecFacilities;
import io.extremum.sharedmodels.basic.MultilingualLanguage;
import io.extremum.sharedmodels.basic.StringOrMultilingual;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = JpaCommonDaoConfiguration.class)
public class SpecFacilitiesTest extends TestWithServices {

    @Autowired
    private AdvancedModelDao dao;

    private static final Locale locale = Locale.forLanguageTag("en-US");
    private final SpecFacilities specFacilities = new SpecFacilities(locale);

    @Test
    @Transactional
    public void specification_composed_properly() {
        for (int i = 0; i < 5; i++) {
            AdvancedTestJpaModel testModel = new AdvancedTestJpaModel();
            testModel.setSize(i);
            int finalI = i;
            testModel.setName(new StringOrMultilingual(new HashMap<MultilingualLanguage, String>() {{
                put(MultilingualLanguage.en_US, "event " + (finalI * 111));
                put(MultilingualLanguage.ru_RU, "событие " + (finalI * 111));
            }}, locale));

            List<NestedModel> nestedModelList = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                NestedModel nestedModel = new NestedModel();
                nestedModel.setName(new StringOrMultilingual(new HashMap<MultilingualLanguage, String>() {{
                    put(MultilingualLanguage.en_US, "nested " + (finalI * 222));
                    put(MultilingualLanguage.ru_RU, "вложенная " + (finalI * 222));
                    put(MultilingualLanguage.de_DE, "verschachtelt " + (finalI * 222));
                }}, locale));
                nestedModelList.add(nestedModel);
            }
            testModel.setNestedmodels(nestedModelList);
            NestedModel nestedModel = new NestedModel();
            nestedModel.setSize(i * 10);

            testModel.setNested(nestedModel);
            testModel.setNestedmodels(nestedModelList);

            dao.save(testModel);
        }
        Page<AdvancedTestJpaModel> result;
        Specification<AdvancedTestJpaModel> spec;

        spec = specFacilities.composeSpec(new CollectionFilter("object.size>2", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getSize() > 2));

        spec = specFacilities.composeSpec(new CollectionFilter("object.size<2", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getSize() < 2));

        spec = specFacilities.composeSpec(new CollectionFilter("object.size>=2", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(3, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getSize() >= 2));

        spec = specFacilities.composeSpec(new CollectionFilter("object.size<=2", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(3, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getSize() <= 2));

        spec = specFacilities.composeSpec(new CollectionFilter("object.name.like(\"event 1*\")", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(result.getContent().get(0).getName(), new StringOrMultilingual(new HashMap<MultilingualLanguage, String>() {{
            put(MultilingualLanguage.en_US, "event 111");
            put(MultilingualLanguage.ru_RU, "событие 111");
        }}, locale));

        spec = specFacilities.composeSpec(new CollectionFilter("object.name.matches(\"event & 222\")", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(result.getContent().get(0).getName(), new StringOrMultilingual(new HashMap<MultilingualLanguage, String>() {{
            put(MultilingualLanguage.en_US, "event 222");
            put(MultilingualLanguage.ru_RU, "событие 222");
        }}, locale));

        spec = specFacilities.composeSpec(new CollectionFilter("object.name.eq(\"событие 222\", \"ru-RU\")", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(1, result.getContent().size());
        assertEquals(result.getContent().get(0).getName(), new StringOrMultilingual(new HashMap<MultilingualLanguage, String>() {{
            put(MultilingualLanguage.en_US, "event 222");
            put(MultilingualLanguage.ru_RU, "событие 222");
        }}, locale));

        spec = specFacilities.composeSpec(new CollectionFilter("object.size>1 && object.nested.size<30", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getSize() > 1 && advancedTestJpaModel.getNested().getSize() < 30));

        spec = specFacilities.composeSpec(new CollectionFilter("object.name==\"event 444\" || object.nested.size<30", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(4, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getName().getText().equals("event 444") || advancedTestJpaModel.getNested().getSize() < 30));

        spec = specFacilities.composeSpec(new CollectionFilter("object.name!=\"event 444\"", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(4, result.getContent().size());
        assertTrue(result.getContent().stream().noneMatch(advancedTestJpaModel -> advancedTestJpaModel.getName().getText().equals("event 444")));

        spec = specFacilities.composeSpec(new CollectionFilter("object.name.any(\"event 111\", \"event 444\")", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(2, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getName().getText().equals("event 444") || advancedTestJpaModel.getName().getText().equals("event 111")));

        spec = specFacilities.composeSpec(new CollectionFilter("object.nestedmodels.exists(e, e.name==\"nested 222\")", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getNestedmodels().stream().anyMatch(nestedModel -> nestedModel.getName().getText().equals("nested 222"))));

        spec = specFacilities.composeSpec(new CollectionFilter("object.nestedmodels.exists(e, e.name.eq(\"вложенная 222\", \"ru-RU\"))", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getNestedmodels().stream().anyMatch(nestedModel -> nestedModel.getName().getText().equals("nested 222"))));

        spec = specFacilities.composeSpec(new CollectionFilter("object.nestedmodels.exists(e, e.name==\"nested 444\")", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getNestedmodels().stream().anyMatch(nestedModel -> nestedModel.getName().getText().equals("nested 444"))));

        spec = specFacilities.composeSpec(new CollectionFilter("object.nestedmodels.exists(e, e.name.eq(\"verschachtelt 222\", \"*\"))", MultilingualLanguage.en_US));
        result = dao.findAll(spec, new OffsetBasedPageRequest(0, 10));
        assertEquals(1, result.getContent().size());
        assertTrue(result.getContent().stream().allMatch(advancedTestJpaModel -> advancedTestJpaModel.getNestedmodels().stream().anyMatch(nestedModel -> nestedModel.getName().getText().equals("nested 222"))));
    }
}
