package io.extremum.common.limit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.extremum.common.collection.visit.DefaultCollectionVisitDriver;
import io.extremum.common.attribute.Attribute;
import io.extremum.common.walk.ShallowAttributeGraphWalker;
import io.extremum.common.walk.VisitDirection;
import io.extremum.sharedmodels.dto.ResponseDto;
import io.extremum.sharedmodels.fundamental.CollectionReference;

import java.util.List;

public class ResponseLimiterImpl implements ResponseLimiter {
    private final long topElementsBudgetInBytes;
    private final ObjectMapper objectMapper;

    private final DefaultCollectionVisitDriver collectionVisitDriver = new DefaultCollectionVisitDriver(
            VisitDirection.LEAVES_TO_ROOT, this::limitCollectionTop, new ShallowAttributeGraphWalker());

    public ResponseLimiterImpl(long topElementsBudgetInBytes, ObjectMapper objectMapper) {
        this.topElementsBudgetInBytes = topElementsBudgetInBytes;
        this.objectMapper = objectMapper;
    }

    @Override
    public void limit(ResponseDto responseDto) {
        collectionVisitDriver.visitCollectionsInResponseDto(responseDto);
    }

    private void limitCollectionTop(CollectionReference<?> reference, Attribute attribute, ResponseDto dto) {
        if (reference.getTop() == null) {
            return;
        }

        int topCollectionLimit = capTopLimitBasedOnBudget(reference);
        applyLimitToCollection(reference, topCollectionLimit);
    }

    private int capTopLimitBasedOnBudget(CollectionReference<?> reference) {
        int elementsThatFit = 0;
        long accumulatedElementsSize = 0;

        for (Object element : reference.getTop()) {
            long currentElementJsonSize = estimateJsonSize(element);
            if (accumulatedElementsSize + currentElementJsonSize > topElementsBudgetInBytes) {
                break;
            }

            accumulatedElementsSize += currentElementJsonSize;
            elementsThatFit++;
        }

        if (elementsThatFit == 0 && reference.getTop().size() > 0) {
            elementsThatFit++;
        }

        return elementsThatFit;
    }

    private long estimateJsonSize(Object object) {
        byte[] serializedForm;
        try {
            serializedForm = objectMapper.writer().writeValueAsBytes(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Should not happen as we are serializing in memory", e);
        }
        return serializedForm.length;
    }

    private void applyLimitToCollection(CollectionReference<?> collectionReference, int topCollectionLimit) {
        @SuppressWarnings("unchecked")
        CollectionReference<Object> castReference = (CollectionReference<Object>) collectionReference;

        List<Object> cappedTop = castReference.getTop().subList(0, topCollectionLimit);
        castReference.setTop(cappedTop);
    }
}