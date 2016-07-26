package com.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAContentType;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAField;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static com.commercetools.sunrise.cms.contentful.models.FieldTypes.*;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentfulCmsPageTest {
    private static final List<Locale> SUPPORTED_LOCALES = asList(Locale.GERMANY, Locale.US);
    private static final String FIELD_NAME = "leftTop";
    private static final String CONTENT_VALUE = "Content of left top";
    private final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, CONTENT_VALUE);
    private ContentfulCmsPage contentfulCmsPage;

    @Before
    public void setUp() throws Exception {
        contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
    }

    @Test
    public void whenLanguageIsNotSupported_thenReturnOptionalEmpty() throws Exception {
        final Locale deAt = Locale.forLanguageTag("de-AT");
        final List<Locale> supportedLocales = Collections.singletonList(deAt);
        final ContentfulCmsPage contentfulCmsPage= new ContentfulCmsPage(mockCdaEntry, supportedLocales);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).isEmpty();
    }

    @Test
    public void whenEntryDoesNotHaveRequiredField_thenReturnOptionalEmpty() throws Exception {
        final String notMatchingFieldName = "notMatchingFieldName";
        final Optional<String> content = contentfulCmsPage.field(notMatchingFieldName);

        assertThat(content).isEmpty();
    }

    @Test
    public void whenEntryExistsInSupportedLanguage_thenReturnIt() throws Exception {
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains("Content of left top");
    }

    @Test
    public void whenEntryFieldTypeIsText_thenReturnOptionalString() throws Exception {
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, CONTENT_VALUE, TEXT);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(CONTENT_VALUE);
    }

    @Test
    public void whenEntryFieldTypeIsDate_thenReturnOptionalString() throws Exception {
        final String localizedFieldContent = "2015-11-06T09:45:27";
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, localizedFieldContent, DATE);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(localizedFieldContent);
    }

    @Test
    public void whenEntryFieldTypeIsInteger_thenReturnOptionalString() throws Exception {
        final int localizedFieldContent = 13;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, localizedFieldContent, INTEGER);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(localizedFieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsNumber_thenReturnOptionalString() throws Exception {
        final double localizedFieldContent = 3.14;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, localizedFieldContent, NUMBER);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(localizedFieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsBoolean_thenReturnOptionalString() throws Exception {
        final boolean localizedFieldContent = true;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, localizedFieldContent, BOOLEAN);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(localizedFieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsLinkAsset_thenReturnOptionalString() throws Exception {
        final String localizedFieldContent = "//some.url";
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(localizedFieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, LINK, true);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(localizedFieldContent);
    }

    @Test
    public void whenEntryFieldTypeLinkIsOtherThanAsset_thenReturnOptionalEmpty() throws Exception {
        final String localizedFieldContent = "//some.url";
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(localizedFieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, LINK, false);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).isEmpty();
    }

    @Test
    public void whenAssetHasNoUrl_thenReturnOptionalEmpty() throws Exception {
        final String localizedFieldContent = null;
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(localizedFieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, LINK, true);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).isEmpty();
    }

    private CDAEntry mockEntry(final String fieldName,
                               final String localizedFieldContent) {
        return mockEntry(fieldName, localizedFieldContent, SYMBOL, false);
    }

    private CDAEntry mockEntry(final String fieldName,
                               final Object localizedFieldContent, final String fieldType) {
        return mockEntry(fieldName, localizedFieldContent, fieldType, false);
    }

    private CDAEntry mockEntry(final String fieldName,
                               final Object localizedFieldContent, final String fieldType,
                               final Boolean isLinkedAsset) {
        CDAEntry mockCdaEntry = mock(CDAEntry.class);
        CDAField mockCdaField = mock(CDAField.class);

        // mock entry type
        CDAContentType mockContentType = mock(CDAContentType.class);
        when(mockCdaEntry.contentType()).thenReturn(mockContentType);
        when(mockCdaField.type()).thenReturn(fieldType);
        when(mockCdaField.linkType()).thenReturn(isLinkedAsset ? LINK_ASSET : "otherLinkedType");
        when(mockCdaField.id()).thenReturn(FIELD_NAME);
        when(mockContentType.fields()).thenReturn(Collections.singletonList(mockCdaField));

        // mock field content
        Map<String, Object> mockFields = new HashMap<>();
        Map<String, Object> mockRawFields = new HashMap<>();
        mockFields.put(Locale.GERMANY.toLanguageTag(), localizedFieldContent);
        when(mockCdaEntry.getField(fieldName)).thenReturn(localizedFieldContent);
        mockRawFields.put(fieldName, mockFields);
        when(mockCdaEntry.rawFields()).thenReturn(mockRawFields);

        return mockCdaEntry;
    }
}