Sunrise Java CMS :sunrise: :pencil2:
==================

[![Build Status](https://travis-ci.org/commercetools/commercetools-sunrise-java-cms.svg?branch=master)](https://travis-ci.org/commercetools/commercetools-sunrise-java-cms) [![Stories in Ready](https://badge.waffle.io/commercetools/commercetools-sunrise-java-cms.png?label=ready&title=Ready)](https://waffle.io/commercetools/commercetools-sunrise-java-cms)

Module for [Sunrise Java](https://github.com/sphereio/commercetools-sunrise-java)
defining unified API to fetch content from different CMS.

* [Javadoc](https://commercetools.github.io/commercetools-sunrise-java-cms/javadoc/index.html)

## Installation
Choose the CMS adapter and follow its installation instructions:

- [Contentful Sunrise Adapter](/cms-contentful)

Note that if you are not using a Sunrise-based project, you'll additionally need to use as dependency:
```
libraryDependencies += "com.commercetools.sunrise.cms" % "cms-api" % "0.1.0"
```

## Architecture

This API consists of five abstractions that reflect CMS hierarchical content structure:

* page
* field
* entry
* array
* path

A page represents uniquely identifiable and thus searchable entity.
It can be defined for several locales and access to all its
localized instances is possible using this API.
It is convenient to see a page as a subtree of underlying CMS with its
hierarchy of traversable fields.

A field is contained inside a page and is identifiable inside it by
a path. It constitutes a single string-representable piece of content
that can be accessed using this API.

An entry is an independent identifiable piece of content that can be requested by this service as a page.
In the repository it can be nested inside other entries or arrays to form more complex trees of content.

An array, non-identifiable in itself and thus can't be fetched as a page. Can be nested inside entries. Provides index-based access to it's items.
Depending on content model array item might be a directly retrievable field or an entry.

A path is a field identifier in the scope of given page.
Because of hierarchical structure of underlying content this identifier
is expected to form "path like" syntax which resembles XPath and will be
explained in the following sections.

### Tree structure

This section describes accessing a sample repository.

![Hierarchical Content](doc/tree.png)

There are three entries that can be requested by this service and fetched as traversable `pages`.

Let's say we'd like to fetch **`entry1`**.

```Java
Optional<CmsPage> pageOpt = service.page("entry1", locales);
```

A `CmsPage` model is created as the result of that operation and its subtree content is available by invoking:

```Java
Optional<String> fieldOpt = page.field("path.to.a.field");
```

In order for `field` operation to succeed given path has to refer to one of the leaves of the tree.
Any intermediary nodes are not representable as strings.

Here are some of correct paths:

field | path | result
----- | ---- | ------
locationField1 | `locationField1` | "{long=19.62, lat=51.37}"
locationField2 | `entry3.locationField2` | "{long=21.62, lat=11.37}"
imageAssetField2 | `array1[0].entry2.imageAssetField2` | "http://host/file2.img"
array2's 1. item | `entry1.array1[1]` | "text2"

Sample invalid paths are:

path | reason of failure
---- | -----------------
`array1` | refers to whole array
`array1[0]` | refers to whole entry
`array1.entry2[0]` | entry2 forms an entry, not an array
`array1[2]` | index out of bounds in array
`array1[1]` | refers to an array
`array1[0].nonExistingField` | no such field

Empty result is returned for all of those paths.

When service is requested to fetch page `entry2` the only correct paths are: `textField1` and `imageAssetField2` as there are no other fields or entries in this subtree.

## Error handling

`CmsServiceException` is thrown in the following situations:

- requested page is not unique - there are more than one results for given page identifier in repository
- problems with data transfer - forbidden access, request badly formed etc.

## Locale

Underlying repository might provide localization of content. To leverage that this service can provide a page
for the list of locales. The first locale on the list is matched against the repository content.
If it's missing the second one is matched and so forth. An empty list can be provided and the behavior is 
dependent on underlying implementation.
