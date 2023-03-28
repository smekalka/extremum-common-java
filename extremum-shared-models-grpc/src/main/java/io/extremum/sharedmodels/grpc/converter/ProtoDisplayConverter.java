package io.extremum.sharedmodels.grpc.converter;

import io.extremum.sharedmodels.basic.StringOrMultilingual;
import io.extremum.sharedmodels.content.Display;
import io.extremum.sharedmodels.content.Display.Type;
import io.extremum.sharedmodels.content.Media;
import io.extremum.sharedmodels.proto.common.ProtoDisplay;
import io.extremum.sharedmodels.proto.common.ProtoDisplay.ProtoType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ProtoDisplayConverter {
    private final ProtoMediaConverter mediaConverter;
    private final ProtoMultilingualConverter multilingualConverter;

    public Display createFromProto(ProtoDisplay proto) {
        if (proto.equals(ProtoDisplay.getDefaultInstance())) {
            return null;
        }

        if (proto.getType().equals(ProtoType.STRING)) {
            return new Display(proto.getStringValue());
        } else if (proto.getType().equals(ProtoType.OBJECT)) {
            Media icon = mediaConverter.createFromProto(proto.getIcon());
            Media splash = mediaConverter.createFromProto(proto.getSplash());

            StringOrMultilingual stringOrMultilingual;
            if (proto.hasMultilingualCaption()) {
                stringOrMultilingual = new StringOrMultilingual(multilingualConverter.createFromProto(proto.getMultilingualCaption()).getMap());
            } else {
                if (proto.getTextCaption().equals("")) {
                    stringOrMultilingual = new StringOrMultilingual();
                } else {
                    stringOrMultilingual = new StringOrMultilingual(proto.getTextCaption());
                }
            }
            return new Display(stringOrMultilingual, icon, splash);
        } else {
            return null;
        }
    }

    public ProtoDisplay createProto(Display display) {
        if (display == null) {
            return ProtoDisplay.getDefaultInstance();
        }
        ProtoDisplay.Builder displayBuilder = ProtoDisplay.newBuilder();
        if (display.getType() == Type.OBJECT) {
            displayBuilder.setType(ProtoType.OBJECT)
                    .setIcon(mediaConverter.createProto(display.getIcon()))
                    .setSplash(mediaConverter.createProto(display.getSplash()));

            StringOrMultilingual caption = display.getCaption();
            if (caption.isMultilingual()) {
                displayBuilder.setMultilingualCaption(multilingualConverter.createProto(caption.getMultilingualContent()));
            } else if (caption.isTextOnly()) {
                displayBuilder.setTextCaption(caption.getText());
            }
        } else if (display.getType() == Type.STRING) {
            displayBuilder.setType(ProtoType.STRING)
                    .setStringValue(display.getStringValue());
        }
        return displayBuilder.build();
    }
}
