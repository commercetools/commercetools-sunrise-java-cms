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
        final String fieldContent = "2015-11-06T09:45:27";
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, DATE);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(fieldContent);
    }

    @Test
    public void whenEntryFieldTypeIsInteger_thenReturnOptionalString() throws Exception {
        final int fieldContent = 13;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, INTEGER);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(fieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsNumber_thenReturnOptionalString() throws Exception {
        final double fieldContent = 3.14;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, NUMBER);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(fieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsBoolean_thenReturnOptionalString() throws Exception {
        final boolean fieldContent = true;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, BOOLEAN);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(fieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsLinkAsset_thenReturnOptionalString() throws Exception {
        final String fieldContent = "//some.url";
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(fieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, LINK, true);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(fieldContent);
    }

    @Test
    public void whenEntryFieldTypeLinkIsOtherThanAsset_thenReturnOptionalEmpty() throws Exception {
        final String fieldContent = "//some.url";
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(fieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, LINK, false);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).isEmpty();
    }

    @Test
    public void whenAssetHasNoUrl_thenReturnOptionalEmpty() throws Exception {
        final String fieldContent = null;
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(fieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, LINK, true);
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).isEmpty();
    }

    private CDAEntry mockEntry(final String fieldName,
                               final String fieldContent) {
        return mockEntry(fieldName, fieldContent, SYMBOL, false);
    }

    private CDAEntry mockEntry(final String fieldName,
                               final Object fieldContent, final String fieldType) {
        return mockEntry(fieldName, fieldContent, fieldType, false);
    }

    private CDAEntry mockEntry(final String fieldName,
                               final Object fieldContent, final String fieldType,
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
        Map<String, Object> mockRawFields = new HashMap<>();
        when(mockCdaEntry.getField(fieldName)).thenReturn(fieldContent);
        when(mockCdaEntry.rawFields()).thenReturn(mockRawFields);

        return mockCdaEntry;
    }
}