/*
 * Copyright 2011-2017 the original author or authors.
 *
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
package org.appng.application.manager.business;

import java.io.IOException;

import org.appng.api.FieldProcessor;
import org.appng.api.ProcessingException;
import org.appng.api.support.CallableAction;
import org.appng.api.support.CallableDataSource;
import org.appng.application.manager.form.PropertyForm;
import org.appng.core.domain.PropertyImpl;
import org.appng.core.domain.SiteImpl;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runners.MethodSorters;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class SitePropertiesTest extends AbstractTest {

	private static final String PROPERTY_EVENT = "propertyEvent";

	private static final String TESTPROPERTY = "testproperty";
	private static final String PROPERTY_NAME = "platform.site.localhost." + TESTPROPERTY;

	@Test
	public void testCreate() throws ProcessingException, IOException {

		SiteImpl site = new SiteImpl();
		site.setName("localhost");
		site.setDomain("localhost");
		site.setHost("localhost");
		siteRepository.save(site);

		ActionCall actionCall = getAction(PROPERTY_EVENT, "create-site-property").withParam(FORM_ACTION,
				"create-site-property").withParam("siteid", "1");
		CallableAction action = actionCall.getCallableAction(new PropertyForm(new PropertyImpl(TESTPROPERTY, "5")));
		FieldProcessor perform = action.perform();
		validate(perform.getMessages());

		actionCall.getCallableAction(new PropertyForm(new PropertyImpl("anotherproperty", "foobar"))).perform();
	}

	@Test
	public void testShowAll() throws ProcessingException, IOException {
		addParameter("sortSite-properties", "id:asc");
		initParameters();
		CallableDataSource dataSource = getDataSource("site-properties").withParam("siteid", "1")
				.getCallableDataSource();
		dataSource.perform("test");
		validate(dataSource.getDatasource());
	}

	@Test
	public void testShowOne() throws ProcessingException, IOException {
		CallableDataSource dataSource = getDataSource("property").withParam("id", PROPERTY_NAME)
				.getCallableDataSource();
		dataSource.perform("test");
		validate(dataSource.getDatasource());
	}

	@Test
	public void testUpdate() throws ProcessingException, IOException {
		ActionCall actionCall = getAction(PROPERTY_EVENT, "update-site-property").withParam(FORM_ACTION,
				"update-site-property").withParam("propertyid", PROPERTY_NAME);
		PropertyImpl property = new PropertyImpl(TESTPROPERTY, "7", "9");
		property.setClob("");
		CallableAction action = actionCall
				.getCallableAction(new PropertyForm(property));
		FieldProcessor perform = action.perform();
		validate(perform.getMessages());

		CallableDataSource dataSource = getDataSource("property").withParam("siteid", "1")
				.withParam("id", PROPERTY_NAME).getCallableDataSource();
		dataSource.perform("test");
		validate(dataSource.getDatasource(), "-data");
	}
}
