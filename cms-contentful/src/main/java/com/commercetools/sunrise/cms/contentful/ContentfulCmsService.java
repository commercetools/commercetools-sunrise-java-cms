package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.CmsService;
import com.commercetools.sunrise.cms.CmsServiceException;
import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDACallback;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;

/**
 * Service that provides page content, coming from Contentful.
 */
public class ContentfulCmsService implements CmsService {

    private static final String ENTRY_TYPE = "content_type";
    private static final String ENTRY_KEY_QUERY_PREFIX = "fields.";
    private static final String CDA_LIMIT_KEY = "limit";
    private static final String LIMIT = "1";
    private static final String INCLUDE_LEVELS_KEY = "include";
    private static final String INCLUDE_MAX_LEVEL = "10";
    private static final String QUERY_LOCALE = "locale";
    private final CDAClient client;
    private final String pageTypeName;
    private final String pageTypeIdFieldName;

    private ContentfulCmsService(final CDAClient client, String pageTypeName, String pageTypeIdFieldName) {
        this.client = client;
        this.pageTypeName = pageTypeName;
        this.pageTypeIdFieldName = pageTypeIdFieldName;
    }

    /**
     * Gets the page content corresponding to the given key.
     * @param pageKey identifying the page
     * @param locales for the localized content inside the page
     * @return a {@code CompletionStage} containing the page content identified by the key,
     * or absent if it could not be found, or a {@link CmsServiceException} if there was a problem
     * when obtaining the content from Contentful.
     */
    @Override
    public CompletionStage<Optional<CmsPage>> page(final String pageKey, final List<Locale> locales) {
        return getEntry(pageKey, locales)
                .thenApply(cdaEntryOptional -> cdaEntryOptional
                        .map(cdaEntry ->
                                new ContentfulCmsPage(cdaEntry, locales)));
    }

    private CompletionStage<Optional<CDAEntry>> getEntry(final String pageKey, List<Locale> locales) {
        return fetchEntry(pageKey, locales).thenApply(cdaArray -> Optional.ofNullable(cdaArray)
                .filter(e -> e.items() != null && !e.items().isEmpty())
                .map(e -> (CDAEntry) e.items().get(0)));
    }

    private CompletionStage<CDAArray> fetchEntry(final String pageKey, List<Locale> locales) {
        final CompletableFuture<CDAArray> future = new CompletableFuture<>();
        final CDACallback<CDAArray> callback = new CDACallback<CDAArray>(){
            @Override
            protected void onSuccess(final CDAArray result) {
                future.complete(result);
            }

            @Override
            protected void onFailure(final Throwable error) {
                // check an exception when entry type doesn't exist
                if (error.getMessage().contains("Bad Request")) {
                    future.complete(null);
                } else {
                    future.completeExceptionally(new CmsServiceException("Could not fetch content for " + pageKey,
                            error));
                }
            }
        };
        final String locale = (locales == null ||locales.isEmpty()) ? "*" : locales.get(0).toLanguageTag();
        client.fetch(CDAEntry.class)
                .where(ENTRY_TYPE, pageTypeName)
                .where(ENTRY_KEY_QUERY_PREFIX + pageTypeIdFieldName, pageKey)
                .where(CDA_LIMIT_KEY, LIMIT)
                .where(INCLUDE_LEVELS_KEY, INCLUDE_MAX_LEVEL)
                .where(QUERY_LOCALE, locale)
                .all(callback);
        return future;
    }

    /**
     * Creates new instance of ContentfulCmsService based on Contentful account credentials
     */
    public static ContentfulCmsService of(final String spaceId, final String token, final String pageTypeName,
                                          final String pageTypeIdFieldName) {
        // TODO create contentful-cms-config class
        final CDAClient client = CDAClient
                .builder()
                .setSpace(spaceId)
                .setToken(token)
                .build();
        return new ContentfulCmsService(client, pageTypeName, pageTypeIdFieldName);
    }
}
