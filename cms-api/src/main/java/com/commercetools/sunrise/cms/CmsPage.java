package com.commercetools.sunrise.cms;

import java.util.Optional;

/**
 * A hierarchical read-only collection of cms nodes that provides access to their string representations.
 * <p>
 * Cms page is a subtree of underlying CMS repository.
 * <p>
 * User of {@link CmsPage} is expected to know the structure of objects in cms and provide proper path to access
 * any field within fetched cms page.
 */
public interface CmsPage {

    /**
     * Get string representation of the content of cms node identified by the given path.
     * <p>
     * Path should form the string of several identifiers - cms node names - delimited with '.' character.
     * Each identifier might be appended with array integer index, e.g. '[3]'.
     * <p>
     * The path constructed in such way is used to traverse a {@link CmsPage} contained in this object.
     * <p>
     * E.g. firstLevel[2].secondLevel[3].thirdLevel[1] executed on this method should result in looking for third item
     * in 'firstLevel' node and if it is found then searching it for fourth item in its 'secondLevel' node.
     * Finally, if it is found then this item is searched for the array node identified with 'thirdLevel'
     * and retrieving second item from it. This last item should have a string representation.
     * <p>
     * If any step of this process fails empty optional is returned.
     *
     * @param path identifying the field (e.g. banner[2].image[2])
     * @return content of node identified by the path, or absent if not found
     */
    Optional<String> field(String path);

    /**
     * Get string representation of the content of cms node identified by the given path or empty string if not found.
     * <p>
     * Path should form the string of several identifiers - cms node names - delimited with '.' character.
     * Each identifier might be appended with array integer index, e.g. '[3]'.
     * <p>
     * The path constructed in such way is used to traverse a {@link CmsPage} contained in this object.
     * <p>
     * E.g. firstLevel[2].secondLevel[3].thirdLevel[1] executed on this method should result in looking for third item
     * in 'firstLevel' node and if it is found then searching it for fourth item in its 'secondLevel' node.
     * Finally, if it is found then this item is searched for the array node identified with 'thirdLevel'
     * and retrieving second item from it. This last item should have a string representation.
     * <p>
     * If any step of this process fails empty string is returned.
     *
     * @param path identifying the field (e.g. banner[2].image[2])
     * @return content of node identified by the path, or empty string if not found
     */
    default String fieldOrEmpty(String path) {
        return field(path).orElse("");
    }
}
