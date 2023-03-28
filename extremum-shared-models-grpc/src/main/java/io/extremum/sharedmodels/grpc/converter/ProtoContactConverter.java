package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.personal.Contact;
import io.extremum.sharedmodels.proto.common.ProtoContact;
import org.springframework.stereotype.Service;

@Service
public class ProtoContactConverter {

    public Contact createFromProto(ProtoContact proto) {
        Contact contact = new Contact();
        contact.setType(proto.getType());
        contact.setContact(proto.getContact());
        contact.setPrimary(proto.getPrimary());
        return contact;
    }

    public ProtoContact createProto(Contact contact) {
        return ProtoContact.newBuilder()
                .setContact(contact.getContact())
                .setType(contact.getType())
                .setPrimary(contact.isPrimary())
                .build();
    }
}
