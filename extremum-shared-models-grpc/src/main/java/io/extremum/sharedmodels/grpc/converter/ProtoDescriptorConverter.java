package io.extremum.sharedmodels.grpc.converter;

import com.google.protobuf.Int64Value;
import io.extremum.sharedmodels.descriptor.Descriptor;
import io.extremum.sharedmodels.proto.common.ProtoDescriptor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoDescriptorConverter {
    private final ProtoDisplayConverter displayConverter;
    private final ProtoZonedTimestampConverter timestampConverter;

    public ProtoDescriptor createProto(Descriptor descriptor) {
        if (descriptor == null) {
            return ProtoDescriptor.getDefaultInstance();
        }
        ProtoDescriptor.Builder descriptorBuilder = ProtoDescriptor.newBuilder();

        Long version = descriptor.getVersion();
        if (version != null) {
            descriptorBuilder.setVersion(Int64Value.newBuilder().setValue(version).build());
        }

        if (descriptor.getStorageType() != null) {
            descriptorBuilder.setStorageType(descriptor.getStorageType());
        }

        String externalId = descriptor.getExternalId();
        String internalId = descriptor.getInternalId();
        String modelType = descriptor.getModelType();

        if (descriptor.getCreated() != null) {
            descriptorBuilder.setCreated(timestampConverter.createProto(descriptor.getCreated()));
        }
        if (descriptor.getModified() != null) {
            descriptorBuilder.setModified(timestampConverter.createProto(descriptor.getModified()));
        }

        return descriptorBuilder
                .setDeleted(descriptor.isDeleted())
                .setExternalId(externalId == null ? "" : externalId)
                .setInternalId(internalId == null ? "" : internalId)
                .setModelType(modelType == null ? "" : modelType)
                .setDisplay(displayConverter.createProto(descriptor.getDisplay()))
                .build();
    }

    public Descriptor createFromProto(ProtoDescriptor proto) {
        if (proto.equals(ProtoDescriptor.getDefaultInstance())) {
            return null;
        }
        String externalId = proto.getExternalId();
        String internalId = proto.getInternalId();
        String modelType = proto.getModelType();

        Descriptor descriptor = new Descriptor();
        descriptor.setDeleted(proto.getDeleted());
        descriptor.setExternalId(externalId.equals("") ? null : externalId);
        descriptor.setInternalId(internalId.equals("") ? null : internalId);
        descriptor.setModelType(modelType.equals("") ? null : modelType);
        descriptor.setStorageType(proto.getStorageType());
        descriptor.setCreated(timestampConverter.createFromProto(proto.getCreated()));
        descriptor.setModified(timestampConverter.createFromProto(proto.getModified()));
        descriptor.setDisplay(displayConverter.createFromProto(proto.getDisplay()));

        if (proto.hasVersion()) {
            descriptor.setVersion(proto.getVersion().getValue());
        } else {
            descriptor.setVersion(null);
        }

        return descriptor;
    }
}
