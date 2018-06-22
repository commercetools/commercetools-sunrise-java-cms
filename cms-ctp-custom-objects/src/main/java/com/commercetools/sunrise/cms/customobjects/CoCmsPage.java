package com.commercetools.sunrise.cms.customobjects;

import com.commercetools.sunrise.cms.CmsPage;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.sphere.sdk.customobjects.CustomObject;
import io.sphere.sdk.json.SphereJsonUtils;

import java.util.Optional;

public class CoCmsPage implements CmsPage {
    private final static NodeMerger MERGER = new NodeMerger();
    private final ObjectNode objectNode;

    public CoCmsPage(CustomObject<JsonNode> co) {
        ObjectNode target = (ObjectNode) (co.getValue()).get("content");
        final ArrayNode dependencies = (ArrayNode) (co.getValue()).get("dependencies");
        for (final JsonNode dependency : dependencies) {
            final JsonNode dependencyContent = dependency.at("/obj/value/content");
            if (dependencyContent != null) {
                target = (ObjectNode) MERGER.merge(dependencyContent, target);
            }
        }
        this.objectNode = target;
        System.err.println(SphereJsonUtils.prettyPrint(objectNode));
    }

    @Override
    public Optional<String> field(String path) {
        final JsonNode at = objectNode.at(transformCmsPathToJsonPointer(path));
        return Optional.ofNullable(at).map(JsonNode::asText);
    }

    private String transformCmsPathToJsonPointer(String path) {
        return "/" + path.replace("].", "/").replace('.', '/').replace('[', '/');
    }
}
