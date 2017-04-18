package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.CmsService;
import com.commercetools.sunrise.cms.CmsServiceException;
import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDACallback;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDALocale;
import com.contentful.java.cda.CDAResource;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Executor;
import java.util.function.Supplier;

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

    private ContentfulCmsService(final CDAClient client, final String pageType, final String pageQueryField,
                                 final Executor callbackExecutor) {
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
    private String getLocaleForContentful(final List<Locale> locales) {
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
    public static ContentfulCmsService of(final String spaceId, final String token, final String pageType,
                                          final String pageQueryField, final Executor callbackExecutor) {
        return of(() -> createClient(spaceId, token), pageType, pageQueryField, callbackExecutor);
    }

    static ContentfulCmsService of(final Supplier<CDAClient> contentfulClientProvider,
                                   final String pageType, final String pageQueryField,
                                   final Executor callbackExecutor) {
        return new ContentfulCmsService(contentfulClientProvider.get(), pageType, pageQueryField, callbackExecutor);
    }

    private static CDAClient createClient(final String spaceId, final String token) {
        return CDAClient.builder()
                .setSpace(spaceId)
                .setToken(token)
                .build();
    }

    /**
     * An Object handling all communication with Contentful platform based on given configuration in order to fetch
     * requested cms page for given locale.
     */
    class ContentCallback {
        private final String pageKey;
        private final String locale;

        private ContentCallback(final String pageKey, final String locale) {
            this.pageKey = pageKey;
            this.locale = locale;
        }

        /**
         * Execute request to Contentful inside configured {@link Executor} context.
         */
        private CompletableFuture<Optional<CDAEntry>> fetch() {
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
        class ContentfulCallback extends CDACallback<CDAArray> {
            private final CompletableFuture<Optional<CDAEntry>> future = new CompletableFuture<>();

            @Override
            protected void onSuccess(final CDAArray result) {
                List<CDAResource> items = result.items();
                if (items.isEmpty()) {
                    future.complete(Optional.empty());
                } else if (items.size() > 1) {
                    completeExceptionally("Non unique identifier used. Result contains more than one page for "
                            + pageKey, null);
                } else {
                    future.complete(Optional.of((CDAEntry) items.get(0)));
                }
            }

            @Override
            protected void onFailure(final Throwable error) {
                try {
                    if (StringUtils.contains(error.getMessage(), "code=400") && localeNotInSpace()) {
                        // Contentful responds with HTTP Bad Request (400) in several cases one of which is
                        // when trying to fetch page for locale that is not configured in the space.
                        // In that case detailed message is provided. All other errors are consequence of wrong
                        // configuration of connection to Contentful or requested space (e.g. content type is not there).
                        completeExceptionally("Requested locale " + locale + " is not defined on CMS. "
                                + "Could not fetch content for " + pageKey, error);
                    } else {
                        completeExceptionally("Could not fetch content for " + pageKey, error);
                    }
                } catch (Throwable e) {
                    completeExceptionally("Could not fetch content for " + pageKey, error);
                }
            }

            private void completeExceptionally(final String message, final Throwable cause) {
                future.completeExceptionally(new CmsServiceException(message, cause));
            }

            private boolean localeNotInSpace() {
                List<CDALocale> contentfulLocales = client.fetchSpace().locales();
                return contentfulLocales.stream()
                        .noneMatch(cdaLocale -> Objects.equals(cdaLocale.code(), locale));
            }

            private CompletableFuture<Optional<CDAEntry>> toCompletableFuture() {
                return future;
            }
        }
    }
}
