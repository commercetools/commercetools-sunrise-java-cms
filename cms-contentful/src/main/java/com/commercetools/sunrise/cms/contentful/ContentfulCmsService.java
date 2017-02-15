package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.CmsService;
import com.commercetools.sunrise.cms.CmsServiceException;
import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDACallback;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAResource;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;

/**
 * Service providing access to CMS pages from Contentful platform.
 * <p>
 * Instance of the service is created per Contentful page type and one of this type's fields upon which query will
 * be executed. A consequence of that is that content should be uniquely identified by chosen field. If there is more
 * than one entity of chosen type with the same query field Contentful will return all of them but this service
 * will result in {@link CompletableFuture} completed by throwing {@link CmsServiceException}
 * informing about non-unique identifier used.
 * <p>
 * Contentful's JVM executor is synchronous and for that reason this service is built with additional {@link Executor}
 * parameter to provide its user with control over execution context in which requests are executed.
 */
public class ContentfulCmsService implements CmsService {

    private final CDAClient client;
    private final String pageType;
    private final String pageQueryField;
    private final Executor callbackExecutor;

    private ContentfulCmsService(CDAClient client, String pageType, String pageQueryField, Executor callbackExecutor) {
        this.client = client;
        this.pageType = pageType;
        this.pageQueryField = "fields." + pageQueryField;
        this.callbackExecutor = callbackExecutor;
    }

    /**
     * Get the page content corresponding to the given key.
     *
     * @param pageKey identifying the page
     * @param locales for the localized content inside the page
     * @return a {@link CompletionStage} containing the page content identified by the key,
     * or absent if it could not be found, or a {@link CmsServiceException} if there was a problem
     * when obtaining content
     */
    @Override
    public CompletionStage<Optional<CmsPage>> page(final String pageKey, final List<Locale> locales) {
        return fetchEntry(pageKey, getLocaleForContentful(locales))
                .thenApply(cdaEntry -> cdaEntry.map(ContentfulCmsPage::new));
    }

    /**
     * Convert first of provided locales to a string expected by Contentful. If list is empty return asterisk: '*'
     * which will make request independent of locale.
     * <p>
     * Contentful provides only single locale to be set per request.
     *
     * @param locales list of locales with only first one relevant for Contentful
     * @return string representation of requested locale adjusted for Contentful
     */
    private String getLocaleForContentful(List<Locale> locales) {
        return locales.isEmpty() ? "*" : locales.get(0).toLanguageTag();
    }

    private CompletableFuture<Optional<CDAEntry>> fetchEntry(final String pageKey, final String locale) {
        return new ContentCallback(pageKey, locale).fetch();
    }

    /**
     * Create new instance of {@link ContentfulCmsService} based on Contentful account credentials.
     *
     * @param spaceId          Contentful space ID
     * @param token            access token to given space
     * @param pageType         Contentful model's page type to be queried against
     * @param pageQueryField   pageType field against which query will be run
     * @param callbackExecutor defines execution context in which requests are executed
     * @return instance of this service ready to serve content based on given configuration
     */
    public static ContentfulCmsService of(String spaceId, String token, String pageType, String pageQueryField,
                                          Executor callbackExecutor) {
        return new ContentfulCmsService(createClient(spaceId, token), pageType, pageQueryField, callbackExecutor);
    }

    private static CDAClient createClient(String spaceId, String token) {
        return CDAClient.builder()
                .setSpace(spaceId)
                .setToken(token)
                .build();
    }

    /**
     * An Object handling all communication with Contentful platform based on given configuration in order to fetch
     * requested cms page for given locale.
     */
    private class ContentCallback {
        private final String pageKey;
        private final String locale;

        ContentCallback(String pageKey, String locale) {
            this.pageKey = pageKey;
            this.locale = locale;
        }

        /**
         * Execute request to Contentful inside configured {@link Executor} context.
         */
        CompletableFuture<Optional<CDAEntry>> fetch() {
            ContentfulCallback contentfulCallback = new ContentfulCallback();
            callbackExecutor.execute(() ->
                    client.fetch(CDAEntry.class)
                            .where("content_type", pageType)
                            .where("include", "10") // levels of entries to include in fetched hierarchy; 10 is Contentful's max
                            .where("locale", locale)
                            .where(pageQueryField, pageKey)
                            .all(contentfulCallback)
            );
            return contentfulCallback.toCompletableFuture();
        }

        /**
         * Wrapper for Contentful's callback which verifies that only single (unique) item was fetched and returns it
         * wrapped into {@link CompletableFuture}.
         * <p>
         * In case fetching failed a meaningful message is returned in {@link CmsServiceException}.
         */
        private class ContentfulCallback extends CDACallback<CDAArray> {
            private final CompletableFuture<Optional<CDAEntry>> future = new CompletableFuture<>();

            @Override
            protected void onSuccess(final CDAArray result) {
                List<CDAResource> items = result.items();
                if (items.isEmpty()) {
                    future.complete(Optional.empty());
                } else if (items.size() > 1) {
                    future.completeExceptionally(
                            new CmsServiceException("Non unique identifier used." +
                                    " Result contains more than one page for " + pageKey));
                } else {
                    future.complete(Optional.of((CDAEntry) items.get(0)));
                }
            }

            @Override
            protected void onFailure(final Throwable error) {
                future.completeExceptionally(new CmsServiceException("Could not fetch content for " + pageKey, error));
            }

            CompletableFuture<Optional<CDAEntry>> toCompletableFuture() {
                return future;
            }
        }
    }
}
