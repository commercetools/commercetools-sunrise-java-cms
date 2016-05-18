package io.commercetools.sunrise.cms.contentful;

import io.commercetools.sunrise.cms.CmsIdentifier;
import io.commercetools.sunrise.cms.CmsService;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

public class ContentfulCmsService implements CmsService {

    @Override
    public CompletionStage<Optional<String>> get(final List<Locale> locales, final CmsIdentifier cmsIdentifier) {
        return null; // TODO
    }
}
