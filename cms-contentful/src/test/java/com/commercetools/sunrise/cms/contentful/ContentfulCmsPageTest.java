package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAContentType;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAField;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.commercetools.sunrise.cms.contentful.FieldType.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentfulCmsPageTest {

    @Test
    public void whenEntryDoesNotHaveRequiredField_thenReturnOptionalEmpty() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", "Content of a field", SYMBOL.type()));

        Optional<String> content = cmsPage.field("notMatchingFieldName");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenEntryExistsInSupportedLanguage_thenReturnIt() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", "Content of a field", SYMBOL.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue("Content of a field");
    }

    @Test
    public void whenEntryFieldTypeIsText_thenReturnOptionalString() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", "Content of a field", SYMBOL.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue("Content of a field");
    }

    @Test
    public void whenEntryFieldTypeIsDate_thenReturnOptionalString() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", "2015-11-06T09:45:27", DATE.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue("2015-11-06T09:45:27");
    }

    @Test
    public void whenEntryFieldTypeIsInteger_thenReturnOptionalString() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", 13, INTEGER.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue(String.valueOf(13));
    }

    @Test
    public void whenEntryFieldTypeIsNumber_thenReturnOptionalString() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", 3.14, NUMBER.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue(String.valueOf(3.14));
    }

    @Test
    public void whenEntryFieldTypeIsBoolean_thenReturnOptionalString() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", true, BOOLEAN.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue(String.valueOf(true));
    }

    @Test
    public void whenEntryFieldTypeIsLinkAsset_thenReturnOptionalString() throws Exception {
        CDAAsset assetContent = mock(CDAAsset.class);
        when(assetContent.url()).thenReturn("//some.url");
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", assetContent, ASSET.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue("//some.url");
    }

    @Test
    public void whenEntryFieldTypeLinkIsOtherThanAsset_thenReturnOptionalEmpty() throws Exception {
        CDAAsset assetContent = mock(CDAAsset.class);
        when(assetContent.url()).thenReturn("//some.url");
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", assetContent, "foo"));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenAssetHasNoUrl_thenReturnOptionalEmpty() throws Exception {
        CDAAsset assetContent = mock(CDAAsset.class);
        when(assetContent.url()).thenReturn(null);
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", assetContent, ASSET.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenEntryFieldTypeIsLocation_thenReturnOptionalString() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", "{lon=19.62, lat=51.37}", LOCATION.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue("{lon=19.62, lat=51.37}");
    }

    @Test
    public void whenEntryFieldTypeIsArrayOfText_thenReturnOptionalString() throws Exception {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("textArrayField", createArray("two"), TEXT.type()));

        Optional<String> content = cmsPage.field("textArrayField[1]");

        assertThat(content).hasValue("two");
    }

    @Test
    public void whenEntryFieldTypeIsArrayOfLinkAssetsInsideArrayEntry_thenReturnOptionalString() throws Exception {
        CDAAsset assetContent = mock(CDAAsset.class);
        when(assetContent.url()).thenReturn("//url");
        CDAEntry mockCdaEntry = mockEntryWithField("assetArrayField", createArray(assetContent), ASSET.type());
        CDAEntry mockCdaArrayEntry = mockEntryWithField("array", createArray(mockCdaEntry), ARRAY.type());
        CmsPage cmsPage = new ContentfulCmsPage(mockCdaArrayEntry);

        Optional<String> content = cmsPage.field("array[1].assetArrayField[1]");

        assertThat(content).hasValue("//url");
    }

    // create array list with given object on the second position
    private ArrayList<Object> createArray(Object object) {
        ArrayList<Object> array = new ArrayList<>();
        array.add(new Object());
        array.add(object);
        array.add(new Object());
        return array;
    }

    private CDAEntry mockEntryWithField(String fieldName, Object fieldContent, String fieldType) {
        CDAEntry mockCdaEntry = mock(CDAEntry.class);
        CDAContentType mockContentType = mockContentType(fieldName, fieldContent, fieldType);
        when(mockCdaEntry.contentType()).thenReturn(mockContentType);
        when(mockCdaEntry.getField(fieldName)).thenReturn(fieldContent);
        when(mockCdaEntry.rawFields()).thenReturn(Collections.singletonMap(fieldName, new Object()));
        return mockCdaEntry;
    }

    private CDAContentType mockContentType(String fieldName, Object fieldContent, String fieldType) {
        CDAField mockCdaField = mock(CDAField.class);

        when(mockCdaField.type()).thenReturn(fieldContent instanceof ArrayList ? ARRAY.type() : fieldType);
        when(mockCdaField.linkType()).thenReturn(fieldType);
        when(mockCdaField.id()).thenReturn(fieldName);

        Map<String, Object> items = new HashMap<>();
        items.put("type", fieldType);
        items.put("linkType", fieldType);
        when(mockCdaField.items()).thenReturn(items);

        CDAContentType mockContentType = mock(CDAContentType.class);
        when(mockContentType.fields()).thenReturn(Collections.singletonList(mockCdaField));
        return mockContentType;
    }
}