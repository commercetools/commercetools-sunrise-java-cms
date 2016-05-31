package io.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAContentType;
import com.contentful.java.cda.CDAEntry;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentfulCmsServiceTest {
    private static final List<Locale> SUPPORTED_LOCALES = asList(Locale.GERMANY, Locale.US);
    private static final String ENTRY_TYPE = "testBanner";
    private static final String ENTRY_KEY = "firstBanner";
    private static final String FIELD_NAME = "leftTop";
    private static final String CONTENT_VALUE = "Content of left top";
    private final CDAEntry mockCdaEntry = mockEntry(ENTRY_TYPE, ENTRY_KEY, FIELD_NAME, CONTENT_VALUE);
    private ContentfulCmsService contentfulCmsService;

    @Before
    public void setUp() throws Exception {
        CDAClient cdaClient = mock(CDAClient.class);
        contentfulCmsService = new ContentfulCmsService(cdaClient);
    }

    // TODO test assets
    @Test
    public void whenSearchedContentExists_thenReturnIt() throws Exception {
        final Optional<String> content = contentfulCmsService.getLocalizedField(SUPPORTED_LOCALES, mockCdaEntry, FIELD_NAME);

        assertThat(content).isPresent();
        assertThat(content.get()).isEqualTo(CONTENT_VALUE);
    }

    @Test
    public void whenLanguageIsNotSupported_thenReturnOptionalEmpty() throws Exception {
        final Locale deAt = Locale.forLanguageTag("de-AT");
        final List<Locale> supportedLocales = Collections.singletonList(deAt);
        final Optional<String> content = contentfulCmsService.getLocalizedField(supportedLocales, mockCdaEntry, FIELD_NAME);

        assertThat(content).isEmpty();
    }

    @Test
    public void whenThereIsNoMatchingFieldName_thenReturnOptionalEmpty() throws Exception {
        final String notMatchingFieldName = "notMatchingFieldName";
        final Optional<String> content = contentfulCmsService.getLocalizedField(SUPPORTED_LOCALES, mockCdaEntry, notMatchingFieldName);

        assertThat(content).isEmpty();
    }

    private CDAEntry mockEntry(String entryType, String entryKey, String fieldName, String localizedFieldContent) {
        final String entryTitle = "name";
        CDAEntry mockCdaEntry = mock(CDAEntry.class);

        // mock entry type
        CDAContentType mockContentType = mock(CDAContentType.class);
        when(mockContentType.id()).thenReturn(entryType);
        when(mockContentType.displayField()).thenReturn(entryTitle);
        when(mockCdaEntry.contentType()).thenReturn(mockContentType);

        // mock entry key
        when(mockCdaEntry.getField(entryTitle)).thenReturn(entryKey);

        // mock field content
        Map<String, String> mockFields = new HashMap<>();
        Map<String, Object> mockRawFields = new HashMap<>();
        mockFields.put(Locale.GERMANY.toLanguageTag(), localizedFieldContent);
        when(mockCdaEntry.getField(fieldName)).thenReturn(localizedFieldContent);
        mockRawFields.put(fieldName, mockFields);
        when(mockCdaEntry.rawFields()).thenReturn(mockRawFields);

        return mockCdaEntry;
    }
}