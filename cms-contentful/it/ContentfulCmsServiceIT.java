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

public class ContentfulCmsServiceIT {
    private static final List<Locale> SUPPORTED_LOCALES = asList(Locale.GERMAN, Locale.US);

    // credentials for contentful demo account
    private static final String SPACE_ID = "cfexampleapi";
    private static final String TOKEN = "b4c0n73n7fu1";
    private ContentfulCmsService contentfulCmsService;

    @Before
    public void setUp() throws Exception {
        contentfulCmsService = ContentfulCmsService.of(SPACE_ID, TOKEN);
    }

    @Test(expected=ExecutionException.class)
    public void whenCouldNotFetchEntry_thenReturnOptionalEmpty() throws Exception {
        final ContentfulCmsService cmsService = ContentfulCmsService.of("", "");
        final CmsIdentifier cmsIdentifier = CmsIdentifier.of("entryType:entryKey.fieldName");
        waitAndGet(cmsService.get(SUPPORTED_LOCALES, cmsIdentifier));
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

}