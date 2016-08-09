package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.contentful.models.FieldTypes;
import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAField;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nullable;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.split;

public class ContentfulCmsPage implements CmsPage {

    private CDAEntry cdaEntry;
    private List<Locale> locales;

    public ContentfulCmsPage(CDAEntry cdaEntry, List<Locale> locales) {
        this.cdaEntry = cdaEntry;
        this.locales = locales;
    }

    @Override
    public Optional<String> field(final String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            return Optional.empty();
        }
        final List<String> allNames = new ArrayList<>(Arrays.asList(split(fieldName, ".")));
        return Optional.ofNullable(getEntryWithContentField(cdaEntry, getEntryNamesList(allNames)))
                .flatMap(entryWithContentField ->
                        getContent(entryWithContentField, getContentFieldName(allNames)));
    }

    private String getContentFieldName(List<String> allNames) {
        return allNames.size() > 1 ? allNames.get(allNames.size() - 1) : allNames.get(0);
    }

    private List<String> getEntryNamesList(List<String> allNames) {
        return allNames.size() > 1 ?
                allNames.subList(0, allNames.size() - 1) : Collections.emptyList();
    }

    private Optional<String> getContent(final CDAEntry entry, final String contentFieldName) {
        return getCdaField(entry, contentFieldName)
                .flatMap(cdaField -> {
                    final Object entryField = entry.getField(contentFieldName);
                    return getContentAccordingToFieldDefinition(entryField, cdaField);
                        });
    }

    private CDAEntry getEntryWithContentField(@Nullable final CDAEntry cdaEntry, final List<String> entryNamesList) {
        if (entryNamesList.isEmpty()) {
            return cdaEntry;
        } else {
            final String key = entryNamesList.get(0);
            if (cdaEntry != null && cdaEntry.rawFields().containsKey(key)) {
                Object item = cdaEntry.getField(key);
                if (item instanceof CDAEntry) {
                    entryNamesList.remove(0);
                    return getEntryWithContentField((CDAEntry) item, entryNamesList);
                }
            }
            return null;
        }
    }

    // CDAField contains information related to field, like it's type
    private Optional<CDAField> getCdaField(final CDAEntry lastLevelEntry, final String fieldName) {
        return lastLevelEntry.contentType().fields()
                .stream()
                .filter(cdaField -> cdaField.id().equals(fieldName)
                        && FieldTypes.ALL_SUPPORTED.contains(cdaField.type()))
                .findFirst();
    }

    private Optional<String> getContentAccordingToFieldDefinition(@Nullable final Object localizedEntryField,
                                                                  final CDAField cdaField) {
        return Optional.ofNullable(localizedEntryField)
                .map(entryField -> {
                    String content = null;
                    if (FieldTypes.CONVERTABLE_TO_STRING.contains(cdaField.type())) {
                        content = String.valueOf(entryField);
                    } else if (cdaField.linkType().equals(FieldTypes.LINK_ASSET)) {
                        content = ((CDAAsset) entryField).url();
                    }
                    return content;
                });
    }

}
