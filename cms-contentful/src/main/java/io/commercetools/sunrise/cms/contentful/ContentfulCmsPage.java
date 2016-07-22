package io.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAField;
import io.commercetools.sunrise.cms.CmsPage;
import io.commercetools.sunrise.cms.contentful.models.FieldTypes;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

import static org.apache.commons.lang3.StringUtils.split;

public class ContentfulCmsPage implements CmsPage  {

    private CDAEntry cdaEntry;
    private List<Locale> locales;

    public ContentfulCmsPage(CDAEntry cdaEntry, List<Locale> locales) {
        this.cdaEntry = cdaEntry;
        this.locales = locales;
    }

    @Override
    public Optional<String> get(final String fieldName) {
        if (StringUtils.isEmpty(fieldName)) {
            return Optional.empty();
        }
        final FieldIdentifier fieldIdentifier = new FieldIdentifier(fieldName);
        return Optional.ofNullable(getEntryWithContentField(cdaEntry, fieldIdentifier))
                .flatMap(entryWithContentField ->
                        getContent(entryWithContentField, fieldIdentifier.contentFieldName));
    }

    private Optional<String> getContent(final CDAEntry entry, final String contentFieldName) {
        return getCdaField(entry, contentFieldName)
                .flatMap(cdaField -> getFirstSupportedLocale(entry, contentFieldName)
                        .flatMap(supportedLocale -> {
                            final Object localizedEntryField = getLocalizedEntryField(supportedLocale, entry,
                                    contentFieldName);
                            return getContentAccordingToFieldDefinition(localizedEntryField, cdaField);
                        }));
    }

    private CDAEntry getEntryWithContentField(@Nullable final CDAEntry cdaEntry, final FieldIdentifier fieldIdentifier) {
        if (fieldIdentifier.isLastLevelEntry()) {
            return cdaEntry;
        } else {
            final String key = fieldIdentifier.getCurrentEntryName();
            if (cdaEntry != null && cdaEntry.rawFields().containsKey(key)) {
                Object item = cdaEntry.getField(key);
                if (item instanceof CDAEntry) {
                    fieldIdentifier.removeCurrentEntryName();
                    return getEntryWithContentField((CDAEntry) item, fieldIdentifier);
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
    
    private Object getLocalizedEntryField(final Locale locale, final CDAEntry lastLevelEntry, final String fieldName) {
        lastLevelEntry.setLocale(locale.toLanguageTag());
        return lastLevelEntry.getField(fieldName);
    }

    private Optional<Locale> getFirstSupportedLocale(final CDAEntry lastLevelEntry, final String fieldName) {
        final Map<String, Object> rawFields = lastLevelEntry.rawFields();
        final Map<String, Object> localeContentMap = getLocaleContentMap(fieldName, rawFields);
        return Optional.ofNullable(localeContentMap).flatMap(map -> {
            final Set<String> allSupportedLocales = map.keySet();
            return locales.stream()
                    .filter(locale -> allSupportedLocales.contains(locale.toLanguageTag()))
                    .findFirst();
        });
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

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLocaleContentMap(String fieldName, Map<String, Object> rawFields) {
        return (Map<String, Object>) rawFields.get(fieldName);
    }

    private class FieldIdentifier {

        private List<String> entryNamesList = Collections.emptyList();

        private String contentFieldName = "";

        FieldIdentifier(@Nonnull final String fieldName) {
            List<String> allNames = new ArrayList<>(Arrays.asList(split(fieldName, ".")));
            if (allNames.size() > 1) {
                final int lastIndex = allNames.size() - 1;
                entryNamesList = allNames.subList(0, lastIndex);
                contentFieldName = allNames.get(lastIndex);
            } else {
                contentFieldName = allNames.get(0);
            }
        }

        String getCurrentEntryName() {
            return entryNamesList.get(0);
        }

        boolean isLastLevelEntry() {
            return entryNamesList.isEmpty();
        }

        void removeCurrentEntryName() {
            entryNamesList.remove(0);
        }
    }

}
