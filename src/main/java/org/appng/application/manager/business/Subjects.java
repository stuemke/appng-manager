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

import java.util.List;

import org.appng.api.ActionProvider;
import org.appng.api.BusinessException;
import org.appng.api.DataContainer;
import org.appng.api.DataProvider;
import org.appng.api.Environment;
import org.appng.api.FieldProcessor;
import org.appng.api.Options;
import org.appng.api.Platform;
import org.appng.api.Request;
import org.appng.api.SiteProperties;
import org.appng.api.model.Application;
import org.appng.api.model.Site;
import org.appng.api.model.Subject;
import org.appng.application.manager.MessageConstants;
import org.appng.application.manager.form.SubjectForm;
import org.appng.application.manager.service.Service;
import org.appng.application.manager.service.ServiceAware;
import org.appng.core.domain.SubjectImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Lazy;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Provides CRUD-operations for a {@link SubjectImpl}.
 * 
 * @author Matthias Müller
 * 
 */

@Lazy
@Component
@Scope("request")
public class Subjects extends ServiceAware implements DataProvider, ActionProvider<SubjectForm> {
	private static final Logger log = LoggerFactory.getLogger(Subjects.class);
	private static final String SUBJECT = "subject";

	public void perform(Site site, Application application, Environment environment, Options options, Request request,
			SubjectForm valueHolder, FieldProcessor fp) {
		String action = getAction(options);
		String errorMessage = null;
		String okMessage = null;
		Service service = getService();
		Integer subjectId = request.convert(options.getOptionValue(SUBJECT, ID), Integer.class);
		try {
			if (ACTION_CREATE.equals(action)) {
				errorMessage = MessageConstants.SUBJECT_CREATE_ERROR;
				service.createSubject(environment.getLocale(), valueHolder, fp);
				okMessage = MessageConstants.SUBJECT_CREATED;
			} else if (ACTION_UPDATE.equals(action)) {
				SubjectImpl subject = valueHolder.getSubject();
				subject.setId(subjectId);
				okMessage = MessageConstants.SUBJECT_UPDATED;
				errorMessage = MessageConstants.SUBJECT_UPDATE_ERROR;
				Boolean isUpdated = service.updateSubject(valueHolder, fp);
				if (isUpdated) {
					String passwordMessage = request.getMessage(MessageConstants.SUBJECT_PASSWORD_UPDATED, subjectId);
					fp.addOkMessage(passwordMessage);
					okMessage = null;
				}
			} else if (ACTION_DELETE.equals(action)) {
				errorMessage = MessageConstants.SUBJECT_DELETE_ERROR;
				Subject currentSubject = environment.getSubject();
				service.deleteSubject(currentSubject, subjectId, fp);
				okMessage = MessageConstants.SUBJECT_DELETED;
			}
			if (null != okMessage) {
				String message = request.getMessage(okMessage, subjectId);
				fp.addOkMessage(message);
			}
		} catch (BusinessException ex) {
			String message = request.getMessage(errorMessage, subjectId);
			log.error(message, ex);
			fp.addErrorMessage(message);
		}
	}

	public DataContainer getData(Site site, Application application, Environment environment, Options options,
			Request request, FieldProcessor fp) {
		Service service = getService();
		Integer subjectId = request.convert(options.getOptionValue(SUBJECT, ID), Integer.class);
		List<String> languages = site.getProperties().getList(SiteProperties.SUPPORTED_LANGUAGES, ",");
		String defaultTimezone = site.getProperties().getString(Platform.Property.TIME_ZONE);
		DataContainer data = null;
		if (subjectId == null && ACTION_CREATE.equals(getAction(options))) {
			data = service.getNewSubject(fp, defaultTimezone, languages);
		} else {
			try {
				data = service.searchSubjects(request, fp, subjectId, defaultTimezone, languages);
			} catch (BusinessException ex) {
				String message = request.getMessage(ex.getMessageKey(), ex.getMessageArgs());
				log.error(message, ex);
				fp.addErrorMessage(message);
			}
		}

		return data;
	}

}
