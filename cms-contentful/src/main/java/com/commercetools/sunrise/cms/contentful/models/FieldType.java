package com.commercetools.sunrise.cms.contentful.models;

import com.contentful.java.cda.CDAField;

import java.util.Arrays;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

public enum FieldType {
    BOOLEAN("Boolean", true),
    DATE("Date", true),
    INTEGER("Integer", true),
    NUMBER("Number", true),
    SYMBOL("Symbol", true),
    TEXT("Text", true),
    LOCATION("Location", true),
    LINK("Link", false),
    ASSET("Asset", false),
    ARRAY("Array", false);

    private static final Set<String> WITH_STRING_REPRESENTATION = Collections.unmodifiableSet(
            Arrays.stream(values())
                    .filter(contentType -> contentType.hasStringRepresentation)
                    .map(FieldType::getType)
                    .collect(Collectors.toSet()));

    private static final Set<String> SUPPORTED_TYPES = Collections.unmodifiableSet(
            Arrays.stream(values())
                    .map(FieldType::getType)
                    .collect(Collectors.toSet()));

    private final String type;
    private final boolean hasStringRepresentation;

    FieldType(String type, boolean hasStringRepresentation) {
        this.type = type;
        this.hasStringRepresentation = hasStringRepresentation;
    }

    public String getType() {
        return type;
    }

    public static boolean isSupported(String type) {
        return SUPPORTED_TYPES.contains(type);
    }

    public static boolean hasStringRepresentation(final CDAField contentType) {
        if (WITH_STRING_REPRESENTATION.contains(contentType.type())) {
            return true;
        }
        if (ARRAY.getType().equals(contentType.type())) {
            Object type = contentType.items().get("type");
            if (type != null && type instanceof String && WITH_STRING_REPRESENTATION.contains(type)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAsset(final CDAField contentType) {
        if (ASSET.getType().equals(contentType.linkType())) {
            return true;
        }
        if (ARRAY.getType().equals(contentType.type())) {
            Object linkType = contentType.items().get("linkType");
            if (Objects.equals(linkType, ASSET.getType())) {
                return true;
            }
        }
        return false;
    }
}
