package org.springframework.batch.core.configuration.xml;

import java.util.ArrayList;
import java.util.List;

import org.springframework.batch.core.listener.AbstractListenerFactoryBean;
import org.springframework.batch.core.listener.ListenerMetaData;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.ManagedMap;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;

/**
 * @author Dan Garrette
 * @since 2.0
 */
public abstract class AbstractListenerParser {

	@SuppressWarnings("unchecked")
	public AbstractBeanDefinition parse(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder listenerBuilder = BeanDefinitionBuilder.genericBeanDefinition(getFactoryClass());
		String id = element.getAttribute("id");
		String listenerRef = element.getAttribute("ref");
		String className = element.getAttribute("class");
		checkListenerElementAttributes(parserContext, element, id, listenerRef, className);

		if (StringUtils.hasText(listenerRef)) {
			listenerBuilder.addPropertyReference("delegate", listenerRef);
		}
		else if (StringUtils.hasText(className)) {
			RootBeanDefinition beanDef = new RootBeanDefinition(className, null, null);
			listenerBuilder.addPropertyValue("delegate", beanDef);
		}
		else {
			parserContext.getReaderContext().error(
					"Neither 'ref' or 'class' specified for <" + element.getTagName() + "> element", element);
		}

		ManagedMap metaDataMap = new ManagedMap();
		for (String metaDataPropertyName : getMethodNameAttributes()) {
			String listenerMethod = element.getAttribute(metaDataPropertyName);
			if (StringUtils.hasText(listenerMethod)) {
				metaDataMap.put(metaDataPropertyName, listenerMethod);
			}
		}
		listenerBuilder.addPropertyValue("metaDataMap", metaDataMap);

		return listenerBuilder.getBeanDefinition();
	}

	private void checkListenerElementAttributes(ParserContext parserContext, Element element, String id,
			String listenerRef, String className) {
		if ((StringUtils.hasText(id) || StringUtils.hasText(className)) && StringUtils.hasText(listenerRef)) {
			NamedNodeMap attributeNodes = element.getAttributes();
			StringBuilder attributes = new StringBuilder();
			for (int i = 0; i < attributeNodes.getLength(); i++) {
				if (i > 0) {
					attributes.append(" ");
				}
				attributes.append(attributeNodes.item(i));
			}
			parserContext.getReaderContext().error(
					"Both 'ref' and " + (StringUtils.hasText(id) ? "'id'" : "'class'")
							+ " specified; use 'class' with an optional 'id' or just 'ref' for <"
							+ element.getTagName() + "> element specified with attributes: " + attributes, element);
		}
	}

	private List<String> getMethodNameAttributes() {
		List<String> methodNameAttributes = new ArrayList<String>();
		for (ListenerMetaData metaData : getMetaDataValues()) {
			methodNameAttributes.add(metaData.getMethodName());
		}
		return methodNameAttributes;
	}

	protected abstract Class<? extends AbstractListenerFactoryBean> getFactoryClass();

	protected abstract ListenerMetaData[] getMetaDataValues();

}