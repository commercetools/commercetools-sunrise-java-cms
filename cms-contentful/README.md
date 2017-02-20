Sunrise Java Contentful CMS
===========================

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/com.commercetools.sunrise.cms/cms-api/badge.svg)](http://search.maven.org/#search|gav|1|g:"com.commercetools.sunrise.cms"%20AND%20a:"cms-contentful")

Module for [Sunrise Java](https://github.com/sphereio/commercetools-sunrise-java)
that serves as an adapter for Contentful CMS platform providing read access to its content.

## Content model

The best way to use Sunrise Java Contentful CMS is to use it in common with following content model:
* every page should have it's separate entry (page entry) with only required fields.
* every page entry should belong to separate wrapper entry.
Wrapper entry type should contain two fields - one entry field of type 'symbol' (short text)
for identifying page entry, and one entry field of type 'reference', which points to
right entry page.


## How to use it

To implement Contentful data into sunrise project, 
one need to create ContentfulCmsService object using API token, space id 
along with information about wrapper entry: name of it's type (pageTypeName),
and id field (pageTypeIdFieldName).

`ContentfulCmsService.of("spaceId", "token", "pageTypeName", "pageTypeIdFieldName");`

For retrieving content for the whole page use:
`cmsService.page("pageKey", localesList);`,
where 'pageKey' is the value of identifying field.

Currently this module doesn't support "Location" entry type,
neither array types(array of text fields, media fields, list of entries).
Every other values are achievable using `CmsPage` method `Optional<String> field(final String fieldName);` 
where `fieldName` is a path to correct field.
E.g. `banner.image.url`. Separated values are ids of succeeding entry fields.
