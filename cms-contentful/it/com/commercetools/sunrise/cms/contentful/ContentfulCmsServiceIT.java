package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.CmsService;
import com.commercetools.sunrise.cms.CmsServiceException;
import org.junit.Test;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

public class ContentfulCmsServiceIT {
    private static final List<Locale> SUPPORTED_LOCALES = singletonList(Locale.GERMANY);

    private static final String PAGE_TYPE_NAME = "page";
    private static final String PAGE_TYPE_ID_FIELD_NAME = "slug";

    @Test
    public void whenCouldNotFetchEntry_thenThrowException() throws Exception {
        CmsService cmsService = ContentfulCmsService.of("", "", PAGE_TYPE_NAME, PAGE_TYPE_ID_FIELD_NAME);

        Throwable thrown = catchThrowable(() -> waitAndGet(cmsService.page("home", SUPPORTED_LOCALES)));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasMessageContaining("Could not fetch content for home");
        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
    }

    @Test
    public void whenAskForExistingStringContent_thenGet() throws Exception {
        CmsService contentfulCmsService = createService();

        Optional<CmsPage> content = waitAndGet(contentfulCmsService.page("finn", SUPPORTED_LOCALES));

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("pageContent.description");
        assertThat(field).hasValue("Fearless Abenteurer! Verteidiger von Pfannkuchen.");
    }

    @Test
    public void whenAskForExistingStringContentAndLocalesAreEmpty_thenGetDefaultLocaleContent() throws Exception {
        CmsService contentfulCmsService = createService();

        Optional<CmsPage> content = waitAndGet(contentfulCmsService.page("finn", emptyList()));

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("pageContent.description");
        assertThat(field).hasValue("Fearless Abenteurer! Verteidiger von Pfannkuchen.");
    }

    @Test
    public void whenAskForExistingStringContentWithNotDefaultLocale_thenGetDefaultLocaleContent() throws Exception {
        CmsService cmsService = createService();

        Optional<CmsPage> content = waitAndGet(cmsService.page("finn", singletonList(Locale.ENGLISH)));

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("pageContent.description");
        assertThat(field).hasValue("Fearless adventurer! Defender of pancakes.");
    }

