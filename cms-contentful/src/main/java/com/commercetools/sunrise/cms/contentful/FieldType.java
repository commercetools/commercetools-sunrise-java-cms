package com.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAField;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Types of content entries that can be retrieved from Contentful platform that are supported by this implementation.
 * <p>
 * Each type contains corresponding type identifier in Contentful and indicator of whether it has direct string
 * representation i.e. content of that type can be converted to string using its {@link Object#toString()} method.
 * <p>
 * There are two special types that need to be handled differently.
 * <p>
 * The 'Array' content type field indicates that the actual type of items inside the array is contained
 * in the content field's 'items' attribute and is fetched from it. It is then matched against other types and handled
 * accordingly.
 * <p>
 * The 'Asset' type indicates that the string representation of the field is the result of its {@link CDAAsset#url()}
 * method. Moreover, 'Asset' type is supposed to be contained in 'linkType' attribute of content type field, unlike in all other
 * cases where 'type' attribute is used for matching.
 */
enum FieldType {
    BOOLEAN("Boolean", true),
    DATE("Date", true),
    INTEGER("Integer", true),
    NUMBER("Number", true),
    SYMBOL("Symbol", true),
    TEXT("Text", true),
    LOCATION("Location", true),
    ASSET("Asset", false),
    ARRAY("Array", false);

    private static final Set<String> WITH_STRING_REPRESENTATION = Collections.unmodifiableSet(
            Arrays.stream(values())
                    .filter(contentType -> contentType.hasStringRepresentation)
                    .map(FieldType::type)
                    .collect(Collectors.toSet()));

    private final String type;
    private final boolean hasStringRepresentation;

    FieldType(String type, boolean hasStringRepresentation) {
        this.type = type;
        this.hasStringRepresentation = hasStringRepresentation;
    }

    String type() {
        return type;
    }

    static Function<Object, String> toStringStrategy(final CDAField contentType) {
        if (hasStringRepresentation(getType(contentType))) {
            return String::valueOf;
        } else if (isAsset(getLinkType(contentType))) {
            return field -> ((CDAAsset) field).url();
        }
        return field -> null;
    }

    static boolean isArray(final CDAField contentType) {
        return ARRAY.type().equals(contentType.type());
    }

    private static boolean hasStringRepresentation(final String type) {
        return WITH_STRING_REPRESENTATION.contains(type);
    }

    private static String getType(final CDAField contentType) {
        return isArray(contentType) ? castToString(contentType.items().get("type")) : contentType.type();
    }

    private static String getLinkType(final CDAField contentType) {
        return isArray(contentType) ? castToString(contentType.items().get("linkType")) : contentType.linkType();
    }

    private static boolean isAsset(final String linkType) {
        return ASSET.type().equals(linkType);
    }

    private static String castToString(final Object type) {
        return type != null && type instanceof String ? (String) type : null;
    }
}
