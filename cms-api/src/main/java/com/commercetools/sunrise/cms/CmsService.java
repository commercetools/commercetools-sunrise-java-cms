package com.commercetools.sunrise.cms;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

/**
 * Service that provides page content, coming from some sort of Content Management System (CMS).
 */
@FunctionalInterface
public interface CmsService {

    /**
     * Gets the page content corresponding to the given key.
     * @param pageKey identifying the page
     * @param locales for the localized content inside the page
     * @return a {@code CompletionStage} containing the page content identified by the key
     */
    CompletionStage<Optional<CmsPage>> page(final String pageKey, final List<Locale> locales);

}