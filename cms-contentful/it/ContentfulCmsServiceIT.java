import io.commercetools.sunrise.cms.CmsIdentifier;
import io.commercetools.sunrise.cms.contentful.ContentfulCmsService;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

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

    @Test
    public void whenAskForExistingStringContentThenGet() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("human", "Finn", "description");
        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isTrue();
        assertThat(content.get()).isEqualTo("Fearless adventurer! Defender of pancakes.");
    }

    @Test
    public void whenAskForNotExistingStringContentThenNotPresent() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("human", "Jake", "likes");
        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isFalse();
    }

    @Test
    public void whenAskForExistingAssetContentThenGet() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("dog", "Jake", "image");
        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isTrue();
        assertThat(content.get()).isEqualTo("//images.contentful.com/cfexampleapi/4hlteQAXS8iS0YCMU6QMWg/2a4d826144f014109364ccf5c891d2dd/jake.png");
    }

    @Test
    public void whenAskForNotExistingAssetContentThenNotPresent() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("dog", "Finn", "image");
        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isFalse();
    }

}