package com.commercetools.sunrise.cms;

import javax.annotation.Nullable;

import static org.apache.commons.lang3.StringUtils.split;

/**
 * CMS Identifier, consisting of the field name and the content type and key.
 *
 * - {@code entryType}: type of entry which usually defines the fields it can contain (e.g. banner)
 * - {@code entryKey}: key that identifies the particular entry (e.g. homeTopLeft)
 * - {@code fieldName}: field name of the required content (e.g. subtitle)
 *
 * @see CmsService
 */
public final class CmsIdentifier {

    private final String entryType;
    private final String entryKey;
    private final String fieldName;

    private CmsIdentifier(final String entryType, final String entryKey, final String fieldName) {
        this.entryType = entryType;
        this.entryKey = entryKey;
        this.fieldName = fieldName;
    }

    public String getEntryType() {
        return entryType;
    }

    public String getEntryKey() {
        return entryKey;
    }

    public String getFieldName() {
        return fieldName;
    }

    /**
     * Creates a CMS Identifier, consisting of the field name and the entry type and key.
     * @param entryWithField of the form {@code entryType:entryKey.fieldName}
     * @return the CMS Identifier for the given input
     */
    public static CmsIdentifier of(final String entryWithField) {
        final String[] parts = split(entryWithField, ":", 2);
        final String contentType = getArrayElement(parts, 0, "");
        final String content = getArrayElement(parts, 1, entryWithField);

        final String[] contentParts = split(content, ".", 2);
        final String contentId = getArrayElement(contentParts, 0, "");
        final String field = getArrayElement(contentParts, 1, "");

        return ofEntryTypeAndKeyAndField(contentType, contentId, field);
    }

    public static CmsIdentifier ofEntryTypeAndKeyAndField(final String entryType, final String entryKey, final String fieldName) {
        return new CmsIdentifier(entryType, entryKey, fieldName);
    }

    /**
     * Given a string array and a particular position, it returns the array element in that position
     * or the default value if the array does not contain an element for that position.
     * @param array the string array from which to get the element
     * @param position the position of the element
     * @param defaultValue the default value returned in case there is no element in that position
     * @return the element from the array in that position, or the default value if there is no element
     */
    @Nullable
    public static String getArrayElement(final String[] array, final int position, final String defaultValue) {
        final boolean containsPosition = array.length > position;
        return containsPosition ? array[position] : defaultValue;
    }

    @Override
    public String toString() {
        return "entry type: '" + this.entryType +
                "', entry key: '" + this.entryKey +
                "', field name: '" + this.fieldName + "'";
    }
}
