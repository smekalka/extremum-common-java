package io.extremum.everything.services.management;

import io.extremum.sharedmodels.dto.RequestDto;
import org.junit.jupiter.api.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

/**
 * @author rpuch
 */
class NullPatcherHooksTest {
    private final NullPatcherHooks hooks = new NullPatcherHooks();

    @Test
    void whenCallingAfterPatchApplied_thenTheDtoPassedAsAnArgumentShouldBeReturned() {
        RequestDto dto = mock(RequestDto.class);

        RequestDto result = hooks.afterPatchAppliedToDto(null, dto);

        assertThat(result, is(sameInstance(dto)));
    }
}