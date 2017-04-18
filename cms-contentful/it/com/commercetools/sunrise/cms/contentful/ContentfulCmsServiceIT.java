package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.CmsService;
import org.junit.Test;

import java.util.Locale;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static java.util.concurrent.TimeUnit.SECONDS;
import static org.assertj.core.api.Assertions.assertThat;

public class ContentfulCmsServiceIT {

    private static final String CONTENTFUL_SPACE_ID = "CONTENTFUL_SPACE_ID";
    private static final String CONTENTFUL_TOKEN = "CONTENTFUL_TOKEN";

    @Test
    public void whenAskForExistingStringContent_thenGet() throws Exception {
        CmsService cmsService = ContentfulCmsService.of(spaceId(), token(), "page", "slug", Runnable::run);

        @SuppressWarnings("OptionalGetWithoutIsPresent")
        Optional<CmsPage> page = cmsService.page("finn", singletonList(Locale.GERMANY)).toCompletableFuture().get(5, SECONDS);

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("pageContent.description");
        assertThat(field).hasValue("Fearless Abenteurer! Verteidiger von Pfannkuchen.");
    }

    private static String spaceId() {
        return getEnv(CONTENTFUL_SPACE_ID);
    }

    private static String token() {
        return getEnv(CONTENTFUL_TOKEN);
    }

    private static String getEnv(final String key) {
        final String env = System.getenv(key);
        if (isNull(env)) {
            throw new RuntimeException(
                    "Missing environment variable " + key + ", please provide the following environment variables for the integration test:\n" +
                            "export " + CONTENTFUL_SPACE_ID + "=\"Your Contentful project key\"\n" +
                            "export " + CONTENTFUL_TOKEN + "=\"Your Contentful authentication token\"\n");
        }
        return env;
    }

}