/**
 * Copyright (c) 2000-2010 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */

package com.liferay.portal.workflow.kaleo.service.impl;

import com.liferay.portal.kernel.exception.PortalException;
import com.liferay.portal.kernel.exception.SystemException;
import com.liferay.portal.model.User;
import com.liferay.portal.service.ServiceContext;
import com.liferay.portal.workflow.kaleo.model.KaleoInstance;
import com.liferay.portal.workflow.kaleo.model.KaleoInstanceToken;
import com.liferay.portal.workflow.kaleo.model.KaleoNode;
import com.liferay.portal.workflow.kaleo.model.impl.KaleoInstanceTokenImpl;
import com.liferay.portal.workflow.kaleo.service.base.KaleoInstanceTokenLocalServiceBaseImpl;

import java.io.Serializable;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * <a href="KaleoInstanceTokenLocalServiceImpl.java.html"><b><i>View Source</i>
 * </b></a>
 *
 * @author Brian Wing Shun Chan
 */
public class KaleoInstanceTokenLocalServiceImpl
	extends KaleoInstanceTokenLocalServiceBaseImpl {

	public KaleoInstanceToken addKaleoInstanceToken(
			long parentKaleoInstanceTokenId,
			Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		KaleoInstanceToken parentKaleoInstanceToken =
			kaleoInstanceTokenPersistence.findByPrimaryKey(
				parentKaleoInstanceTokenId);
		User user = userPersistence.findByPrimaryKey(
			parentKaleoInstanceToken.getUserId());
		Date now = new Date();

		long kaleoInstanceTokenId = counterLocalService.increment();

		KaleoInstanceToken kaleoInstanceToken =
			kaleoInstanceTokenPersistence.create(kaleoInstanceTokenId);

		kaleoInstanceToken.setGroupId(serviceContext.getScopeGroupId());
		kaleoInstanceToken.setCompanyId(user.getCompanyId());
		kaleoInstanceToken.setUserId(user.getUserId());
		kaleoInstanceToken.setUserName(user.getFullName());
		kaleoInstanceToken.setCreateDate(now);
		kaleoInstanceToken.setModifiedDate(now);
		kaleoInstanceToken.setKaleoDefinitionId(
			parentKaleoInstanceToken.getKaleoDefinitionId());
		kaleoInstanceToken.setKaleoInstanceId(
			parentKaleoInstanceToken.getKaleoInstanceId());
		kaleoInstanceToken.setParentKaleoInstanceTokenId(
			parentKaleoInstanceToken.getKaleoInstanceTokenId());

		setCurrentKaleoNode(
			kaleoInstanceToken,
			parentKaleoInstanceToken.getCurrentKaleoNodeId());

		kaleoInstanceToken.setCompleted(false);

		kaleoInstanceTokenPersistence.update(kaleoInstanceToken, false);

		return kaleoInstanceToken;
	}

	public KaleoInstanceToken completeKaleoInstanceToken(
			long kaleoInstanceTokenId)
		throws PortalException, SystemException {

		KaleoInstanceToken kaleoInstanceToken =
			kaleoInstanceTokenPersistence.findByPrimaryKey(
				kaleoInstanceTokenId);

		kaleoInstanceToken.setCompleted(true);
		kaleoInstanceToken.setCompletionDate(new Date());

		kaleoInstanceTokenPersistence.update(kaleoInstanceToken, false);

		return kaleoInstanceToken;
	}

	public void deleteKaleoInstanceTokensByDefinition(long kaleoDefinitionId)
		throws SystemException {

		kaleoInstanceTokenPersistence.removeByKaleoDefinitionId(
			kaleoDefinitionId);
	}

	public void deleteKaleoInstanceTokensByInstance(long kaleoInstanceId)
		throws SystemException {

		kaleoInstanceTokenPersistence.removeByKaleoInstanceId(kaleoInstanceId);
	}

	public List<KaleoInstanceToken> getKaleoInstanceTokens(
			long parentKaleoInstanceTokenId, Date completionDate,
			ServiceContext serviceContext)
		throws SystemException {

		return kaleoInstanceTokenPersistence.findByC_PKITI_CD(
			serviceContext.getCompanyId(), parentKaleoInstanceTokenId,
			completionDate);
	}

	public List<KaleoInstanceToken> getKaleoInstanceTokens(
			long parentKaleoInstanceTokenId, ServiceContext serviceContext)
		throws SystemException {

		return kaleoInstanceTokenPersistence.findByC_PKITI(
			serviceContext.getCompanyId(), parentKaleoInstanceTokenId);
	}

	public int getKaleoInstanceTokensCount(
			long parentKaleoInstanceTokenId, Date completionDate,
			ServiceContext serviceContext)
		throws SystemException {

		return kaleoInstanceTokenPersistence.countByC_PKITI_CD(
			serviceContext.getCompanyId(), parentKaleoInstanceTokenId,
			completionDate);
	}

	public int getKaleoInstanceTokensCount(
			long parentKaleoInstanceTokenId, ServiceContext serviceContext)
		throws SystemException {

		return kaleoInstanceTokenPersistence.countByC_PKITI(
			serviceContext.getCompanyId(), parentKaleoInstanceTokenId);
	}

	public KaleoInstanceToken getRootKaleoInstanceToken(
			long kaleoInstanceId, Map<String, Serializable> workflowContext,
			ServiceContext serviceContext)
		throws PortalException, SystemException {

		KaleoInstance kaleoInstance = kaleoInstancePersistence.findByPrimaryKey(
			kaleoInstanceId);

		long rootKaleoInstanceTokenId =
			kaleoInstance.getRootKaleoInstanceTokenId();

		if (rootKaleoInstanceTokenId > 0) {
			return kaleoInstanceTokenPersistence.findByPrimaryKey(
				rootKaleoInstanceTokenId);
		}

		// Kaleo instance token

		User user = userPersistence.findByPrimaryKey(
			serviceContext.getUserId());
		Date now = new Date();

		rootKaleoInstanceTokenId = counterLocalService.increment();

		KaleoInstanceToken kaleoInstanceToken =
			kaleoInstanceTokenPersistence.create(rootKaleoInstanceTokenId);

		kaleoInstanceToken.setGroupId(serviceContext.getScopeGroupId());
		kaleoInstanceToken.setCompanyId(user.getCompanyId());
		kaleoInstanceToken.setUserId(user.getUserId());
		kaleoInstanceToken.setUserName(user.getFullName());
		kaleoInstanceToken.setCreateDate(now);
		kaleoInstanceToken.setModifiedDate(now);
		kaleoInstanceToken.setKaleoDefinitionId(
			kaleoInstance.getKaleoDefinitionId());
		kaleoInstanceToken.setKaleoInstanceId(
			kaleoInstance.getKaleoInstanceId());
		kaleoInstanceToken.setParentKaleoInstanceTokenId(
			KaleoInstanceTokenImpl.DEFAULT_PARENT_KALEO_INSTANCE_TOKEN_ID);

		kaleoInstanceTokenPersistence.update(kaleoInstanceToken, false);

		// Kaleo instance

		kaleoInstance.setRootKaleoInstanceTokenId(rootKaleoInstanceTokenId);

		kaleoInstancePersistence.update(kaleoInstance, false);

		return kaleoInstanceToken;
	}

	public KaleoInstanceToken updateKaleoInstanceToken(
			long kaleoInstanceTokenId, long currentKaleoNodeId)
		throws PortalException, SystemException {

		KaleoInstanceToken kaleoInstanceToken =
			kaleoInstanceTokenPersistence.findByPrimaryKey(
				kaleoInstanceTokenId);

		kaleoInstanceToken.setModifiedDate(new Date());

		setCurrentKaleoNode(kaleoInstanceToken, currentKaleoNodeId);

		kaleoInstanceTokenPersistence.update(kaleoInstanceToken, false);

		return kaleoInstanceToken;
	}

	protected void setCurrentKaleoNode(
			KaleoInstanceToken kaleoInstanceToken, long currentKaleoNodeId)
		throws PortalException, SystemException {

		kaleoInstanceToken.setCurrentKaleoNodeId(currentKaleoNodeId);

		KaleoNode currentKaleoNode = kaleoNodeLocalService.getKaleoNode(
			currentKaleoNodeId);

		kaleoInstanceToken.setCurrentKaleoNodeName(currentKaleoNode.getName());
	}

}