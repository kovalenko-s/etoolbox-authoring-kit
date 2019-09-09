/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
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
package com.exadel.aem.toolkit.core.handlers.widget;

import java.lang.reflect.Field;
import java.time.DateTimeException;
import java.time.format.DateTimeFormatter;
import java.time.temporal.Temporal;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.common.TypeHint;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DatePicker;
import com.exadel.aem.toolkit.api.annotations.widgets.datepicker.DateTimeValue;
import com.exadel.aem.toolkit.core.exceptions.ValidationException;
import com.exadel.aem.toolkit.core.handlers.Handler;
import com.exadel.aem.toolkit.core.maven.PluginRuntime;
import com.exadel.aem.toolkit.core.util.DialogConstants;
import com.exadel.aem.toolkit.core.util.validation.Validation;

public class DatePickerHandler implements Handler, BiConsumer<Element, Field> {
    private static final String INVALID_FORMAT_EXCEPTION_TEMPLATE = "Invalid %s '%s' for @DatePicker field '%s'";
    private static final String INVALID_VALUE_EXCEPTION_TEMPLATE = "Property '%s' of @DatePicker does not correspond to specified valueFormat";

    @Override
    public void accept(Element element, Field field) {
        DatePicker datePickerAttribute = field.getAnnotationsByType(DatePicker.class)[0];
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ISO_DATE_TIME;

        // check specified typeHint, report if invalid
        if (datePickerAttribute.typeHint() == TypeHint.STRING) {
            element.setAttribute(DialogConstants.PN_TYPE_HINT, datePickerAttribute.typeHint().toString());
        } else if (datePickerAttribute.typeHint() != TypeHint.NONE) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(
                    INVALID_FORMAT_EXCEPTION_TEMPLATE,
                    "typeHint",
                    datePickerAttribute.typeHint(),
                    element.getTagName()));
            return;
        }
        // for a String-storing field, check and process specified valueFormat, report if invalid
        if (datePickerAttribute.typeHint() == TypeHint.STRING
            && !StringUtils.isEmpty(datePickerAttribute.valueFormat())) {
            try {
                // Java8 DateTimeFormatter interprets D as 'day of year', unlike Coral engine
                // so a primitive replacement here to make sure 'DD' as in 'YYYY-MM-DD' is not passed to formatter
                String patchedValueFormat = datePickerAttribute.valueFormat().replaceAll("\\bD{1,2}\\b", "dd");
                dateTimeFormatter = DateTimeFormatter.ofPattern(patchedValueFormat);
            } catch (IllegalArgumentException e) {
                PluginRuntime.context().getExceptionHandler().handle(new ValidationException(
                        INVALID_FORMAT_EXCEPTION_TEMPLATE,
                        "valueFormat",
                        datePickerAttribute.valueFormat(),
                        element.getTagName()));
                return;
            }
        }
        // store values with specified or default formatting
        storeDateValue(element, DialogConstants.PN_MIN_DATE, datePickerAttribute.minDate(), dateTimeFormatter);
        storeDateValue(element, DialogConstants.PN_MAX_DATE, datePickerAttribute.maxDate(), dateTimeFormatter);
    }

    private void storeDateValue(
            Element element,
            String attribute,
            DateTimeValue value,
            DateTimeFormatter formatter
    ) {
        if (isEmptyDateTime(value)) {
            return;
        }
        Object thisDateTime = Validation.forMethod(DatePicker.class, attribute).getFilteredValue(value);
        if (thisDateTime == null || !ClassUtils.isAssignable(thisDateTime.getClass(), Temporal.class)) {
            return;
        }
        try {
            getXmlUtil().setAttribute(element, attribute, formatter.format((Temporal)thisDateTime));
        } catch (DateTimeException e) {
            PluginRuntime.context().getExceptionHandler().handle(new ValidationException(
                    INVALID_VALUE_EXCEPTION_TEMPLATE,
                    attribute));
        }
    }

    private static boolean isEmptyDateTime(DateTimeValue value) {
        return StringUtils.isEmpty(value.timezone())
                && value.minute() == 0
                && value.hour() == 0
                && value.day() == 0
                && value.month() == 0
                && value.year() == 0;
    }
}
