package io.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.SynchronizedSpace;
import io.commercetools.sunrise.cms.CmsIdentifier;
import io.commercetools.sunrise.cms.CmsService;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.function.Predicate;

public class ContentfulCmsService implements CmsService {

    final private Map<String, CDAEntry> entries;

    ContentfulCmsService(final Map<String, CDAEntry> entries) {
        this.entries = entries;
    }

    @Override
    public CompletionStage<Optional<String>> get(final List<Locale> locales, final CmsIdentifier cmsIdentifier) {
        return CompletableFuture.supplyAsync(() -> getEntry(cmsIdentifier)
                .map(entry -> getLocalizedField(locales, entry, cmsIdentifier.getFieldName()))
                .orElseGet(Optional::empty));
    }

    // TODO create models
    private Optional<CDAEntry> getEntry(CmsIdentifier cmsIdentifier) {
        return entries.values()
                .stream()
                .filter(entryPredicate(cmsIdentifier))
                .findFirst();
    }

    private Predicate<CDAEntry> entryPredicate(CmsIdentifier cmsIdentifier) {
        return entry -> {
            final String contentfulEntryType = entry.contentType().id();
            final String contentfulEntryKey = entry.getField(entry.contentType().displayField());
            return contentfulEntryType.equals(cmsIdentifier.getEntryType())
                    && contentfulEntryKey.equals(cmsIdentifier.getEntryKey());
        };
    }

    private Optional<String> getLocalizedField(List<Locale> locales, CDAEntry cdaEntry, String fieldName) {
        Optional<Locale> localeOptional = getFirstSupportedLocale(locales, cdaEntry, fieldName);
        Object cdaEntryField = localeOptional.map(locale -> {
            cdaEntry.setLocale(locale.toLanguageTag());
            return cdaEntry.getField(fieldName);
        }).orElse(null);
        return getContentAccordingToFieldType(cdaEntryField);
    }

    private Optional<Locale> getFirstSupportedLocale(List<Locale> locales,
                                                     CDAEntry cdaEntry, String fieldName) {
        final Map<String, Object> stringObjectMap = cdaEntry.rawFields();
        Map<String, Object> contentMap = (Map<String, Object>) stringObjectMap.get(fieldName);
        if (contentMap == null) {
            return Optional.empty();
        }
        Set<String> localesFromEntry = contentMap.keySet();
        return locales.stream()
                .filter(locale -> localesFromEntry.contains(locale.toLanguageTag()))
                .findFirst();
    }

    private Optional<String> getContentAccordingToFieldType(Object cdaEntryField) {
        // TODO arrays support
        if (cdaEntryField instanceof CDAAsset) {
            return Optional.of(((CDAAsset) cdaEntryField).url());
        } else if (cdaEntryField instanceof String) {
            return Optional.of(cdaEntryField.toString());
        } else {
            return Optional.empty();
        }
    }

    public static ContentfulCmsService of(String spaceId, String token) {
        // TODO create contentful-cms-config class
        CDAClient client = CDAClient
                .builder()
                .setSpace(spaceId)
                .setToken(token)
                .build();
        // TODO cache based on SynchronisedSpace
        SynchronizedSpace space = client.sync().fetch();
        return new ContentfulCmsService(space.entries());
    }
}
