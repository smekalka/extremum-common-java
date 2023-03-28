package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.personal.Name;
import io.extremum.sharedmodels.proto.common.ProtoName;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoNameConverter {
    private final ProtoMultilingualConverter multilingualConverter;

    public Name createFromProto(ProtoName proto) {
        Name name = new Name();

        StringOrMultilingual first;
        if (proto.hasObjectFirst()) {
            first = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getObjectFirst()).getMap());
        } else {
            first = new StringOrMultilingual(proto.getStringFirst());
        }
        name.setFirst(first);

        StringOrMultilingual full;
        if (proto.hasObjectFull()) {
            full = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getObjectFull()).getMap());
        } else {
            full = new StringOrMultilingual(proto.getStringFull());
        }
        name.setFull(full);

        StringOrMultilingual middle;
        if (proto.hasObjectMiddle()) {
            middle = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getObjectMiddle()).getMap());
        } else {
            middle = new StringOrMultilingual(proto.getStringMiddle());
        }
        name.setMiddle(middle);

        StringOrMultilingual last;
        if (proto.hasObjectLast()) {
            last = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getObjectLast()).getMap());
        } else {
            last = new StringOrMultilingual(proto.getStringLast());
        }
        name.setLast(last);

        StringOrMultilingual maiden;
        if (proto.hasObjectMaiden()) {
            maiden = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getObjectMaiden()).getMap());
        } else {
            maiden = new StringOrMultilingual(proto.getStringMaiden());
        }
        name.setMaiden(maiden);

        StringOrMultilingual preferred;
        if (proto.hasObjectPreferred()) {
            preferred = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getObjectPreferred()).getMap());
        } else {
            preferred = new StringOrMultilingual(proto.getStringPreferred());
        }
        name.setPreferred(preferred);

        StringOrMultilingual patronymic;
        if (proto.hasObjectPatronymic()) {
            patronymic = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getObjectPatronymic()).getMap());
        } else {
            patronymic = new StringOrMultilingual(proto.getStringPatronymic());
        }
        name.setPatronymic(patronymic);

        StringOrMultilingual matronymic;
        if (proto.hasObjectMatronymic()) {
            matronymic = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getObjectMatronymic()).getMap());
        } else {
            matronymic = new StringOrMultilingual(proto.getStringMatronymic());
        }
        name.setMatronymic(matronymic);

        return name;
    }

    public ProtoName createProto(Name name) {
        ProtoName.Builder nameBuilder = ProtoName.newBuilder();

        StringOrMultilingual first = name.getFirst();
        StringOrMultilingual middle = name.getMiddle();
        StringOrMultilingual last = name.getLast();
        StringOrMultilingual full = name.getFull();
        StringOrMultilingual preferred = name.getPreferred();
        StringOrMultilingual maiden = name.getMaiden();
        StringOrMultilingual matronymic = name.getMatronymic();
        StringOrMultilingual patronymic = name.getPatronymic();

        if (first.isTextOnly()) {
            nameBuilder.setStringFirst(first.getText());
        } else if (first.isMultilingual()) {
            nameBuilder.setObjectFirst(multilingualConverter.createProto(first.getMultilingualContent()));
        }

        if (middle.isTextOnly()) {
            nameBuilder.setStringMiddle(middle.getText());
        } else if (middle.isMultilingual()) {
            nameBuilder.setObjectMiddle(multilingualConverter.createProto(middle.getMultilingualContent()));
        }

        if (last.isTextOnly()) {
            nameBuilder.setStringLast(last.getText());
        } else if (last.isMultilingual()) {
            nameBuilder.setObjectLast(multilingualConverter.createProto(last.getMultilingualContent()));
        }

        if (full.isTextOnly()) {
            nameBuilder.setStringFull(full.getText());
        } else if (full.isMultilingual()) {
            nameBuilder.setObjectFull(multilingualConverter.createProto(full.getMultilingualContent()));
        }

        if (preferred.isTextOnly()) {
            nameBuilder.setStringPreferred(preferred.getText());
        } else if (preferred.isMultilingual()) {
            nameBuilder.setObjectPreferred(multilingualConverter.createProto(preferred.getMultilingualContent()));
        }

        if (maiden.isTextOnly()) {
            nameBuilder.setStringMaiden(maiden.getText());
        } else if (maiden.isMultilingual()) {
            nameBuilder.setObjectMaiden(multilingualConverter.createProto(maiden.getMultilingualContent()));
        }

        if (patronymic.isTextOnly()) {
            nameBuilder.setStringPatronymic(patronymic.getText());
        } else if (patronymic.isMultilingual()) {
            nameBuilder.setObjectPatronymic(multilingualConverter.createProto(patronymic.getMultilingualContent()));
        }

        if (matronymic.isTextOnly()) {
            nameBuilder.setStringMatronymic(matronymic.getText());
        } else if (matronymic.isMultilingual()) {
            nameBuilder.setObjectMatronymic(multilingualConverter.createProto(matronymic.getMultilingualContent()));
        }
        return nameBuilder.build();
    }
}
