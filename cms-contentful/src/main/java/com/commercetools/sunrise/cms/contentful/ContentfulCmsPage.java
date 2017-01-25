package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.contentful.models.FieldTypes;
import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAField;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * Skip last segment which is expected to be a field name.
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
                String arrayKey = arrayMatcher.group(1);
                int index = Integer.parseInt(arrayMatcher.group(2));
                nextEntry = getEntryFromArray(entry, arrayKey, index);
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

    private Object getEntryFromArray(CDAEntry parentEntry, String arrayFieldKey, int index) {
        if (parentEntry.rawFields().containsKey(arrayFieldKey)) {
            Object field = parentEntry.getField(arrayFieldKey);
            if (field instanceof ArrayList) {
                ArrayList arrayList = (ArrayList) field;
                if (index < arrayList.size()) {
                    return arrayList.get(index);
                }
            }
        }
        return null;
    }

    private Optional<String> findContent(final CDAEntry entry, final String fieldKey) {
        return getField(entry, fieldKey).flatMap(field ->
                findContentTypeField(entry, fieldKey).flatMap(contentTypeField ->
                        getContentBasedOnType(field, contentTypeField)));
    }

    private Optional<Object> getField(final CDAEntry entry, final String fieldKey) {
        return Optional.ofNullable(entry.getField(fieldKey));
    }

    private Optional<CDAField> findContentTypeField(final CDAEntry entry, final String fieldKey) {
        return entry.contentType().fields().stream()
                .filter(field -> field.id().equals(fieldKey) && FieldTypes.ALL_SUPPORTED.contains(field.type()))
                .findAny();
    }

    /**
     * Convert content of the field to String if possible.
     *
     * @param entryField object to convert to string
     * @param contentTypeField information about object's type
     * @return content of the field in String representation if possible
     */
    private Optional<String> getContentBasedOnType(final Object entryField, final CDAField contentTypeField) {
        if (FieldTypes.CONVERTABLE_TO_STRING.contains(contentTypeField.type())) {
            return Optional.of(String.valueOf(entryField));
        } else if (FieldTypes.LINK_ASSET.equals(contentTypeField.linkType())) {
            return Optional.ofNullable(((CDAAsset) entryField).url());
        }
        return Optional.empty();
    }

}
