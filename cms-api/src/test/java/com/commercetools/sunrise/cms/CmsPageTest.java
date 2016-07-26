package com.commercetools.sunrise.cms;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class CmsPageTest {

    @Test
    public void getsMessage() throws Exception {
        final CmsPage cmsPage = (f -> Optional.of("bar"));
        assertThat(cmsPage.fieldOrEmpty(null)).isEqualTo("bar");
    }

    @Test
    public void getsEmptyStringWhenFieldNotFound() throws Exception {
        final CmsPage cmsPage = (f -> Optional.empty());
        assertThat(cmsPage.fieldOrEmpty(null)).isEmpty();
    }
}
