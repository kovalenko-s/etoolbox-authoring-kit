/*
 * Licensed under the Apache License, Version 2.0 (the "License").
 * You may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.exadel.aem.toolkit.core.lists.services.impl;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.sling.api.resource.LoginException;
import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.api.resource.ResourceResolverFactory;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.common.collect.ImmutableMap;
import com.day.cq.commons.jcr.JcrConstants;
import com.day.cq.wcm.api.Page;
import com.day.cq.wcm.api.PageManager;

import com.exadel.aem.toolkit.api.lists.models.SimpleListItem;
import com.exadel.aem.toolkit.api.lists.services.ListHelper;
import com.exadel.aem.toolkit.core.CoreConstants;
import com.exadel.aem.toolkit.core.lists.models.SimpleListItemImpl;

/**
 * Implements {@link ListHelper} interface to provide an OSGi service that assists in retrieving AEMBox Lists values
 */
@Component(service = ListHelper.class)
public class ListHelperImpl implements ListHelper {
    private static final Logger LOG = LoggerFactory.getLogger(ListHelper.class);

    private static final Map<Class<?>, Class<?>> INTERFACE_ADAPTATIONS = ImmutableMap.of(
        SimpleListItem.class, SimpleListItemImpl.class
    );

    @Reference
    ResourceResolverFactory resourceResolverFactory;

    /* ------------------------
       Public interface members
       ------------------------ */

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> List<T> getList(String path, Class<T> itemType) {
        return getItemsStream(path)
            .map(getMapperFunction(itemType))
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, String> getMap(String path) {
        return getMap(
            path,
            JcrConstants.JCR_TITLE,
            resource -> resource.getValueMap().get(CoreConstants.PN_VALUE, StringUtils.EMPTY));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public <T> Map<String, T> getMap(String path, String keyName, Class<T> itemType) {
        return getMap(path, keyName, getMapperFunction(itemType));
    }

    /* ---------------
       Utility methods
       --------------- */

    /**
     * Retrieves a {@code Map} collected from list entries under the provided path. Map keys are defined by
     * the {@code keyName}, while the values are defined by the provided {@code mapper} function
     * @param path     JCR path of the items list
     * @param keyName  Item resource property that holds the key of the resulting map
     * @param mapper   {@code Function} that converts a list item resource into the object of required type
     * @param <T>      Type of map values
     * @return Map containing {@code <T>}-typed instances
     */
    private <T> Map<String, T> getMap(String path, String keyName, Function<Resource, T> mapper) {
        return getItemsStream(path)
            .map(resource -> new ImmutablePair<>(
                resource.getValueMap().get(keyName, StringUtils.EMPTY),
                mapper.apply(resource)))
            .filter(pair -> pair.getRight() != null)
            .collect(Collectors.toMap(
                ImmutablePair::getLeft,
                ImmutablePair::getRight,
                (a, b) -> b,
                LinkedHashMap::new));
    }

    /**
     * Retrieves a {@code ResourceResolver} instance and gets  a sequence of {@code Resource}s under the provided path
     * as a Java {@code Stream} for further processing
     * @param path JCR path of the items list
     * @return {@code Stream} or resource objects, or an empty stream
     */
    private Stream<Resource> getItemsStream(String path) {
        ResourceResolver resourceResolver = getResourceResolver();
        if (resourceResolver == null) {
            return Stream.empty();
        }
        Resource listResource = getListResource(path, resourceResolver);
        Stream<Resource> result = listResource != null
            ? StreamSupport.stream(Spliterators.spliteratorUnknownSize(listResource.listChildren(), Spliterator.ORDERED), false)
            : Stream.empty();
        resourceResolver.close();
        return result;
    }

    /**
     * Creates a {@code ResourceResolver} instance bound to AEM Authoring Toolkit services
     * @return {@code ResourceResolver} object, or null
     */
    private ResourceResolver getResourceResolver() {
        try {
            return resourceResolverFactory.getResourceResolver(
                Collections.singletonMap(ResourceResolverFactory.SUBSERVICE, CoreConstants.SUBSERVICE_READ));
        } catch (LoginException e) {
            LOG.error(e.getMessage());
        }
        return null;
    }

    /**
     * Retrieves an AEMBox List page resource by the provided JCR {@code path}
     * @param path JCR path of the items list
     * @param resourceResolver {@code ResourceResolver} used to retrieve the resource
     * @return {@code Resource object, or null if the resource resolver is missing or the path in unresolvable}
     */
    private static Resource getListResource(String path, ResourceResolver resourceResolver) {
        if (resourceResolver == null || StringUtils.isBlank(path)) {
            return null;
        }
        return Optional.of(resourceResolver)
            .map(resolver -> resolver.adaptTo(PageManager.class))
            .map(pageManager -> pageManager.getPage(path))
            .map(Page::getContentResource)
            .map(contentRes -> contentRes.getChild("list"))
            .orElse(null);
    }


    /**
     * Retrieves a {@code Function} used to convert a {@code Resource} object into the required value type
     * @param itemType {@code Class} reference representing type of entries required
     * @param <T>      Type of value
     * @return {@code Function} object
     */
    private static <T> Function<Resource, T> getMapperFunction(Class<T> itemType) {
        return getMapperFunction(resource -> resource.adaptTo(itemType), itemType);
    }

    /**
     * Retrieves a {@code Function} used to convert a {@code Resource} object into the required value type
     * @param source   Source {@code Function} object that may need further amending
     * @param itemType {@code Class} reference representing required type of items
     * @param <T>      Type of items
     * @return Amended {@code Function} object
     */
    private static <T> Function<Resource, T> getMapperFunction(Function<Resource, T> source, Class<T> itemType) {
        Function<Resource, T> mapper = source;
        if (Resource.class.equals(itemType)) {
            mapper = itemType::cast;
        } else if (INTERFACE_ADAPTATIONS.containsKey(itemType)) {
            mapper = resource -> itemType.cast(resource.adaptTo(INTERFACE_ADAPTATIONS.get(itemType)));
        }
        return mapper;
    }
}
