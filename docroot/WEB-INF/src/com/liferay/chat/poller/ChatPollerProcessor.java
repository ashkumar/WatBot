/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
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

package com.liferay.chat.poller;

import com.liferay.chat.model.Entry;
import com.liferay.chat.model.Status;
import com.liferay.chat.service.EntryLocalServiceUtil;
import com.liferay.chat.service.StatusLocalServiceUtil;
import com.liferay.chat.util.BuddyFinderUtil;
import com.liferay.chat.util.ChatConstants;
import com.liferay.chat.util.PortletPropsValues;
import com.liferay.portal.NoSuchUserException;
import com.liferay.portal.kernel.json.JSONArray;
import com.liferay.portal.kernel.json.JSONFactoryUtil;
import com.liferay.portal.kernel.json.JSONObject;
import com.liferay.portal.kernel.poller.BasePollerProcessor;
import com.liferay.portal.kernel.poller.PollerRequest;
import com.liferay.portal.kernel.poller.PollerResponse;
import com.liferay.portal.kernel.util.HtmlUtil;
import com.liferay.portal.kernel.util.ListUtil;
import com.liferay.portal.kernel.util.StringPool;
import com.liferay.portal.kernel.util.Time;
import com.liferay.portal.model.ContactConstants;
import com.liferay.portal.model.User;
import com.liferay.portal.service.UserLocalServiceUtil;

import java.util.Collections;
import java.util.List;

/**
 * @author Brian Wing Shun Chan
 */
public class ChatPollerProcessor extends BasePollerProcessor {

	protected void addEntry(PollerRequest pollerRequest) throws Exception {
		long toUserId = getLong(pollerRequest, "toUserId");
		String content = getString(pollerRequest, "content");

		if (toUserId > 0) {
			EntryLocalServiceUtil.addEntry(
				pollerRequest.getTimestamp(), pollerRequest.getUserId(),
				toUserId, content);
		}
	}

	@Override
	protected void doReceive(
			PollerRequest pollerRequest, PollerResponse pollerResponse)
		throws Exception {

		getBuddies(pollerRequest, pollerResponse);
		getEntries(pollerRequest, pollerResponse);
	}

	@Override
	protected void doSend(PollerRequest pollerRequest) throws Exception {
		addEntry(pollerRequest);
		updateStatus(pollerRequest);
	}

	protected void getBuddies(
			PollerRequest pollerRequest, PollerResponse pollerResponse)
		throws Exception {

		List<Object[]> buddies = BuddyFinderUtil.getBuddies(
			pollerRequest.getCompanyId(), pollerRequest.getUserId());

		JSONArray buddiesJSONArray = JSONFactoryUtil.createJSONArray();

		for (Object[] buddy : buddies) {
			boolean awake = (Boolean)buddy[0];
			String firstName = (String)buddy[1];
			long groupId = (Long)buddy[2];
			String lastName = (String)buddy[3];
			boolean male = (Boolean)buddy[4];
			String middleName = (String)buddy[5];
			long portraitId = (Long)buddy[6];
			String screenName = (String)buddy[7];
			long userId = (Long)buddy[8];
			String userUuid = (String)buddy[9];

			JSONObject curUserJSONObject = JSONFactoryUtil.createJSONObject();

			Status buddyStatus = StatusLocalServiceUtil.getUserStatus(userId);

			awake = buddyStatus.getAwake();

			curUserJSONObject.put("awake", awake);

			String fullName = ContactConstants.getFullName(
				firstName, middleName, lastName);

			curUserJSONObject.put("fullName", fullName);
			curUserJSONObject.put("groupId", groupId);
			curUserJSONObject.put("portraitId", portraitId);

			String portraitURL = com.liferay.portal.model.UserConstants.getPortraitURL(
				StringPool.BLANK, male, portraitId, userUuid);

			curUserJSONObject.put("portraitURL", portraitURL);

			curUserJSONObject.put("screenName", screenName);

			String statusMessage = buddyStatus.getMessage();

			curUserJSONObject.put("statusMessage", statusMessage);
			curUserJSONObject.put("userId", userId);

			buddiesJSONArray.put(curUserJSONObject);
		}

		pollerResponse.setParameter("buddies", buddiesJSONArray);
	}

	protected void getEntries(
			PollerRequest pollerRequest, PollerResponse pollerResponse)
		throws Exception {

		Status status = StatusLocalServiceUtil.getUserStatus(
			pollerRequest.getUserId());

		long createDate = 0;

		if (pollerRequest.isInitialRequest()) {
			createDate = status.getModifiedDate() - Time.DAY;
		}

		List<Entry> entries = EntryLocalServiceUtil.getNewEntries(
			pollerRequest.getUserId(), createDate, 0,
			PortletPropsValues.BUDDY_LIST_MAX_BUDDIES);

		entries = ListUtil.copy(entries);

		Collections.reverse(entries);

		JSONArray entriesJSONArray = JSONFactoryUtil.createJSONArray();

		for (Entry entry : entries) {
			JSONObject entryJSONObject = JSONFactoryUtil.createJSONObject();

			entryJSONObject.put("entryId", entry.getEntryId());
			entryJSONObject.put("createDate", entry.getCreateDate());
			entryJSONObject.put("fromUserId", entry.getFromUserId());

			if (entry.getFromUserId() != pollerRequest.getUserId()) {
				try {
					User fromUser = UserLocalServiceUtil.getUserById(
						entry.getFromUserId());

					entryJSONObject.put("fromFullName", fromUser.getFullName());
					entryJSONObject.put(
						"fromPortraitId", fromUser.getPortraitId());
				}
				catch (NoSuchUserException nsue) {
					continue;
				}
			}

			entryJSONObject.put("toUserId", entry.getToUserId());
			entryJSONObject.put("content", HtmlUtil.escape(entry.getContent()));
			entryJSONObject.put("flag", entry.getFlag());

			entriesJSONArray.put(entryJSONObject);
		}

		pollerResponse.setParameter("entries", entriesJSONArray);

		if (!entries.isEmpty()) {
			pollerResponse.setParameter(
				PollerResponse.POLLER_HINT_HIGH_CONNECTIVITY,
				Boolean.TRUE.toString());
		}

		boolean updatePresence = getBoolean(pollerRequest, "updatePresence");

		if (updatePresence) {
		}
		else if (!entries.isEmpty()) {
			updatePresence = true;
		}
		else {
			long onlineTimestamp =
				status.getModifiedDate() + ChatConstants.ONLINE_DELTA -
					ChatConstants.MAX_POLL_LATENCY;

			if (onlineTimestamp < pollerRequest.getTimestamp()) {
				updatePresence = true;
			}
		}

		if (updatePresence) {
			StatusLocalServiceUtil.updateStatus(
				pollerRequest.getUserId(), pollerRequest.getTimestamp());
		}
	}

	protected void updateStatus(PollerRequest pollerRequest) throws Exception {
		long timestamp = -1;
		int online = getInteger(pollerRequest, "online");
		int awake = getInteger(pollerRequest, "awake");
		String activePanelIds = getString(pollerRequest, "activePanelIds");
		String statusMessage = getString(pollerRequest, "statusMessage");
		int playSound = getInteger(pollerRequest, "playSound");

		if ((online != -1) || (awake != -1) || (activePanelIds != null) ||
			(statusMessage != null) || (playSound != -1)) {

			StatusLocalServiceUtil.updateStatus(
				pollerRequest.getUserId(), timestamp, online, awake,
				activePanelIds, statusMessage, playSound);
		}
	}

}