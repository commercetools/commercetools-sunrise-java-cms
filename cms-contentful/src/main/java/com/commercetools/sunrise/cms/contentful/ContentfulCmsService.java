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

/**
 * Service that provides access to CMS pages on Contentful platform.
 *
 * TODO PB explain that the service can not be created for entire space but rather for given pageType and queryField
 */
public class ContentfulCmsService implements CmsService {

    private final CDAClient client;
    private final String pageType;
    private final String pageQueryFieldName;

    private ContentfulCmsService(CDAClient client, String pageType, String pageQueryFiel) {
        this.client = client;
        this.pageType = pageType;
        this.pageQueryFieldName = "fields." + pageQueryFiel;
    }

    /**
     * Gets the page content corresponding to the given key.
     *
     * @param pageKey identifying the page
     * @param locales for the localized content inside the page
     * @return a {@code CompletionStage} containing the page content identified by the key,
     * or absent if it could not be found, or a {@link CmsServiceException} if there was a problem
     * when obtaining the content from Contentful.
     */
    @Override
    public CompletionStage<Optional<CmsPage>> page(final String pageKey, final List<Locale> locales) {
        return fetchEntry(pageKey, getLocaleForContentful(locales))
                .thenApply(cdaEntry -> cdaEntry.map(ContentfulCmsPage::new));
    }

    // TODO PB explanation
    private String getLocaleForContentful(List<Locale> locales) {
        // currently contentful provides only single locale to be set per request
        return locales.isEmpty() ? "*" : locales.get(0).toLanguageTag();
    }

    private CompletableFuture<Optional<CDAEntry>> fetchEntry(final String pageKey, final String locale) {
        return new ContentCallback(pageKey, locale).fetch();
    }

    /**
     * Creates new instance of ContentfulCmsService based on Contentful account credentials
     */
    public static ContentfulCmsService of(String spaceId, String token, String pageType, String pageQueryField) {
        return new ContentfulCmsService(createClient(spaceId, token), pageType, pageQueryField);
    }

    private static CDAClient createClient(String spaceId, String token) {
        return CDAClient.builder()
                    .setSpace(spaceId)
                    .setToken(token)
                    .build();
    }

    private class ContentCallback extends CDACallback<CDAArray> {
        private final CompletableFuture<Optional<CDAEntry>> future = new CompletableFuture<>();
        private final String pageKey;
        private final String locale;

        ContentCallback(String pageKey, String locale) {
            this.pageKey = pageKey;
            this.locale = locale;
        }

        @Override
        protected void onSuccess(final CDAArray result) {
            future.complete(findCdaEntry(result));
        }

        @Override
        protected void onFailure(final Throwable error) {
            // check an exception when entry type doesn't exist
            if (error.getMessage().contains("Bad Request")) {
                future.complete(Optional.empty());
            } else {
                future.completeExceptionally(new CmsServiceException("Could not fetch content for " + pageKey, error));
            }
        }

        private Optional<CDAEntry> findCdaEntry(CDAArray result) {
            Optional<CDAEntry> entry = Optional.empty();
            List<CDAResource> items = result.items();
            if (items.size() == 1) {
                CDAResource cdaResource = items.get(0);
                if (cdaResource instanceof CDAEntry) {
                    entry = Optional.of((CDAEntry) cdaResource);
                }
            } else {
                throw new CmsServiceException("Non unique identifier used. Result contains more than one page for "
                        + pageKey);
            }
            return entry;
        }

        CompletableFuture<Optional<CDAEntry>> fetch() {
            client.fetch(CDAEntry.class)
                    .where("content_type", pageType)
                    .where("include", "10") // max level of entries to include in fetched hierarchy of entries
                    .where("locale", locale)
                    .where(pageQueryFieldName, pageKey)
                    .all(this);
            return future;
        }
    }
}
