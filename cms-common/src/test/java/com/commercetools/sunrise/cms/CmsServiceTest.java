package com.commercetools.sunrise.cms;

import org.junit.Test;

import java.util.Optional;

import static java.util.concurrent.CompletableFuture.completedFuture;
import static org.assertj.core.api.Assertions.assertThat;

public class CmsServiceTest {

    @Test
    public void getsMessage() throws Exception {
        final CmsPage cmsPage = fieldName -> null;
        final CmsService cmsService = ((k, a) -> completedFuture(Optional.of(cmsPage)));
        assertThat(cmsService.get("anything", null).toCompletableFuture().join()).contains(cmsPage);
    }

    @Test
    public void getsEmptyStringWhenKeyNotFound() throws Exception {
        final CmsService cmsService = ((k, a) -> completedFuture(Optional.empty()));
        assertThat(cmsService.get("", null).toCompletableFuture().join()).isEmpty();
    }
}