    @Test
    public void whenAskForExistingStringContentWithNotDefinedLocale_thenReturnEmpty() throws Exception {
        CmsService cmsService = createService();

        Optional<CmsPage> content = waitAndGet(cmsService.page("finn", singletonList(Locale.ITALIAN)));

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenAskForNotExistingStringContent_thenReturnEmpty() throws Exception {
        CmsService cmsService = createService();

        Optional<CmsPage> content = waitAndGet(cmsService.page("finn", SUPPORTED_LOCALES));

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("pageContent.notExistingField");
        assertThat(field).isNotPresent();
    }

    @Test
    public void whenAskForExistingAssetContent_thenGet() throws Exception {
        CmsService cmsService = createService();

        Optional<CmsPage> content = waitAndGet(cmsService.page("jacke", SUPPORTED_LOCALES));

        assertThat(content).isPresent();
        String actual = content.get().fieldOrEmpty("pageContent.image");
        assertThat(actual).isEqualToIgnoringCase("//images.contentful.com/l6chdlzlf8jn/2iVeCh1FGoy00Oq8WEI2aI/93c3f0841fcf59743f57e238f6ed67aa/jake.png");
    }

    @Test
    public void whenAskForNotExistingAssetContent_thenReturnEmpty() throws Exception {
        CmsService cmsService = createService();

        Optional<CmsPage> content = waitAndGet(cmsService.page("jacke", SUPPORTED_LOCALES));

        assertThat(content).isPresent();
        String actual = content.get().fieldOrEmpty("pageContent.notExistingAsset");
        assertThat(actual).isEmpty();

    }

    @Test
    public void whenAskForContentInArray_thenGetElement() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);
        Optional<CmsPage> content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("array[0].name");
        assertThat(field).hasValue("author1");
    }

    @Test
    public void whenAskForContentInArrayOutsideTheScope_thenReturnEmpty() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        // array element consists of only 4 items
        Optional<String> field = content.get().field("array[4].name");
        assertThat(field).isNotPresent();
    }

    @Test
    public void whenAskForContentInNestedArray_thenGetElement() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("array[1].images[1].photo");
        assertThat(field).hasValue("//images.contentful.com/l6chdlzlf8jn/3slBXXe6WcsiM46OuEuIKe/bab374a315d1825c111d9d89843cafc0/logo.gif");
    }

    @Test
    public void whenAskForTextContentArray_thenGetElement() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("array[2].simpleText");
        assertThat(field).hasValue("simpleText1");
    }

    @Test
    public void whenAskForLocation_thenGetLocationField() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("locationField");
        assertThat(field).hasValue("{lon=19.62158203125, lat=51.37199469960235}");
    }

    @Test
    public void whenAskForContentInMediaField_thenGetUrlField() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("mediaOneFile");
        assertThat(field).hasValue("//images.contentful.com/l6chdlzlf8jn/2m1NzbeXYUksOGIwceEy0U/4e3cc53a96313a4bd822777af78a3b4d/some-5.jpg");
    }

    @Test
    public void whenAskForContentInMediaArray_thenGetUrlElements() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("mediaManyFiles[0]");
        assertThat(field).hasValue("//images.contentful.com/l6chdlzlf8jn/6j9p38phC0oU0g42aqUSc4/2eb0c261bc13353ed867b13076af6b1f/logo.gif");

        field = content.get().field("mediaManyFiles[1]");
        assertThat(field).hasValue("//images.contentful.com/l6chdlzlf8jn/27BPx56xMcuGKe8uk8Auss/335581cd9daf3e9d0de254313e36d43b/some-5.jpg");
    }

    @Test
    public void whenAskForArray_thenReturnEmpty() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        // textArrayField is an array element so it can't be fetched as a whole
        Optional<String> field = content.get().field("array[3].textArrayField");
        assertThat(field).isNotPresent();
    }

    @Test
    public void whenAskForContentWithGermanOrEmptyLocales_thenGetGermanTranslation() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", SUPPORTED_LOCALES).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("array[3].textArrayField[1]");
        assertThat(field).hasValue("zwei");

        content = contentfulCmsService.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        field = content.get().field("array[3].textArrayField[1]");
        assertThat(field).hasValue("zwei");

        content = waitAndGet(contentfulCmsService.page("finn", singletonList(Locale.ITALIAN)));

        assertThat(content).isNotPresent();
    }

    @Test
    public void whenAskForContentWithEnglishTranslation_thenGetEnglishTranslation() throws Exception {
        ContentfulCmsService contentfulCmsService = ContentfulCmsService.of(spaceId(), token(), "finn", PAGE_TYPE_ID_FIELD_NAME);

        Optional<CmsPage> content = contentfulCmsService.page("finn", singletonList(Locale.ENGLISH)).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(content).isPresent();
        Optional<String> field = content.get().field("array[3].textArrayField[1]");
        assertThat(field).hasValue("two");
    }

    private <T> T waitAndGet(final CompletionStage<T> stage) throws InterruptedException, ExecutionException, TimeoutException {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    private CmsService createService() {
        return ContentfulCmsService.of(spaceId(), token(), PAGE_TYPE_NAME, PAGE_TYPE_ID_FIELD_NAME);
    }

    private static String spaceId() {
        return getValueForEnvVar("CONTENTFUL_SPACE_ID");
    }

    private static String token() {
        return getValueForEnvVar("CONTENTFUL_TOKEN");
    }

    private static String getValueForEnvVar(final String key) {
        return Optional.ofNullable(System.getenv(key))
                .orElseThrow(() -> new RuntimeException(
                        "Missing environment variable " + key + ", please provide the following environment variables for the integration test:\n" +
                                "export " + ("CONTENTFUL_" + "SPACE_ID") + "=\"Your Contentful project key\"\n" +
                                "export " + ("CONTENTFUL_" + "TOKEN") + "=\"Your Contentful authentication token\"\n"));
    }

}