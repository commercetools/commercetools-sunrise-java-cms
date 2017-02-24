package com.commercetools.sunrise.cms;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * A service providing access to CMS pages.
 */
@FunctionalInterface
public interface CmsService {

    /**
     * Retrieves the page identified by the given key for any of the provided locales trying to match the first
     * on the list that the content is defined for.
     * <p>
     * Depending on implementation and CMS provider if requested page is not defined for any of requested locales
     * an empty result might be returned or configured default locale might be used.
     *
     * @param pageKey identifier of the page
     * @param locales list of locales for which the content is expected to be defined
     * @return requested page or empty result
     */
    CompletionStage<Optional<CmsPage>> page(String pageKey, List<Locale> locales);

}
