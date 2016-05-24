package io.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAContentType;
import com.contentful.java.cda.CDAEntry;
import io.commercetools.sunrise.cms.CmsIdentifier;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
    private static final String ENTRY_TYPE = "testBanner";
    private static final String ENTRY_KEY = "firstBanner";
    private static final String FIELD_NAME = "leftTop";
    private static final String ENTRY_ID = "firstEntryId";
    private static final String CONTENT_VALUE = "Content of left top";
    private CDAEntry mockCdaEntry = mockEntry(ENTRY_TYPE, ENTRY_KEY, FIELD_NAME, CONTENT_VALUE);
    private ContentfulCmsService contentfulCmsService;

    @Before
    public void setUp() throws Exception {
        Map<String, CDAEntry> entryMap = new HashMap<>();
        entryMap.put(ENTRY_ID, mockCdaEntry);
        contentfulCmsService = new ContentfulCmsService(entryMap);
    }

    // TODO test assets
    @Test
    public void whenSearchedContentExists_thenReturnIt() throws Exception {
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField(ENTRY_TYPE, ENTRY_KEY, FIELD_NAME);

        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isTrue();
        assertThat(content.get()).isEqualTo(CONTENT_VALUE);
    }

    @Test
    public void whenLanguageIsNotSupported_thenReturnOptionalEmpty() throws Exception {
        final Locale deAt = Locale.forLanguageTag("de-AT");
        List<Locale> supportedLocales = Collections.singletonList(deAt);
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField(ENTRY_TYPE, ENTRY_KEY, FIELD_NAME);

        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(supportedLocales, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isFalse();
    }

    @Test
    public void whenThereIsNoMatchingFieldName_thenReturnOptionalEmpty() throws Exception {
        final String notMatchingFieldName = "notMatchingFieldName";
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField(ENTRY_TYPE, ENTRY_KEY, notMatchingFieldName);

        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isFalse();
    }

    @Test
    public void whenThereIsNoMatchingEntryKey_thenReturnOptionalEmpty() throws Exception {
        final String notMatchingEntryKey = "notMatchingEntryKey";
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField(ENTRY_TYPE, notMatchingEntryKey, FIELD_NAME);

        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isFalse();
    }

    @Test
    public void whenThereIsNoMatchingEntryType_thenReturnOptionalEmpty() throws Exception {
        final String notMatchingEntryType = "notMatchingEntryType";
        CmsIdentifier identifier = CmsIdentifier.ofEntryTypeAndKeyAndField(notMatchingEntryType, ENTRY_KEY, FIELD_NAME);

        CompletionStage<Optional<String>> optionalCompletionStage = contentfulCmsService.get(SUPPORTED_LOCALES, identifier);
        Optional<String> content = optionalCompletionStage.toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content.isPresent()).isFalse();
    }

    private CDAEntry mockEntry(String entryType, String entryKey, String fieldName, String localizedFieldContent) {
        CDAEntry mockCdaEntry = mock(CDAEntry.class);

        // mock entry type
        CDAContentType mockContentType = mock(CDAContentType.class);
        when(mockContentType.id()).thenReturn(entryType);
        when(mockCdaEntry.contentType()).thenReturn(mockContentType);

        // mock entry key
        when(mockCdaEntry.getField(ContentfulCmsService.ENTRY_KEY)).thenReturn(entryKey);

        // mock field content
        Map<String, String> mockFields = new HashMap<>();
        Map<String, Object> mockRawFields = new HashMap<>();
        mockFields.put(DE_de.toLanguageTag(), localizedFieldContent);
        when(mockCdaEntry.getField(fieldName)).thenReturn(localizedFieldContent);
        mockRawFields.put(fieldName, mockFields);
        when(mockCdaEntry.rawFields()).thenReturn(mockRawFields);

        return mockCdaEntry;
    }
}