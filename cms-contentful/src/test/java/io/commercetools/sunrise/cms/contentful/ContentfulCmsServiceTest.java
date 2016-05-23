package io.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.SynchronizedSpace;
import io.commercetools.sunrise.cms.CmsIdentifier;
import io.commercetools.sunrise.cms.contentful.ContentfulCmsService;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.TimeUnit;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentfulCmsServiceTest {
    private static final Locale DE_de = Locale.forLanguageTag("de-DE");
    private static final Locale EN_US = Locale.forLanguageTag("en-US");
    private static final List<Locale> SUPPORTED_LOCALES = asList(DE_de, EN_US);

    private static final String SPACE_ID = "spaceId";
    private static final String TOKEN = "token";
    private ContentfulCmsService contentfulCmsService = new ContentfulCmsService(SPACE_ID, TOKEN);

    @Test
    public void TestWenAskForExistingStringContentThenGet() throws Exception {
        SynchronizedSpace mockSynchonizedSpace = mock(SynchronizedSpace.class);
        when(contentfulCmsService.).thenReturn()
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("testBanner", "First banner", "leftTop");
        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isTrue();
        assertThat(content.get()).isEqualTo("[adventure]");
    }

    @Test
    public void whenAskForExistingStringContentThenGet() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField("human", "Finn", "likes");
        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isTrue();
        assertThat(content.get()).isEqualTo("[adventure]");
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