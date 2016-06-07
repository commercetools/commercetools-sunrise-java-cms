import io.commercetools.sunrise.cms.CmsIdentifier;
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
    private ContentfulCmsService contentfulCmsService;

    private static String spaceId() {
        return getValueForEnvVar(IT_CF_SPACE_ID);
    }

    private static String token() {
        return getValueForEnvVar(IT_CF_TOKEN);
    }

    @Before
    public void setUp() throws Exception {
        contentfulCmsService = ContentfulCmsService.of(spaceId(), token());
    }

    @Test
    public void whenCouldNotFetchEntry_thenThrowException() throws Exception {
        final ContentfulCmsService cmsService = ContentfulCmsService.of("", "");
        final CmsIdentifier cmsIdentifier = CmsIdentifier.of("entryType:entryKey.fieldName");

        Throwable thrown = catchThrowable(() -> waitAndGet(cmsService.get(SUPPORTED_LOCALES, cmsIdentifier)));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasMessageContaining("Unauthorized");
    }

    @Test
    public void whenAskForExistingStringContentThenGet() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("human", "Finn", "description");
        Optional<String> content = waitAndGet(contentfulCmsService.get(SUPPORTED_LOCALES, identifier));

        assertThat(content).contains("Fearless adventurer! Defender of pancakes.");
    }

    @Test
    public void whenAskForNotExistingStringContentThenNotPresent() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("human", "Jake", "likes");
        Optional<String> content = waitAndGet(contentfulCmsService.get(SUPPORTED_LOCALES, identifier));

        assertThat(content).isEmpty();
    }

    @Test
    public void whenAskForExistingAssetContentThenGet() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("dog", "Jake", "image");
        Optional<String> content = waitAndGet(contentfulCmsService.get(SUPPORTED_LOCALES, identifier));

        assertThat(content).contains("//images.contentful.com/cfexampleapi/4hlteQAXS8iS0YCMU6QMWg/2a4d826144f014109364ccf5c891d2dd/jake.png");
    }

    @Test
    public void whenAskForNotExistingAssetContentThenNotPresent() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("dog", "Finn", "image");
        Optional<String> content = waitAndGet(contentfulCmsService.get(SUPPORTED_LOCALES, identifier));

        assertThat(content).isEmpty();
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