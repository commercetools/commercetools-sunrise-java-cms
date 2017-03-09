package com.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAField;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import static com.commercetools.sunrise.cms.contentful.FieldType.ARRAY;
import static com.commercetools.sunrise.cms.contentful.FieldType.ASSET;
import static com.commercetools.sunrise.cms.contentful.FieldType.BOOLEAN;
import static com.commercetools.sunrise.cms.contentful.FieldType.DATE;
import static com.commercetools.sunrise.cms.contentful.FieldType.INTEGER;
import static com.commercetools.sunrise.cms.contentful.FieldType.LOCATION;
import static com.commercetools.sunrise.cms.contentful.FieldType.NUMBER;
import static com.commercetools.sunrise.cms.contentful.FieldType.SYMBOL;
import static com.commercetools.sunrise.cms.contentful.FieldType.TEXT;
import static java.util.Collections.singletonMap;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class FieldTypeTest {

    private static final List<FieldType> stringFieldTypes = Arrays.asList(BOOLEAN, DATE, INTEGER, NUMBER, SYMBOL, TEXT, LOCATION);

    @Test
    public void toStringStrategy_stringFieldTypes() {
        stringFieldTypes.forEach(fieldType -> {
            String toStringResult = "toString() result";

            CDAField mockField = mock(CDAField.class);
            when(mockField.toString()).thenReturn(toStringResult);
            when(mockField.type()).thenReturn(fieldType.type());

            Function<Object, String> stringStrategy = FieldType.toStringStrategy(mockField);

            assertThat(stringStrategy.apply(mockField)).isEqualTo(toStringResult);
        });
    }

    @Test
    public void toStringStrategy_null() throws Exception {
        Function<Object, String> nullStringStrategy = FieldType.toStringStrategy(mock(CDAField.class));
        assertThat(nullStringStrategy.apply(new Object())).isNull();
    }

    @Test
    public void toStringStrategy_asset() {
        CDAField mockContentType = mock(CDAField.class);
        when(mockContentType.linkType()).thenReturn(ASSET.type());

        Function<Object, String> stringStrategy = FieldType.toStringStrategy(mockContentType);

        CDAAsset mockField = mock(CDAAsset.class);
        String urlResult = "//url";
        when(mockField.url()).thenReturn(urlResult);
        assertThat(stringStrategy.apply(mockField)).isEqualTo(urlResult);
    }

    @Test
    public void toStringStrategy_array_stringFieldTypes() {
        stringFieldTypes.forEach(fieldType -> {
            String toStringResult = "toString() result";

            CDAField mockField = mock(CDAField.class);
            when(mockField.toString()).thenReturn(toStringResult);
            when(mockField.type()).thenReturn(ARRAY.type());
            when(mockField.items()).thenReturn(singletonMap("type", fieldType.type()));

            Function<Object, String> stringStrategy = FieldType.toStringStrategy(mockField);

            assertThat(stringStrategy.apply(mockField)).isEqualTo(toStringResult);
        });
    }

    @Test
    public void toStringStrategy_array_asset() throws Exception {
        CDAField mockContentType = mock(CDAField.class);
        when(mockContentType.type()).thenReturn(ARRAY.type());
        when(mockContentType.items()).thenReturn(singletonMap("linkType", ASSET.type()));

        Function<Object, String> stringStrategy = FieldType.toStringStrategy(mockContentType);

        CDAAsset mockField = mock(CDAAsset.class);
        String urlResult = "//url";
        when(mockField.url()).thenReturn(urlResult);
        assertThat(stringStrategy.apply(mockField)).isEqualTo(urlResult);
    }

    @Test
    public void isArray() {
        CDAField mockField = mock(CDAField.class);

        when(mockField.type()).thenReturn("Array");
        assertThat(FieldType.isArray(mockField)).isTrue();

        when(mockField.type()).thenReturn("Boolean");
        assertThat(FieldType.isArray(mockField)).isFalse();
    }
}