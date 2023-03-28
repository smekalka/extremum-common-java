package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.personal.Language;
import io.extremum.sharedmodels.proto.common.ProtoLanguage;
import io.extremum.sharedmodels.proto.common.ProtoLanguage.ProtoLevel;
import org.springframework.stereotype.Service;

@Service
public class ProtoLanguageConverter {

    public Language createFromProto(ProtoLanguage proto) {
        Language language = new Language();
        language.setLanguageTag(proto.getLanguageTag());
        language.setLevel(Language.Level.valueOf(proto.getLevel().name()));
        return language;
    }

    public ProtoLanguage createProto(Language language) {
        return ProtoLanguage.newBuilder()
                .setLanguageTag(language.getLanguageTag())
                .setLevel(ProtoLevel.valueOf(language.getLevel().name()))
                .build();
    }
}
