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
package com.exadel.aem.toolkit.plugin.handlers.common;

import java.lang.annotation.Annotation;
import java.util.function.BiConsumer;

import com.exadel.aem.toolkit.api.handlers.Source;
import com.exadel.aem.toolkit.api.handlers.Target;
import com.exadel.aem.toolkit.plugin.maven.PluginRuntime;

/**
 * Implements {@code BiConsumer} to populate a {@link Target} instance via calls to custom handlers attached to the
 * annotations adaptable from the provided {@link Source}
 */
public class CustomHandlingHandler implements BiConsumer<Source, Target> {

    /**
     * Processes data that can be extracted from the given {@code Source} and stores in into the provided {@code Target}
     * @param source {@code Source} object to get data
     * @param target Resulting {@code Target} object
     */
    @Override
    public void accept(Source source, Target target) {
        PluginRuntime
            .context()
            .getReflection()
            .getHandlers(target.getScope(), source.adaptTo(Annotation[].class))
            .forEach(handler -> handler.accept(source, target));
    }
}
