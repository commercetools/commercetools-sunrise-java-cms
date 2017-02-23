package com.commercetools.sunrise.cms.contentful;

import com.commercetools.sunrise.cms.CmsPage;
import com.commercetools.sunrise.cms.CmsService;
import com.commercetools.sunrise.cms.CmsServiceException;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.isNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.ThrowableAssert.catchThrowable;

@SuppressWarnings("OptionalGetWithoutIsPresent")
public class ContentfulCmsServiceIT {

    private static final String CONTENTFUL_SPACE_ID = "CONTENTFUL_SPACE_ID";
    private static final String CONTENTFUL_TOKEN = "CONTENTFUL_TOKEN";

    private static CmsService cmsServiceFor_Finn_PageType;
    private static CmsService cmsServiceFor_Page_PageType;

    @BeforeClass
    public static void setUp() {
        cmsServiceFor_Finn_PageType = service("finn");
        cmsServiceFor_Page_PageType = service("page");
    }

    @Test
    public void whenAskForExistingStringContent_thenGet() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Page_PageType.page("finn", singletonList(Locale.GERMANY)));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("pageContent.description");
        assertThat(field).hasValue("Fearless Abenteurer! Verteidiger von Pfannkuchen.");
    }

    @Test
    public void whenAskForExistingStringContentAndLocalesAreEmpty_thenGetDefaultLocaleContent() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Page_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("pageContent.description");
        assertThat(field).hasValue("Fearless Abenteurer! Verteidiger von Pfannkuchen.");
    }

    @Test
    public void whenAskForExistingStringContentWithNotDefaultLocale_thenGetDefaultLocaleContent() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Page_PageType.page("finn", singletonList(Locale.ENGLISH)));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("pageContent.description");
        assertThat(field).hasValue("Fearless adventurer! Defender of pancakes.");
    }

    @Test
    public void whenNoConfigurationForClientProvided_thenThrowException() {
        CmsService cmsService = service("", "", "page", "slug");

        Throwable thrown = catchThrowable(() -> waitAndGet(cmsService.page("home", singletonList(Locale.GERMANY))));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasMessageContaining("Could not fetch content for home");
        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
    }

    @Test
    public void whenAskForContentWithLocaleNotDefinedInSpace_thenThrowException() {
        Throwable thrown = catchThrowable(() -> waitAndGet(cmsServiceFor_Page_PageType.page("finn", singletonList(Locale.ITALIAN))));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasMessage("com.commercetools.sunrise.cms.CmsServiceException: Requested locale it is not defined on CMS. Could not fetch content for finn");
        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
    }

    @Test
    public void whenAskForContentWithNoGivenQueryFieldDefined_thenThrowException() {
        CmsService cmsService = service(spaceId(), token(), "page", "non-existing-field-name");

        Throwable thrown = catchThrowable(() -> waitAndGet(cmsService.page("finn", singletonList(Locale.ENGLISH))));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasMessageContaining("Could not fetch content for finn");
        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
    }

    @Test
    public void whenAskForContentForWhichThereIsNoContentTypeDefinedInSpace_thenThrowException() {
        CmsService cmsService = service("non-existing-page-type");

        Throwable thrown = catchThrowable(() -> waitAndGet(cmsService.page("finn", singletonList(Locale.ENGLISH))));

        assertThat(thrown).isInstanceOf(ExecutionException.class).hasMessageContaining("Could not fetch content for finn");
        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
    }

    @Test
    public void whenAskForNonUniqueContent_thenThrowException() {
        Throwable thrown = catchThrowable(() -> waitAndGet(cmsServiceFor_Page_PageType.page("jacke", singletonList(Locale.GERMANY))));

        assertThat(thrown).hasCauseInstanceOf(CmsServiceException.class);
        assertThat(thrown.getCause()).hasMessage("Non unique identifier used. Result contains more than one page for jacke");
    }

    @Test
    public void whenAskForNotExistingStringContent_thenReturnEmpty() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Page_PageType.page("finn", singletonList(Locale.GERMANY)));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("pageContent.notExistingField");
        assertThat(field).isNotPresent();
    }

    @Test
    public void whenRequestedPageDoesNotExist_thenReturnEmpty() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Page_PageType.page("non-existing-page-key", singletonList(Locale.GERMANY)));

        assertThat(page).isNotPresent();
    }

    @Test
    public void whenAskForContentInArray_thenGetElement() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("array[0].name");
        assertThat(field).hasValue("author1");
    }

    @Test
    public void whenAskForContentInArrayOutsideTheScope_thenReturnEmpty() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        // array element consists of only 4 items
        Optional<String> field = page.get().field("array[4].name");
        assertThat(field).isNotPresent();
    }

    @Test
    public void whenAskForContentInNestedArray_thenGetElement() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("array[1].images[1].photo");
        assertThat(field).hasValue("//images.contentful.com/l6chdlzlf8jn/3slBXXe6WcsiM46OuEuIKe/bab374a315d1825c111d9d89843cafc0/logo.gif");
    }

    @Test
    public void whenAskForTextContentArray_thenGetElement() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("array[2].simpleText");
        assertThat(field).hasValue("simpleText1");
    }

    @Test
    public void whenAskForLocation_thenGetLocationField() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("locationField");
        assertThat(field).hasValue("{lon=19.62158203125, lat=51.37199469960235}");
    }

    @Test
    public void whenAskForContentInMediaField_thenGetUrlField() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("mediaOneFile");
        assertThat(field).hasValue("//images.contentful.com/l6chdlzlf8jn/2m1NzbeXYUksOGIwceEy0U/4e3cc53a96313a4bd822777af78a3b4d/some-5.jpg");
    }

    @Test
    public void whenAskForContentInMediaArray_thenGetUrlElements() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("mediaManyFiles[0]");
        assertThat(field).hasValue("//images.contentful.com/l6chdlzlf8jn/6j9p38phC0oU0g42aqUSc4/2eb0c261bc13353ed867b13076af6b1f/logo.gif");

        field = page.get().field("mediaManyFiles[1]");
        assertThat(field).hasValue("//images.contentful.com/l6chdlzlf8jn/27BPx56xMcuGKe8uk8Auss/335581cd9daf3e9d0de254313e36d43b/some-5.jpg");
    }

    @Test
    public void whenAskForArray_thenReturnEmpty() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", emptyList()));

        assertThat(page).isPresent();
        // textArrayField is an array element so it can't be fetched as a whole
        Optional<String> field = page.get().field("array[3].textArrayField");
        assertThat(field).isNotPresent();
    }

    @Test
    public void whenAskForContentWithGermanOrEmptyLocales_thenGetGermanTranslation() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", singletonList(Locale.GERMANY)));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("array[3].textArrayField[1]");
        assertThat(field).hasValue("zwei");

        page = cmsServiceFor_Finn_PageType.page("finn", emptyList()).toCompletableFuture().get(5, TimeUnit.SECONDS);

        assertThat(page).isPresent();
        field = page.get().field("array[3].textArrayField[1]");
        assertThat(field).hasValue("zwei");
    }

    @Test
    public void whenAskForContentWithEnglishTranslation_thenGetEnglishTranslation() throws Exception {
        Optional<CmsPage> page = waitAndGet(cmsServiceFor_Finn_PageType.page("finn", singletonList(Locale.ENGLISH)));

        assertThat(page).isPresent();
        Optional<String> field = page.get().field("array[3].textArrayField[1]");
        assertThat(field).hasValue("two");
    }

    private Optional<CmsPage> waitAndGet(final CompletionStage<Optional<CmsPage>> stage) throws Exception {
        return stage.toCompletableFuture().get(5, TimeUnit.SECONDS);
    }

    private static CmsService service(String pageType) {
        return service(spaceId(), token(), pageType, "slug");
    }

    private static CmsService service(String spaceId, String token, String pageType, String pageQueryField) {
        return ContentfulCmsService.of(spaceId, token, pageType, pageQueryField, Runnable::run);
    }

    private static String spaceId() {
        return getEnv(CONTENTFUL_SPACE_ID);
    }

    private static String token() {
        return getEnv(CONTENTFUL_TOKEN);
    }

    private static String getEnv(final String key) {
        final String env = System.getenv(key);
        if (isNull(env)) {
            throw new RuntimeException(
                    "Missing environment variable " + key + ", please provide the following environment variables for the integration test:\n" +
                            "export " + CONTENTFUL_SPACE_ID + "=\"Your Contentful project key\"\n" +
                            "export " + CONTENTFUL_TOKEN + "=\"Your Contentful authentication token\"\n");
        }
        return env;
    }

}