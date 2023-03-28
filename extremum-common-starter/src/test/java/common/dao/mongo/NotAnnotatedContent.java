package common.dao.mongo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * @author rpuch
 */
@NoArgsConstructor
@AllArgsConstructor
class NotAnnotatedContent {
    @Getter
    @Setter
    private String content;
}
