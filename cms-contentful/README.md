Sunrise Java Contentful CMS
===========================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.commercetools.sunrise.cms/cms-api/badge.svg)](http://search.maven.org/#search|gav|1|g:"com.commercetools.sunrise.cms"%20AND%20a:"cms-contentful")

Module for [Sunrise Java](https://github.com/sphereio/commercetools-sunrise-java)
that serves as an adapter for Contentful CMS platform providing read access to its content.

## Configuration

In order to create a new instance of `ContentfulCmsService` following parameters needs to be provided:

param | description
----- | -----------
spaceId | Contentful space ID
token | access token to given space
pageType | Contentful model's content type
pageQueryField | a field defined in `pageType`
callbackExecutor | defines execution context in which requests are executed

```Java
ContentfulCmsService.of("spaceId", "token", "pageType", "pageQueryField", callbackExecutor);
```

Instance of the service is created per Contentful page type and one of this type's fields upon which queries will
be executed.

Contentful's JVM executor is synchronous and for that reason this service is built with additional `Executor`
parameter to provide its user with control over execution context in which requests are executed.

## Localization

Contentful provides its users with localization facilities. Several locales might be defined for given space.
This adapter might be used to request a page with empty locales list. In such situation implementation makes such
request locale-independent. Contentful uses space's default locale in this case.

## Supported field types

Contentful provides several field types. This implementation serves following of them:

* Boolean
* Date
* Integer
* Number
* Symbol
* Text
* Location
* Asset
* Array

Note that `Asset` field is converted to string in a special way. It is done by getting contained `url`.
Fields of all other types are just converted by their `toString()` method.

`Array` type is not representable as string. Fields of that type can only be used to access contained items.

## Integration Tests

In order to provide automatic verification of correct communication of ContentfulCmsService a single integration test
is implemented.

In order for the test to pass CONTENTFUL_SPACE_ID and CONTENTFUL_TOKEN environment variables need to be set properly.

Contentful space is expected to contain content created by reproducing following steps:

* choose locale: de-DE
* create content type with name 'page' and api ID 'page'
* in 'page' type create new Text field with name 'slug' and field ID 'slug'
* in 'page' type create new Reference field with name 'pageContent' and field ID 'pageContent'
* create content type with name 'pageContent' and api ID 'pageContent'
* in 'pageContent' type create new Text field with name 'description' and field ID 'description'
* add content entry for 'pageContent' type with the following value in 'description' field:
  "Fearless Abenteurer! Verteidiger von Pfannkuchen."
* add content entry for 'page' type with the following values:
  'slug': 'finn'; 'pageContent': reference to the 'pageContent' entry created in the previous step.

## Error handling

Content should be uniquely identified by chosen field. If there is more than one entity of chosen type with the same query field Contentful will return all of them but this service execution will result in `CompletableFuture` completed exceptionally by throwing `CmsServiceException` informing about non-unique identifier used.

If content is requested for a specific locale that is not defined for the space this implementation will respond
with 'CmsServiceException' with adequate message.
