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
    public void whenBlankPath_returnEmpty() {
        ContentfulCmsPage page = new ContentfulCmsPage(null);

        assertThat(page.field(null)).isNotPresent();
        assertThat(page.field("")).isNotPresent();
        assertThat(page.field(" ")).isNotPresent();
    }

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
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", mockAsset("//some.url"), ASSET.type()));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).hasValue("//some.url");
    }

    @Test
    public void whenFieldTypeLinkIsOtherThanAsset_returnEmpty() {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", mockAsset("//some.url"), "foo"));

        Optional<String> content = cmsPage.field("aField");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenAssetHasNoUrl_returnEmpty() {
        CmsPage cmsPage = new ContentfulCmsPage(mockEntryWithField("aField", mockAsset(null), ASSET.type()));

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
        CDAEntry mockCdaEntry = mockEntryWithField("assetArrayField", createArray(mockAsset("//url")), ASSET.type());
        CDAEntry mockCdaArrayEntry = mockEntryWithField("array", createArray(mockCdaEntry), ARRAY.type());
        CmsPage cmsPage = new ContentfulCmsPage(mockCdaArrayEntry);

        Optional<String> content = cmsPage.field("array[1].assetArrayField[1]");

        assertThat(content).hasValue("//url");
    }

    @Test
    public void whenExpectedArrayEntryButNotMatchedName_returnEmpty() {
        CDAEntry mockCdaEntry = mockEntryWithField("aField", "true", BOOLEAN.type());
        CmsPage cmsPage = new ContentfulCmsPage(mockCdaEntry);

        Optional<String> content = cmsPage.field("array[1].assetArrayField[1]");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenExpectedArrayEntryButNotMatchedAsList_returnEmpty() {
        CDAEntry mockCdaEntry = mockEntryWithField("aField", "true", BOOLEAN.type());
        CDAEntry mockCdaArrayEntry = mockEntryWithField("notArray", mockCdaEntry);
        CmsPage cmsPage = new ContentfulCmsPage(mockCdaArrayEntry);

        Optional<String> content = cmsPage.field("notArray[1].aField[1]");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenExpectedArrayFieldButNotMatchedAsArray_returnEmpty() {
        CDAEntry mockCdaEntry = mockEntryWithField("aField", "text", TEXT.type());
        CDAEntry mockCdaArrayEntry = mockEntryWithField("first", mockCdaEntry);
        CmsPage cmsPage = new ContentfulCmsPage(mockCdaArrayEntry);

        Optional<String> content = cmsPage.field("first.aField[1]");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenExpectedFieldInArrayButNotMatchedAsActualList_returnEmpty() {
        CDAEntry mockCdaEntry = mockEntryWithField("notArrayField", "text", ARRAY.type());
        CDAEntry mockCdaArrayEntry = mockEntryWithField("aField", mockCdaEntry);
        CmsPage cmsPage = new ContentfulCmsPage(mockCdaArrayEntry);

        Optional<String> content = cmsPage.field("aField.notArrayField[1]");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenExpectedFieldInArrayButExceedingSize_returnEmpty() {
        CDAEntry mockCdaEntry = mockEntryWithField("array", createArray("text"), TEXT.type());
        CDAEntry mockCdaArrayEntry = mockEntryWithField("aField", mockCdaEntry);
        CmsPage cmsPage = new ContentfulCmsPage(mockCdaArrayEntry);

        Optional<String> content = cmsPage.field("aField.array[3]");

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenManySimpleSegmentsPresent_returnIt() {
        CDAEntry fourth = mockEntryWithField("fourth", "true", BOOLEAN.type());
        CDAEntry third = mockEntryWithField("third", fourth);
        CDAEntry second = mockEntryWithField("second", third);
        CDAEntry first = mockEntryWithField("first", second);
        CmsPage cmsPage = new ContentfulCmsPage(first);

        Optional<String> content = cmsPage.field("first.second.third.fourth");

        assertThat(content).hasValue("true");
    }

    @Test
    public void whenManySegmentsPresent_returnIt() {
        CDAEntry fourth = mockEntryWithField("fourth", createArray("text", "text2"), TEXT.type());
        CDAEntry third = mockEntryWithField("third", createArray(fourth, "text3"), TEXT.type());
        CDAEntry second = mockEntryWithField("second", third);
        CDAEntry first = mockEntryWithField("first", second);
        CmsPage cmsPage = new ContentfulCmsPage(first);

        Optional<String> content = cmsPage.field("first.second.third[1].fourth[1]");
        assertThat(content).hasValue("text");
        Optional<String> content1 = cmsPage.field("first.second.third[1].fourth[2]");
        assertThat(content1).hasValue("text2");
        Optional<String> content2 = cmsPage.field("first.second.third[2]");
        assertThat(content2).hasValue("text3");
    }

    @Test
    public void whenIncorrectSegments_returnEmpty() {
        CDAEntry fourth = mockEntryWithField("fourth", createArray("text", "text2"), TEXT.type());
        CDAEntry third = mockEntryWithField("third", createArray(fourth, "text3"), TEXT.type());
        CDAEntry second = mockEntryWithField("second", third);
        CDAEntry first = mockEntryWithField("first", second);
        CmsPage cmsPage = new ContentfulCmsPage(first);

        Optional<String> content = cmsPage.field("first.second.third[1].frth[1]");
        assertThat(content).isNotPresent();
        Optional<String> content1 = cmsPage.field("first.second.thrd[1].fourth[2]");
        assertThat(content1).isNotPresent();
        Optional<String> content2 = cmsPage.field("fist.second.third[2]");
        assertThat(content2).isNotPresent();
    }

    private CDAAsset mockAsset(final String value) {
        CDAAsset assetContent = mock(CDAAsset.class);
        when(assetContent.url()).thenReturn(value);
        return assetContent;
    }

    // create array list with given object on the second position
    private ArrayList<Object> createArray(Object object) {
        return createArray(object, new Object());
    }

    // create array list with given objects on the second and third position
    private ArrayList<Object> createArray(Object object1, Object object2) {
        ArrayList<Object> array = new ArrayList<>();
        array.add(new Object());
        array.add(object1);
        array.add(object2);
        return array;
    }

}