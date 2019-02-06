/*
 * Copyright (c) 2011 HawkinsSoftware
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Byron Hawkins of HawkinsSoftware
 */
package org.hawkinssoftware.rns.core.role;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.role.DomainSpecifications.OrthogonalSet;
import org.hawkinssoftware.rns.core.util.RNSLogging.Tag;
import org.hawkinssoftware.rns.core.util.RNSUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * DOC comment task awaits.
 * 
 * @author Byron Hawkins
 */
public class DomainSpecificationsLoader
{
	public static DomainSpecifications load(InputStream in) throws SAXException, IOException, ParserConfigurationException
	{
		Assembly assembly = new Assembly();
		assembly.assemble(in);
		return assembly.specifications;
	}

	/**
	 * DOC comment task awaits.
	 * 
	 * @author Byron Hawkins
	 */
	private static class Assembly
	{
		private final DomainSpecifications specifications = new DomainSpecifications();

		void assemble(InputStream in) throws SAXException, IOException, ParserConfigurationException
		{
			DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			Document document = builder.parse(in);

			Node rootNode = document.getFirstChild();
			while (rootNode != null)
			{
				Node node = rootNode.getFirstChild();
				while (node != null)
				{
					if (node instanceof Element)
					{
						Element entry = (Element) node;
						if (entry.getNodeName().equals("orthogonal-set"))
						{
							addOrthogonalSet(entry);
						}
						else if (entry.getNodeName().equals("domain-containment"))
						{
							addDomainContainment(entry);
						}
						else
						{
							Log.out(Tag.WARNING, "Unknown orthogonal-scope element %s", entry.getNodeName());
						}
					}
					node = node.getNextSibling();
				}
				rootNode = rootNode.getNextSibling();
			}
		}

		private void addOrthogonalSet(Element orthogonalSetElement)
		{
			OrthogonalSet orthogonalSet = new OrthogonalSet();
			Node node = orthogonalSetElement.getFirstChild();
			while (node != null)
			{
				if (node instanceof Element)
				{
					Element entry = (Element) node;
					if (entry.getNodeName().equals("domain"))
					{
						orthogonalSet.domainTypenamesAssembly.add(RNSUtils.makeCanonical(entry.getFirstChild().getNodeValue()));
					}
					else if (entry.getNodeName().equals("package"))
					{
						orthogonalSet.packagePatternsAssembly.add(entry.getFirstChild().getNodeValue());
					}
					else
					{
						Log.out(Tag.CRITICAL, "Unknown orthogonal-set element %s", entry.getNodeName());
					}
				}
				node = node.getNextSibling();
			}
			specifications.orthogonalSetsAssembly.add(orthogonalSet);
		}

		private void addDomainContainment(Element domainContainment)
		{
			String parent = domainContainment.getAttribute("parent-domain");
			String child = domainContainment.getAttribute("child-domain");

			if (parent.length() == 0)
			{
				Log.out(Tag.CRITICAL, "Missing parent domain in <domain-containment> element.");
			}
			if (child.length() == 0)
			{
				Log.out(Tag.CRITICAL, "Missing child domain in <domain-containment> element.");
			}

			specifications.parentDomainByChildDomain.put(RNSUtils.makeCanonical(child), RNSUtils.makeCanonical(parent));
		}
	}
}