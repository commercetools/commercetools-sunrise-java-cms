package com.commercetools.sunrise.cms.customobjects;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.CmsService;
import com.fasterxml.jackson.databind.JsonNode;
import io.sphere.sdk.client.SphereClient;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.customobjects.queries.CustomObjectByKeyGet;
import io.sphere.sdk.customobjects.queries.CustomObjectQuery;
import io.sphere.sdk.expansion.ExpansionPath;
import io.sphere.sdk.queries.PagedQueryResult;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;

final class CoCmsService implements CmsService {
    private final SphereClient client;

    CoCmsService(SphereClient client) {
        this.client = client;
    }

    @Override
    public CompletionStage<Optional<CmsPage>> page(String pageKey, List<Locale> locales) {
        final CustomObjectQuery<JsonNode> customObjectQuery = CustomObjectQuery.ofJsonNode()
                .withPredicates(m -> m.container().is("co-cms-pages"))
                .plusPredicates(m -> m.key().is(pageKey))
                .withExpansionPaths(ExpansionPath.of("value.dependencies[*]"))
                .withLimit(1L);
        return client.execute(customObjectQuery).thenApplyAsync(this::createCmsPageOption);
    }

    private Optional<CmsPage> createCmsPageOption(final PagedQueryResult<CustomObject<JsonNode>> co) {
        return co.head().map(this::createPage);
    }

    private CmsPage createPage(CustomObject<JsonNode> co) {
        return new CoCmsPage(co);
    }
}
