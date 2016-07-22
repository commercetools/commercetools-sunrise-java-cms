import io.commercetools.sunrise.cms.CmsPage;
import io.commercetools.sunrise.cms.CmsServiceException;
import io.commercetools.sunrise.cms.contentful.ContentfulCmsService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class ContentfulCmsServiceIT {
    private static final List<Locale> SUPPORTED_LOCALES = asList(Locale.GERMAN, Locale.US);

    // credentials for contentful demo account
    private static final String IT_PREFIX = "CONTENTFUL_";
    private static final String IT_CF_SPACE_ID = IT_PREFIX + "SPACE_ID";
    private static final String IT_CF_TOKEN = IT_PREFIX + "TOKEN";
    private static final String PAGE_TYPE_NAME = "page";
    private static final String PAGE_TYPE_ID_FIELD_NAME = "slug";
    private ContentfulCmsService contentfulCmsService;

    private static String spaceId() {
        return getValueForEnvVar(IT_CF_SPACE_ID);
    }

    private static String token() {
        return getValueForEnvVar(IT_CF_TOKEN);
    }

    @Before
    public void setUp() throws Exception {
        contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), PAGE_TYPE_NAME, PAGE_TYPE_ID_FIELD_NAME);
    }

    @Test
    public void whenCouldNotFetchEntry_thenThrowException() throws Exception {
        final ContentfulCmsService cmsService = ContentfulCmsService.of("", "", PAGE_TYPE_NAME, PAGE_TYPE_ID_FIELD_NAME);

        Throwable thrown = catchThrowable(() -> waitAndGet(cmsService.get("home", SUPPORTED_LOCALES)));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasMessageContaining("Could not fetch content for home");
        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
    }

    @Test
    public void whenAskForExistingStringContentThenGet() throws Exception {
        Optional<CmsPage> content = waitAndGet(contentfulCmsService.get("finn", SUPPORTED_LOCALES));
        assertThat(content).isPresent();

        assertThat(content.get().get("pageContent.description")).contains("Fearless adventurer! Defender of pancakes.");
    }

    @Test
    public void whenAskForNotExistingStringContentThenNotPresent() throws Exception {
        Optional<CmsPage> content = waitAndGet(contentfulCmsService.get("finn", SUPPORTED_LOCALES));
        assertThat(content).isPresent();
        assertThat(content.get().get("pageContent.notExistingField")).isEmpty();
    }

    @Test
    public void whenAskForExistingAssetContentThenGet() throws Exception {
        Optional<CmsPage> content = waitAndGet(contentfulCmsService.get("jacke", SUPPORTED_LOCALES));
        assertThat(content).isPresent();
        assertThat(content.get().getOrEmpty("pageContent.image")).isEqualToIgnoringCase("//images.contentful.com/1d61yybg0mzf/3NtARkX3tYQOaCw2MG86is/95381cd0170fc38092237bdcb6fce4a8/jake.png");
    }

    @Test
    public void whenAskForNotExistingAssetContentThenNotPresent() throws Exception {
        Optional<CmsPage> content = waitAndGet(contentfulCmsService.get("jacke", SUPPORTED_LOCALES));
        assertThat(content).isPresent();
        assertThat(content.get().getOrEmpty("pageContent.notExistingAsset")).isEmpty();

    }

    private <T> T waitAndGet(final CompletionStage<T> stage) throws InterruptedException, ExecutionException, TimeoutException {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    private static String getValueForEnvVar(final String key) {
        return Optional.ofNullable(System.getenv(key))
                .orElseThrow(() -> new RuntimeException(
                        "Missing environment variable " + key + ", please provide the following environment variables for the integration test:\n" +
                                "export " + IT_CF_SPACE_ID + "=\"Your Contentful project key\"\n" +
                                "export " + IT_CF_TOKEN + "=\"Your Contentful authentication token\"\n"));
    }

}