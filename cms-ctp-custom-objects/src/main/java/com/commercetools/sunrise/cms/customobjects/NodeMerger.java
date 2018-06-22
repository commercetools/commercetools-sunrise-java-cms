package com.commercetools.sunrise.cms.customobjects;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

class NodeMerger {

    public JsonNode merge(final JsonNode source, final JsonNode target) {
        if (source.getNodeType().equals(target.getNodeType())) {
            switch (source.getNodeType()) {
                case STRING:
                    return source.deepCopy();
                case ARRAY: {
                    final ArrayNode mergedNode = target.deepCopy();
                    mergedNode.addAll((ArrayNode) source);
                    return mergedNode;
                }
                case OBJECT: {
                    final ObjectNode mergedNode = target.deepCopy();
                    for (Iterator<String> iter = mergedNode.fieldNames(); iter.hasNext(); ) {
                    final String fieldName = iter.next();
                        final JsonNode sourceChild = source.get(fieldName);
                        if (sourceChild != null) {
                            final JsonNode mergedChild = merge(sourceChild, mergedNode.get(fieldName));
                            mergedNode.set(fieldName, mergedChild);
                        }
                    }

                    final Set<String> fieldsToCopy = new HashSet<String>();
                    source.fieldNames().forEachRemaining(fieldsToCopy::add);
                    target.fieldNames().forEachRemaining(fieldsToCopy::remove);
                    fieldsToCopy.forEach(f -> mergedNode.set(f, source.get(f)));

                    return mergedNode;
                }
            }
        }
        return target.deepCopy();
    }
}
