Sunrise Java Contentful CMS
===========================

Module for [Sunrise Java](https://github.com/sphereio/commercetools-sunrise-java) with Contentful service. 

### Requirements

Every entry type should have entry text field with id 'name', which needs to be marked as 'Entry title'.

### How to use it

To implement Contentful data into sunrise project, 
one need to create ContentfulCmsService object using API token and space id:

`ContentfulCmsService cmsService = ContentfulCmsService.of("spaceId", "token");`

For retrieving string content use:
`cmsService.get(localesList, cmsIdentifier);`

Currently this module doesn't support "Location" entry type. 
All other entry types are returned as strings.

### Integration

For Contentful CmsIdentifier fields serves as:
* entryType - it is an id of Contentful entry type.
* entryKey - it is entry name. Look [here](.#Requirements).
* fieldName - it is field name, which contains required value or asset.
