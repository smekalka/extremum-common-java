package common.dao.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.TypeAlias;

/**
 * @author rpuch
 */
@NoArgsConstructor
@AllArgsConstructor
@TypeAlias("StringContent")
class AnnotatedContent {
    @Getter
    @Setter
    private String content;
}
