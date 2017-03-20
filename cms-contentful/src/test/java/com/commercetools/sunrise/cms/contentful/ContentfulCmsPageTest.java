package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAEntry;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.commercetools.sunrise.cms.contentful.ContentfulMockUtil.mockEntryWithField;
import static com.commercetools.sunrise.cms.contentful.FieldType.ARRAY;
import static com.commercetools.sunrise.cms.contentful.FieldType.ASSET;
import static com.commercetools.sunrise.cms.contentful.FieldType.BOOLEAN;
import static com.commercetools.sunrise.cms.contentful.FieldType.DATE;
import static com.commercetools.sunrise.cms.contentful.FieldType.INTEGER;
import static com.commercetools.sunrise.cms.contentful.FieldType.LOCATION;
import static com.commercetools.sunrise.cms.contentful.FieldType.NUMBER;
import static com.commercetools.sunrise.cms.contentful.FieldType.SYMBOL;
import static com.commercetools.sunrise.cms.contentful.FieldType.TEXT;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ContentfulCmsPageTest {

    @Test
    public void verifyFieldsOfAllTypesWithDirectStringRepresentation() {
        Map<FieldType, Object> typeToContentMap = new HashMap<>();
        typeToContentMap.put(BOOLEAN, true);
        typeToContentMap.put(DATE, Calendar.getInstance().getTime());
        typeToContentMap.put(INTEGER, 13);
        typeToContentMap.put(NUMBER, 3.14);
        typeToContentMap.put(SYMBOL, "Content of a symbol field");
        typeToContentMap.put(TEXT, "Content of a text field");
        typeToContentMap.put(LOCATION, "{lon=19.62, lat=51.37}");

        typeToContentMap.forEach((fieldType, fieldContent) -> {
            CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", fieldContent, fieldType.type()));

            Optional<String> content = cmsPage.field("aField");

            assertThat(content).hasValue(String.valueOf(fieldContent));
        });
    }

    @Test
    public void whenNoSuchField_returnEmpty() {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", "Content of a field", SYMBOL.type()));

        Optional<String> content = cmsPage.field("notMatchingFieldName");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenFieldTypeIsLinkAsset_returnIt() {
        CDAAsset assetContent = mock(CDAAsset.class);
        when(assetContent.url()).thenReturn("//some.url");
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", assetContent, ASSET.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue("//some.url");
    }

    @Test
    public void whenFieldTypeLinkIsOtherThanAsset_returnEmpty() {
        CDAAsset assetContent = mock(CDAAsset.class);
        when(assetContent.url()).thenReturn("//some.url");
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", assetContent, "foo"));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenAssetHasNoUrl_returnEmpty() {
        CDAAsset assetContent = mock(CDAAsset.class);
        when(assetContent.url()).thenReturn(null);
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", assetContent, ASSET.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenFieldTypeIsArrayOfText_returnIt() {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("textArrayField", createArray("two"), TEXT.type()));

        Optional<String> content = cmsPage.field("textArrayField[1]");

        assertThat(content).hasValue("two");
    }

    @Test
    public void whenFieldTypeIsArrayOfLinkAssetsInsideArrayEntry_returnIt() {
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

}