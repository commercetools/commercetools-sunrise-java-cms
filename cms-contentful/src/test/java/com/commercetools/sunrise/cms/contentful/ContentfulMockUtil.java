package com.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAContentType;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAField;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.commercetools.sunrise.cms.contentful.FieldType.ARRAY;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ContentfulMockUtil {

    static CDAEntry mockEntryWithField(String fieldName, Object fieldContent, String fieldType) {
        CDAEntry mockCdaEntry = mock(CDAEntry.class);
        CDAContentType mockContentType = mockContentType(fieldName, fieldContent, fieldType);
        when(mockCdaEntry.contentType()).thenReturn(mockContentType);
        when(mockCdaEntry.getField(fieldName)).thenReturn(fieldContent);
        when(mockCdaEntry.rawFields()).thenReturn(Collections.singletonMap(fieldName, new Object()));
        return mockCdaEntry;
    }

    static CDAEntry mockEntryWithField(String fieldName, Object fieldContent) {
        return mockEntryWithField(fieldName, fieldContent, null);
    }

    private static CDAContentType mockContentType(String fieldName, Object fieldContent, String fieldType) {
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
