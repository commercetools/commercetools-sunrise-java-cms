package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAField;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.commercetools.sunrise.cms.contentful.models.FieldType.ARRAY;
import static com.commercetools.sunrise.cms.contentful.models.FieldType.toStringStrategy;
import static java.util.Arrays.copyOfRange;
import static org.apache.commons.lang3.StringUtils.split;

public class ContentfulCmsPage implements CmsPage {

    private static final Pattern ARRAY_KEY_PATTERN = Pattern.compile("(.+)\\[(\\d+)\\]$");

    private CDAEntry cdaEntry;
    private List<Locale> locales;

    public ContentfulCmsPage(final CDAEntry cdaEntry, final List<Locale> locales) {
        this.cdaEntry = cdaEntry;
        this.locales = locales;
    }

    @Override
    public Optional<String> field(final String path) {
        if (StringUtils.isEmpty(path)) {
            return Optional.empty();
        }

        final String[] pathSegments = split(path, ".");
        final String fieldKey = pathSegments[pathSegments.length - 1];
        final String[] entryPathSegments = createEntryPathSegments(pathSegments);
        return findEntry(entryPathSegments).flatMap(lastEntry ->
                findContent(lastEntry, fieldKey));
    }

    /**
     * Form an array with last segment skipped which is expected to be a field name.
     *
     * @param pathSegments entire path segments
     * @return path segments of entries only
     */
    private String[] createEntryPathSegments(final String[] pathSegments) {
        return copyOfRange(pathSegments, 0, pathSegments.length - 1);
    }

    /**
     * Traverse contained CDAEntry to match all path segments.
     *
     * @param pathSegments array of all path segments that lead to CDAEntry of interest
     * @return CDAEntry that matches path segments
     */
    private Optional<CDAEntry> findEntry(final String[] pathSegments) {
        CDAEntry entry = cdaEntry;

        for (String key: pathSegments) {
            Object nextEntry = null;

            Matcher arrayMatcher = ARRAY_KEY_PATTERN.matcher(key);

            if (arrayMatcher.find()) {
                nextEntry = getEntryFromArray(entry, arrayMatcher);
            } else if (entry.rawFields().containsKey(key)) {
                nextEntry = entry.getField(key);
            }

            if (nextEntry != null && nextEntry instanceof CDAEntry) {
                entry = (CDAEntry) nextEntry;
            } else {
                return Optional.empty();
            }
        }

        return Optional.of(entry);
    }

    /**
     * Try to get an item from input entry which is supposed to contain an array of fields.
     *
     * After pattern has been matched arrayMatcher contains a regexp group of key and index.
     * E.g. after matching 'key[1]' two groups of 'key' and '1' are contained in matcher.
     *
     * They are later used to find 'key' field in parentEntry and retrieve '1' (second)
     * item from that field which is expected to form an array list.
     *
     * If the process fails null is returned.
     *
     * @param parentEntry should contain expected array field
     * @param arrayMatcher contains key and index groups after pattern has been matched
     * @return matched entry or null
     */
    @Nullable
    private Object getEntryFromArray(final CDAEntry parentEntry, final Matcher arrayMatcher) {
        String arrayEntryKey = arrayMatcher.group(1);
        int index = Integer.parseInt(arrayMatcher.group(2));
        if (parentEntry.rawFields().containsKey(arrayEntryKey)) {
            Object field = parentEntry.getField(arrayEntryKey);
            if (field instanceof List) {
                List list = (List) field;
                if (index < list.size()) {
                    return list.get(index);
                }
            }
        }
        return null;
    }

    /**
     * Extract field from an entry and convert it its string representation.
     *
     * @param entry should contain expected field
     * @param fieldKey id of field to be search inside entry
     * @return string representation of the field
     */
    private Optional<String> findContent(final CDAEntry entry, final String fieldKey) {
        Matcher arrayMatcher = ARRAY_KEY_PATTERN.matcher(fieldKey);
        if (arrayMatcher.find()) {
            String arrayFieldKey = arrayMatcher.group(1);
            return findContentTypeField(entry, arrayFieldKey, true).flatMap(contentTypeField ->
                    getFieldFromArray(entry, arrayMatcher).map(field ->
                            getContentBasedOnType(field, contentTypeField)));
        }
        return findContentTypeField(entry, fieldKey, false).flatMap(contentTypeField ->
                Optional.ofNullable(entry.getField(fieldKey)).map(field ->
                        getContentBasedOnType(field, contentTypeField)));
    }

    /**
     * Try to get a field from input entry which is supposed to contain an array of fields.
     *
     * After pattern has been matched arrayMatcher contains a regexp group of key and index.
     * E.g. after matching 'key[1]' two groups of 'key' and '1' are contained in matcher.
     *
     * They are later used to find 'key' field in entry and retrieve '1' (second)
     * item from that field which is expected to form an array list.
     *
     * If the process fails empty optional object is returned.
     *
     * @param entry should contain expected array field
     * @param arrayMatcher contains key and index groups after pattern has been matched
     * @return matched field or empty optional object
     */
    private Optional<Object> getFieldFromArray(final CDAEntry entry, final Matcher arrayMatcher) {
        String arrayFieldKey = arrayMatcher.group(1);
        int index = Integer.parseInt(arrayMatcher.group(2));
        Object field = entry.getField(arrayFieldKey);
        Object item = null;
        if (field != null && field instanceof List) {
            List list = (List) field;
            if (index < list.size()) {
                item = list.get(index);
            }
        }
        return Optional.ofNullable(item);
    }

    /**
     * Find content type of an entry and validate if it's supported by this implementation.
     */
    private Optional<CDAField> findContentTypeField(final CDAEntry entry, final String fieldKey,
                                                    boolean arrayExpected) {
        return entry.contentType().fields().stream()
                .filter(field -> field.id().equals(fieldKey))
                .filter(field -> arrayExpected == ARRAY.type().equals(field.type()))
                .findAny();
    }

    /**
     * Convert content of the field to String if possible.
     *
     * @param field object to convert to string
     * @param contentType information about field's type
     * @return content of the field in String representation if possible
     */
    private String getContentBasedOnType(final Object field, final CDAField contentType) {
        return toStringStrategy(contentType).apply(field);
    }
}
