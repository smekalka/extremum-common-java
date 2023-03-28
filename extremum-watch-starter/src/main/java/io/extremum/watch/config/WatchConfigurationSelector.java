package io.extremum.watch.config;

import io.extremum.watch.annotation.EnableWatch;
import io.extremum.watch.config.conditional.BlockingWatchConfiguration;
import io.extremum.watch.config.conditional.KafkaConfiguration;
import io.extremum.watch.config.conditional.ReactiveWatchConfiguration;
import io.extremum.watch.config.conditional.WebSocketConfiguration;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.type.AnnotationMetadata;

public class WatchConfigurationSelector implements ImportSelector {
    @Override
    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        AnnotationAttributes attributes =
                AnnotationAttributes.fromMap(
                        annotationMetadata.getAnnotationAttributes(EnableWatch.class.getName(),
                                false));
        if (attributes != null) {
            boolean reactive = attributes.getBoolean("reactive");
            return reactive ?
                    new String[] {
                            ReactiveWatchConfiguration.class.getName()
                    } :
                    new String[] {
                            BlockingWatchConfiguration.class.getName(),
                            WebSocketConfiguration.class.getName(),
                            KafkaConfiguration.class.getName()
                    };
         } else {
            return new String[0];
        }
    }
}
