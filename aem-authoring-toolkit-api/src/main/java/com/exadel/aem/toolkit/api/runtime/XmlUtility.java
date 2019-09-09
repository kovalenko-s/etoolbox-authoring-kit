package com.exadel.aem.toolkit.api.runtime;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.Map;
import java.util.function.BinaryOperator;

import org.w3c.dom.Element;

import com.exadel.aem.toolkit.api.annotations.widgets.attribute.Data;
import com.exadel.aem.toolkit.api.annotations.widgets.common.XmlScope;

/**
 * An abstraction of class encapsulating routines for XML generation and handling
 */
public interface XmlUtility {
    /**
     * Creates named XML {@code Element} node with default JCR type
     * @param name Tag name of the XML node
     * @return {@code Element} instance
     */
    Element createNodeElement(String name);

    /**
     * Creates named XML {@code Element} node with default JCR type and specific {@code sling:resourceType}
     * @param name Tag name of the XML node
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String resourceType);

    /**
     * Creates named XML {@code Element} node with default JCR type and additional properties
     * @param name Tag name of the XML node
     * @param properties {@code Map} of String values to be rendered as additional XML node attributes
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Map<String,String> properties);

    /**
     * Creates named XML {@code Element} node with specific JCR type and optional properties
     * @param name Tag name of the XML node
     * @param nodeType Value of {@code jcr:primaryType} attribute
     * @param properties {@code Map} of String values to be rendered as additional XML node attributes
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String nodeType, Map<String,String> properties);

    /**
     * Creates named XML {@code Element} node with default JCR type,
     * additional properties, and specific {@code sling:resourceType}
     * @param name Tag name of the XML node
     * @param properties {@code Map} of String values to be rendered as additional XML node attributes
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Map<String,String> properties, String resourceType);

    /**
     * Creates named XML {@link Element} node with specific JCR type and optional properties
     * @param name Tag name of the XML node
     * @param nodeType Value of {@code jcr:primaryType} attribute
     * @param properties {@link Map} of String values to be rendered as additional XML node attributes
     * @param resourceType Value of {@code sling:resourceType} attribute
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, String nodeType, Map<String, String> properties, String resourceType);

    /**
     * Creates named XML {@code Element} node from existing {@code Annotation} instance
     * @param name Tag name of the XML node
     * @param source Annotation to be rendered to XML
     * @return {@code Element} instance
     */
    Element createNodeElement(String name, Annotation source);

    /**
     * Stores Double-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param value Value to set
     */
    void setAttribute(Element element, String name, Double value);

    /**
     * Stores Long-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param value Value to set
     */
    void setAttribute(Element element, String name, Long value);

    /**
     * Stores Boolean-typed value as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param value Value to set
     */
    void setAttribute(Element element, String name, Boolean value);

    /**
     * Stores String value as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param value Value to set
     */
    void setAttribute(Element element, String name, String value);

    /**
     * Stores list of String values as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param values Values to set
     */
    void setAttribute(Element element, String name, List<String> values);

    /**
     * Stores list of String values as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name
     * @param values Values to set
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    void setAttribute(Element element, String name, List<String> values, BinaryOperator<String> attributeMerger);

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name, same as annotation property name
     * @param source Annotation to look for a value in
     */
    void setAttribute(Element element, String name, Annotation source);

    /**
     * Stores property value of a specific {@code Annotation} as an XML attribute
     * @param element {@code Element} node
     * @param name Attribute name, same as annotation property name
     * @param source Annotation to look for a value in
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     */
    void setAttribute(Element element, String name, Annotation source, BinaryOperator<String> attributeMerger);

    /**
     * Populates {@code Element} node with all eligible property values of an {@code Annotation} instance
     * @param element Element node
     * @param annotation Annotation to take property values from
     */
    void mapProperties(Element element, Annotation annotation);

    /**
     * Populates {@code Element} node with all eligible property values of an {@code Annotation} instance,
     * honoring {@link XmlScope} specified for an annotation or a particular annotation method
     * @param element Element node
     * @param annotation Annotation to take property values from
     */
    void mapProperties(Element element, Annotation annotation, XmlScope scope);

    /**
     * Populates {@code Element} node with property values of an {@code Annotation} instance,
     * skipping specified annotation fields
     * @param element Element node
     * @param annotation Annotation to take property values from
     * @param skippedFields List of field names to skip
     */
    void mapProperties(Element element, Annotation annotation, List<String> skippedFields);

    /**
     * Tries to append provided {@code Element} node as a child to a parent {@code Element} node.
     * If child node with same name already exists, it is updated with attribute values of the newcoming node
     * @param parent Routine than provides Element to serve as parent
     * @param child Element to serve as child
     * @return Appended child
     */
    Element appendChild(Element parent, Element child);

    /**
     * Tries to append provided {@code Element} node as a child to a parent {@code Element} node.
     * If child node with same name already exists, it is updated with attribute values of the newcoming node
     * @param parent Element to serve as parent
     * @param child Element to serve as child
     * @param attributeMerger Function that manages an existing attribute value and a new one
     *                        in case when a new value is set to an existing {@code Element}
     * @return Appended child
     */
    Element appendChild(Element parent, Element child, BinaryOperator<String> attributeMerger);

    /**
     * Appends to the current {@code Element} node a child {@code datasource} node bearing link to an ACS Commons list
     * @param element Element to store data in
     * @param path Path to ACS Commons List in JCR repository
     * @param resourceType <Optional>Use this to set {@code sling:resourceType} of data source, other than standard</Optional>
     */
    void appendAcsCommonsList(Element element, String path, String resourceType);

    /**
     * Appends {@link Data} values to an {@code Element} node, storing them within {@code granite:data} predefined subnode
     * @param element Element to store data in
     * @param data Provided values as an array of {@code Data} annotations
     */
    void appendDataAttributes(Element element, Data[] data);

    /**
     * Appends values to an {@code Element} node, storing them within {@code granite:data} predefined subnode
     * @param element Element to store data in
     * @param data Provided values as a {@code Map<String, String>} instance
     */
    void appendDataAttributes(Element element, Map<String, String> data);


    /**
     * Retrieve child {@code Element} node of the specified node by name
     * @param parent Element to analyze
     * @param childName Name of child to look for
     * @return Element instance or null value
     */
    Element getChildElementNode(Element parent, String childName);

    /**
     * Generates compliant XML tag name from an arbitrary string
     * @param name Raw (unchecked) string for a tag name
     * @return Valid tag name
     */
    String getValidName(String name);
    /**
     * Generates compliant XML tag name from an arbitrary string
     * @param name Raw (unchecked) string for a tag name
     * @param invalidNamePattern Regex string {@param name} to be tested against in order to skip invalid characters
     * @param defaultValue Fallback name to be used if {@param name} is either blank or is reduced to blank as a result of testing
     * @return Valid tag name
     */
    String getValidName(String name, String invalidNamePattern, String defaultValue);

    /**
     * Generates compliant XML tag name thai is unique within the scope of specified parent node
     * @param name Raw (unchecked) string for a tag name
     * @param context Parent node
     * @return Valid and locally unique tag name
     */
    String getUniqueName(String name, Element context);

    /**
     * Generates compliant XML tag name thai is unique within the scope of specified parent node
     * @param name Raw (unchecked) string for a tag name
     * @param invalidNamePattern Regex string {@param name} to be tested against in order to skip invalid characters
     * @param defaultValue Fallback name to be used if {@param name} is either blank or is reduced to blank as a result of testing
     * @param context Parent node
     * @return Valid and locally unique tag name
     */
    String getUniqueName(String name, String invalidNamePattern, String defaultValue, Element context);
}