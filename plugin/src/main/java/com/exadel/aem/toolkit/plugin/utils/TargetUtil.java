package com.exadel.aem.toolkit.plugin.utils;

import org.apache.commons.lang3.StringUtils;

import com.exadel.aem.toolkit.api.annotations.meta.ResourceTypes;
import com.exadel.aem.toolkit.api.handlers.Target;

/**
 * Contains utility methods for manipulation with {@link com.exadel.aem.toolkit.api.handlers.Target}
 */
public class TargetUtil {

    private TargetUtil() {
    }

    /**
     * Gets whether the currently rendered XML element is a Granite {@code Multifield} element (judged by the fact
     * it has an appropriate resource type and a non-empty subnode named "items")
     * @param target {@code Target} instance
     * @return true or false
     */
    public static boolean isMultifield(Target target) {
        if (StringUtils.equals(target.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE), ResourceTypes.MULTIFIELD)) {
            return true;
        }
        boolean hasSingularFieldNode = target.getChildren().size() == 1
            && DialogConstants.NN_FIELD.equals(target.getChildren().get(0).getName());
        if (!hasSingularFieldNode) {
            return false;
        }
        // Assuming this is a custom multifield, i.e. the target does not match any of the known resource types
        return ClassUtil.getConstantValues(ResourceTypes.class).values()
            .stream()
            .map(Object::toString)
            .noneMatch(restype -> restype.equals(target.getAttribute(DialogConstants.PN_SLING_RESOURCE_TYPE)));
    }
}
