package io.commercetools.sunrise.cms.contentful.models;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public final class FieldTypes {
    public static final String BOOLEAN = "Boolean";
    public static final String DATE = "Date";
    public static final String INTEGER = "Integer";
    public static final String LINK = "Link";
    public static final String NUMBER = "Number";
    public static final String SYMBOL = "Symbol";
    public static final String TEXT = "Text";
    public static final String LINK_ASSET = "Asset";
    public static final List<String> CONVERTABLE_TO_STRING = Collections.unmodifiableList(
            Arrays.asList(BOOLEAN, DATE, INTEGER, NUMBER, SYMBOL, TEXT));
    public static final List<String> ALL_SUPPORTED = Collections.unmodifiableList(
            new ArrayList<String>(CONVERTABLE_TO_STRING) {{
                add(LINK);
            }});

    private FieldTypes() {
        throw new AssertionError();
    }
}