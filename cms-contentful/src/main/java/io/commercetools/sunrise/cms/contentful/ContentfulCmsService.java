package io.commercetools.sunrise.cms.contentful;

import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDAAsset;
import com.contentful.java.cda.CDACallback;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import io.commercetools.sunrise.cms.CmsIdentifier;
import io.commercetools.sunrise.cms.CmsService;
import io.commercetools.sunrise.cms.CmsServiceException;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Service that provides page content, coming from Contentful.
 */
public class ContentfulCmsService implements CmsService {

    private static final String ENTRY_TYPE = "content_type";
    private static final String ENTRY_KEY = "fields.name[match]";
    private static final String CDA_LIMIT_KEY = "limit";
    private static final String LIMIT = "1";
    private final CDAClient client;

    ContentfulCmsService(final CDAClient client) {
        this.client = client;
    }

    /**
     * Gets the content corresponding to the given CMS identifier for the first found given language.
     * @param locales the list of locales used to translate the message
     * @param cmsIdentifier identifier of the CMS entry field
     * @return the {@code completionStage} of the content in the first found given language,
     * or absent if it could not be found, or a {@link CmsServiceException} if there was a problem
     * when obtaining the content from Contentful.
     */
    @Override
    public CompletionStage<Optional<String>> get(final List<Locale> locales, final CmsIdentifier cmsIdentifier) {
        return getEntry(cmsIdentifier)
                .thenApply(cdaEntryOptional -> cdaEntryOptional
                        .flatMap(cdaEntry -> getLocalizedField(locales, cdaEntry, cmsIdentifier.getFieldName())));
    }

    private CompletionStage<Optional<CDAEntry>> getEntry(final CmsIdentifier cmsIdentifier) {
        return fetchEntry(cmsIdentifier).thenApply(cdaArray -> Optional.ofNullable(cdaArray)
                .filter(e -> e.items() != null && !e.items().isEmpty())
                .map(e -> (CDAEntry) e.items().get(0)));
    }

    private CompletionStage<CDAArray> fetchEntry(final CmsIdentifier cmsIdentifier) {
        final CompletableFuture<CDAArray> future = new CompletableFuture<>();
        final CDACallback<CDAArray> callback = new CDACallback<CDAArray>(){
            @Override
            protected void onSuccess(final CDAArray result) {
                future.complete(result);
            }

            @Override
            protected void onFailure(final Throwable error) {
                future.completeExceptionally(new CmsServiceException("Could not fetch content for " + cmsIdentifier.toString(), error));
            }
        };
        client.fetch(CDAEntry.class)
                .where(ENTRY_TYPE, cmsIdentifier.getEntryType())
                .where(ENTRY_KEY, cmsIdentifier.getEntryKey())
                .where(CDA_LIMIT_KEY, LIMIT)
                .all(callback);
        return future;
    }

    Optional<String> getLocalizedField(final List<Locale> locales, final CDAEntry cdaEntry, final String fieldId) {
        final Optional<Locale> localeOptional = getFirstSupportedLocale(locales, cdaEntry, fieldId);
        final Object cdaEntryField = localeOptional.map(locale -> {
            cdaEntry.setLocale(locale.toLanguageTag());
            return cdaEntry.getField(fieldId);
        }).orElse(null);
        return getContentAccordingToFieldType(cdaEntryField);
    }

    private Optional<Locale> getFirstSupportedLocale(final List<Locale> locales,
                                                     final CDAEntry cdaEntry, final String fieldId) {
        final Map<String, Object> rawFields = cdaEntry.rawFields();
        final Map<String, Object> localeContentMap = getLocaleContentMap(fieldId, rawFields);
        return Optional.ofNullable(localeContentMap).flatMap(map -> {
            final Set<String> allSupportedLocales = map.keySet();
            return locales.stream()
                .filter(locale -> allSupportedLocales.contains(locale.toLanguageTag()))
                .findFirst();
        });
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> getLocaleContentMap(String fieldName, Map<String, Object> rawFields) {
        return (Map<String, Object>) rawFields.get(fieldName);
    }

    private Optional<String> getContentAccordingToFieldType(@Nullable final Object cdaEntryField) {
        if (cdaEntryField instanceof CDAAsset) {
            return Optional.of(((CDAAsset) cdaEntryField).url());
        } else if (cdaEntryField instanceof String) {
            return Optional.of(cdaEntryField.toString());
        } else {
            return Optional.empty();
        }
    }

    /**
     * Creates new instance of ContentfulCmsService based on Contentful account credentials
     */
    public static ContentfulCmsService of(final String spaceId, final String token) {
        // TODO create contentful-cms-config class
        final CDAClient client = CDAClient
                .builder()
                .setSpace(spaceId)
                .setToken(token)
                .build();
        return new ContentfulCmsService(client);
    }
}
