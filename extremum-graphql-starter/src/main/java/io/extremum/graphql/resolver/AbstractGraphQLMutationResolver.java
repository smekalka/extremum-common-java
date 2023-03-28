package io.extremum.graphql.resolver;

import com.github.dozermapper.core.Mapper;
import graphql.execution.MergedField;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.NullValue;
import graphql.language.ObjectField;
import graphql.language.ObjectValue;
import graphql.language.Value;
import graphql.schema.DataFetchingEnvironment;
import io.extremum.everything.services.defaultservices.DefaultRemover;
import io.extremum.everything.services.management.ModelRetriever;
import io.extremum.everything.services.management.ModelSaver;
import io.extremum.graphql.dao.IdToModelResolver;
import io.extremum.graphql.model.mapping.BeanMapperFactory;
import io.extremum.security.DataSecurity;
import io.extremum.sharedmodels.basic.BasicModel;
import io.extremum.sharedmodels.basic.Model;
import io.extremum.sharedmodels.descriptor.Descriptor;
import lombok.SneakyThrows;
import org.apache.commons.beanutils.PropertyUtilsBean;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public abstract class AbstractGraphQLMutationResolver {

    private final ModelSaver modelSaver;
    private final ModelRetriever modelRetriever;
    private final DataSecurity dataSecurity;
    private final DefaultRemover defaultRemover;
    private final Mapper mapper;
    private final IdToModelResolver idToModelResolver;

    public AbstractGraphQLMutationResolver(ModelSaver modelSaver, ModelRetriever modelRetriever, DataSecurity dataSecurity, DefaultRemover defaultRemover) {
        this.modelSaver = modelSaver;
        this.modelRetriever = modelRetriever;
        this.dataSecurity = dataSecurity;
        this.defaultRemover = defaultRemover;
        this.idToModelResolver = new IdToModelResolver(modelRetriever);
        this.mapper = new BeanMapperFactory().getMapper();
    }

    private Mapper getMapper() {
        return mapper;
    }

    @SneakyThrows
    protected <T extends BasicModel<?>> T updateOrCreate(String id, T input, DataFetchingEnvironment environment) {
        T model;
        List<String> nullValues = new ArrayList<>();
        MergedField mergedField = environment.getMergedField();
        PropertyUtilsBean pub = new PropertyUtilsBean();
        if (mergedField != null) {
            List<Field> fields = mergedField.getFields();
            Optional<Argument> inputArgumentOpt = fields.get(0).getArguments()
                    .stream()
                    .filter(argument -> argument.getName().equals("input"))
                    .findFirst();

            if (inputArgumentOpt.isPresent()) {
                nullValues = getNullValues(inputArgumentOpt.get().getValue());
            }
        }
        if (id != null) {
            if (input == null) {
                model = (T) modelRetriever.retrieveModel(new Descriptor(id));
                dataSecurity.checkGetAllowed(model);
            } else {
                model = (T) modelRetriever.retrieveModel(new Descriptor(id));
                dataSecurity.checkGetAllowed(model);
                dataSecurity.checkPatchAllowed(model);
                getMapper().map(input, model);
                idToModelResolver.resolveNestedModels(model);
                for (String nullValue : nullValues) {
                    pub.setProperty(model, nullValue, null);
                }
                model = (T) modelSaver.saveModel(model);
            }
        } else {
            dataSecurity.checkCreateAllowed(input);
            idToModelResolver.resolveNestedModels(input);
            model = (T) modelSaver.saveModel(input);
        }

        return model;
    }

    private List<String> getNullValues(Value value) {
        List<String> result = new ArrayList<>();
        doGetNullValues(value, "", result);

        return result;
    }

    private void doGetNullValues(Value value, String root, List<String> result) {
        String newRoot;
        if (value instanceof ObjectValue) {
            for (ObjectField objectField : ((ObjectValue) value).getObjectFields()) {
                if (root.isEmpty()) {
                    newRoot = objectField.getName();
                } else {
                    newRoot = StringUtils.joinWith(".", (Object[]) new String[]{root, objectField.getName()});
                }
                if (objectField.getValue() instanceof NullValue) {
                    result.add(newRoot);
                }
                doGetNullValues(objectField.getValue(), newRoot, result);
            }
        }
    }

    protected boolean delete(String id) {
        Descriptor descriptor = new Descriptor(id);
        Model model = modelRetriever.retrieveModel(descriptor);
        dataSecurity.checkRemovalAllowed(model);
        defaultRemover.remove(descriptor.getInternalId());
        return true;
    }
}