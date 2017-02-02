package com.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAContentType;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAField;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

import static com.commercetools.sunrise.cms.contentful.models.FieldType.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentfulCmsPageTest {
    private static final List<Locale> SUPPORTED_LOCALES = asList(Locale.GERMANY, Locale.US);
    private static final String FIELD_NAME = "leftTop";
    private static final String CONTENT_VALUE = "Content of left top";

    @Test
    public void whenEntryDoesNotHaveRequiredField_thenReturnOptionalEmpty() throws Exception {
        CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, CONTENT_VALUE, SYMBOL.type());
        ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final String notMatchingFieldName = "notMatchingFieldName";
        final Optional<String> content = contentfulCmsPage.field(notMatchingFieldName);

        assertThat(content).isEmpty();
    }

    @Test
    public void whenEntryExistsInSupportedLanguage_thenReturnIt() throws Exception {
        CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, CONTENT_VALUE, SYMBOL.type());
        ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains("Content of left top");
    }

    @Test
    public void whenEntryFieldTypeIsText_thenReturnOptionalString() throws Exception {
        CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, CONTENT_VALUE, SYMBOL.type());
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(CONTENT_VALUE);
    }

    @Test
    public void whenEntryFieldTypeIsDate_thenReturnOptionalString() throws Exception {
        final String fieldContent = "2015-11-06T09:45:27";
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, DATE.type());
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(fieldContent);
    }

    @Test
    public void whenEntryFieldTypeIsInteger_thenReturnOptionalString() throws Exception {
        final int fieldContent = 13;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, INTEGER.type());
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(fieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsNumber_thenReturnOptionalString() throws Exception {
        final double fieldContent = 3.14;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, NUMBER.type());
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(fieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsBoolean_thenReturnOptionalString() throws Exception {
        final boolean fieldContent = true;
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, BOOLEAN.type());
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(String.valueOf(fieldContent));
    }

    @Test
    public void whenEntryFieldTypeIsLinkAsset_thenReturnOptionalString() throws Exception {
        final String fieldContent = "//some.url";
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(fieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, ASSET.type());
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).contains(fieldContent);
    }

    @Test
    public void whenEntryFieldTypeLinkIsOtherThanAsset_thenReturnOptionalEmpty() throws Exception {
        final String fieldContent = "//some.url";
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(fieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, "foo");
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).isEmpty();
    }

    @Test
    public void whenAssetHasNoUrl_thenReturnOptionalEmpty() throws Exception {
        final String fieldContent = null;
        final CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn(fieldContent);
        final CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, mockAsset, ASSET.type());
        final ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);
        final Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content).isEmpty();
    }

    @Test
    public void whenEntryFieldTypeIsLocation_thenReturnOptionalString() throws Exception {
        Object fieldContent = "{lon=19.62158203125, lat=51.37199469960235}";
        CDAEntry mockCdaEntry = mockEntry(FIELD_NAME, fieldContent, LOCATION.type());
        ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, SUPPORTED_LOCALES);

        Optional<String> content = contentfulCmsPage.field(FIELD_NAME);

        assertThat(content.isPresent());
        assertThat(content.get()).isEqualTo("{lon=19.62158203125, lat=51.37199469960235}");
    }

    @Test
    public void whenEntryFieldTypeIsArrayOfText_thenReturnOptionalString() throws Exception {
        CDAEntry mockCdaEntry = mockEntry("textArrayField", createArray("two"), TEXT.type());
        ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaEntry, singletonList(Locale.ENGLISH));

        Optional<String> content = contentfulCmsPage.field("textArrayField[1]");

        assertThat(content.isPresent());
        assertThat(content.get()).isEqualTo("two");
    }

    @Test
    public void whenEntryFieldTypeIsArrayOfLinkAssetsInsideArrayEntry_thenReturnOptionalString() throws Exception {
        CDAAsset mockAsset = mock(CDAAsset.class);
        when(mockAsset.url()).thenReturn("//url");
        CDAEntry mockCdaEntry = mockEntry("assetArrayField", createArray(mockAsset), ASSET.type());
        CDAEntry mockCdaArrayEntry = mockEntry("array", createArray(mockCdaEntry), ARRAY.type());
        ContentfulCmsPage contentfulCmsPage = new ContentfulCmsPage(mockCdaArrayEntry, singletonList(Locale.ENGLISH));

        Optional<String> content = contentfulCmsPage.field("array[1].assetArrayField[1]");

        assertThat(content.isPresent());
        assertThat(content.get()).isEqualTo("//url");
    }

    private ArrayList<Object> createArray(Object object) {
        ArrayList<Object> array = new ArrayList<>();
        array.add(new Object());
        array.add(object);
        array.add(new Object());
        return array;
    }

    private CDAEntry mockEntry(final String fieldName, final Object fieldContent, final String fieldType) {
        CDAEntry mockCdaEntry = mock(CDAEntry.class);
        CDAField mockCdaField = mock(CDAField.class);

        // mock entry type
        CDAContentType mockContentType = mock(CDAContentType.class);
        when(mockCdaEntry.contentType()).thenReturn(mockContentType);
        when(mockCdaField.type()).thenReturn(fieldContent instanceof ArrayList ? ARRAY.type() : fieldType);
        when(mockCdaField.linkType()).thenReturn(fieldType);
        when(mockCdaField.id()).thenReturn(fieldName);
        Map<String, Object> items = new HashMap<>();
        items.put("type", fieldType);
        items.put("linkType", fieldType);
        when(mockCdaField.items()).thenReturn(items);
        when(mockContentType.fields()).thenReturn(Collections.singletonList(mockCdaField));

        // mock field content
        Map<String, Object> mockRawFields = new HashMap<>();
        mockRawFields.put(fieldName, new Object());
        when(mockCdaEntry.getField(fieldName)).thenReturn(fieldContent);
        when(mockCdaEntry.rawFields()).thenReturn(mockRawFields);

        return mockCdaEntry;
    }
}