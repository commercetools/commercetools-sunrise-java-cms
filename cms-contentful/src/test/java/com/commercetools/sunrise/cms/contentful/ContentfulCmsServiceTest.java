package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.CmsService;
import com.commercetools.sunrise.cms.CmsServiceException;
import com.contentful.java.cda.CDAArray;
import com.contentful.java.cda.CDAClient;
import com.contentful.java.cda.CDAEntry;
import com.contentful.java.cda.CDAResource;
import com.contentful.java.cda.FetchQuery;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionException;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Supplier;

import static com.commercetools.sunrise.cms.contentful.ContentfulMockUtil.mockEntryWithField;
import static com.commercetools.sunrise.cms.contentful.FieldType.BOOLEAN;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isA;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ContentfulCmsServiceTest {

    @Test
    public void whenAskForExistingStringContent_thenGet() {
        FetchQuery<CDAEntry> fetchQuery = getFetchQuery(singletonList(mockEntryWithField("aField", true, BOOLEAN.type())));
        CDAClient cdaClient = mockCdaClient(fetchQuery);
        CmsService cmsService = service(() -> cdaClient);

        Optional<CmsPage> page = get(cmsService.page("aPage", singletonList(Locale.GERMANY)));

        assertThat(page).isPresent();
        verify(fetchQuery).where(eq("locale"), eq(Locale.GERMANY.toLanguageTag()));
    }

    @Test
    public void whenAskForExistingStringContent_emptyLocales_thenGet() {
        FetchQuery<CDAEntry> fetchQuery = getFetchQuery(singletonList(mockEntryWithField("aField", true, BOOLEAN.type())));
        CDAClient cdaClient = mockCdaClient(fetchQuery);
        CmsService cmsService = service(() -> cdaClient);

        Optional<CmsPage> page = get(cmsService.page("aPage", emptyList()));

        assertThat(page).isPresent();
        verify(fetchQuery).where(eq("locale"), eq("*"));
    }

    @Test
    public void whenAskForNotExistingContent_thenReturnEmpty() {
        FetchQuery<CDAEntry> fetchQuery = getFetchQuery(emptyList());
        CDAClient cdaClient = mockCdaClient(fetchQuery);
        CmsService cmsService = service(() -> cdaClient);

        Optional<CmsPage> page = get(cmsService.page("aPage", emptyList()));

        assertThat(page).isNotPresent();
    }

    @Test
    public void whenAskForNonUniqueContent_thenCompleteExceptionally() {
        FetchQuery<CDAEntry> fetchQuery = getFetchQuery(Arrays.asList(mock(CDAResource.class), mock(CDAResource.class)));
        CDAClient cdaClient = mockCdaClient(fetchQuery);
        CmsService cmsService = service(() -> cdaClient);

        Throwable thrown = catchThrowable(() -> get(cmsService.page("aPage", emptyList())));

        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
        assertThat(thrown.getCause()).hasMessage("Non unique identifier used. Result contains more than one page for aPage");
    }

    // TODO check this more
    @Test
    public void whenAskWithSthWrong_thenCompleteExceptionally() {
        FetchQuery<CDAEntry> fetchQuery = getFetchQueryForException(new Exception());
        CDAClient cdaClient = mockCdaClient(fetchQuery);
        CmsService cmsService = service(() -> cdaClient);

        Throwable thrown = catchThrowable(() -> get(cmsService.page("aPage", emptyList())));

        assertThat(thrown).isInstanceOf(CompletionException.class).hasMessageContaining("Could not fetch content for aPage");
        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
    }

    private static CmsService service(Supplier<CDAClient> mockCdaClient) {
        return ContentfulCmsService.of(mockCdaClient, "pageType", "pageQueryField", Runnable::run);
    }

    private CDAClient mockCdaClient(FetchQuery<CDAEntry> fetchQuery) {
        CDAClient client = mock(CDAClient.class);

        when(client.fetch(same(CDAEntry.class))).thenReturn(fetchQuery);

        return client;
    }

    private FetchQuery<CDAEntry> getFetchQuery(List<CDAResource> result) {
        SpiedFetchQuery fetchQuery = SpiedFetchQuery.of();

        Class<ContentfulCmsService.ContentCallback.ContentfulCallback> clazz = ContentfulCmsService.ContentCallback.ContentfulCallback.class;
        doAnswer(invocation -> {
            ContentfulCmsService.ContentCallback.ContentfulCallback callback = invocation.getArgumentAt(0, clazz);
            callback.onSuccess(mockClientResult(result));
            return null;
        }).when(fetchQuery).all(isA(clazz));

        return fetchQuery;
    }

    private FetchQuery<CDAEntry> getFetchQueryForException(Throwable throwable) {
        SpiedFetchQuery fetchQuery = SpiedFetchQuery.of();

        Class<ContentfulCmsService.ContentCallback.ContentfulCallback> clazz = ContentfulCmsService.ContentCallback.ContentfulCallback.class;
        doAnswer(invocation -> {
            ContentfulCmsService.ContentCallback.ContentfulCallback callback = invocation.getArgumentAt(0, clazz);
            callback.onFailure(throwable);
            return null;
        }).when(fetchQuery).all(isA(clazz));

        return fetchQuery;
    }

    public static class SpiedFetchQuery extends FetchQuery<CDAEntry> {
        private SpiedFetchQuery spy;

        private SpiedFetchQuery(final Class<CDAEntry> type, final CDAClient client) {
            super(type, client);
        }

        @Override
        public FetchQuery<CDAEntry> where(final String key, final String value) {
            return spy;
        }

        static SpiedFetchQuery of() {
            SpiedFetchQuery fetchQuery = new SpiedFetchQuery(CDAEntry.class, null);
            fetchQuery.initSpy();
            return fetchQuery.spy;
        }

        private void initSpy() {
            spy = spy(this);
            spy.spy = spy;
        }
    }

    private static CDAArray mockClientResult(List<CDAResource> result) {
        return new CDAArray() {
            @Override
            public List<CDAResource> items() {
                return result;
            }
        };
    }

    private Optional<CmsPage> get(final CompletionStage<Optional<CmsPage>> stage) {
        return stage.toCompletableFuture().join();
    }
}