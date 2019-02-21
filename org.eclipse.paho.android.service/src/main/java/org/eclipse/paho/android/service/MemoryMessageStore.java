/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 *
 *   Contributors:
 *     James Sutton - Removing SQL Injection vunerability (bug 467378)
 */
package org.eclipse.paho.android.service;

import java.util.Iterator;
import java.util.HashMap;
import java.util.HashSet;

import org.eclipse.paho.client.mqttv3.MqttMessage;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

/**
 * Implementation of the {@link MessageStore} interface, using a SQLite database
 * 
 */
class MemoryMessageStore implements MessageStore {

	// TAG used for indentify trace data etc.
	private static final String TAG = "MemoryMessageStore";

	private MqttTraceHandler traceHandler = null;

	private HashMap<String, MemStoredData> memDb = null;

	public MemoryMessageStore(MqttService service) {
		this.traceHandler = service;

		// Open message database
		memDb = new HashMap<String, MemStoredData>();

		traceHandler.traceDebug(TAG, "MemoryMessageStore<init> complete");
	}

	@Override
	public String storeArrived(String clientHandle, String topic,
			MqttMessage message) {

		String messageId = java.util.UUID.randomUUID().toString();
		MemStoredData data = new MemStoredData(messageId, topic, message);
		memDb.put(messageId, data);
		return messageId;
	}

	@Override
	public boolean discardArrived(String clientHandle, String id) {
		memDb.remove(id);
		return true;
	}

	/**
	 * Get an iterator over all messages stored (optionally for a specific client)
	 * 
	 * @param clientHandle
	 *            identifier for the client.<br>
	 *            If null, all messages are retrieved
	 * @return iterator of all the arrived MQTT messages
	 */
	@Override
	public Iterator<StoredMessage> getAllArrivedMessages(final String clientHandle) {
		HashSet<StoredMessage> messages = new HashSet();
		for (String k: memDb.keySet()) {
			messages.add(memDb.get(k));
		}
		return messages.iterator();
  }

	@Override
	public void clearArrivedMessages(String clientHandle) {
		memDb.clear();
	}

	private class MemStoredData implements StoredMessage {
		private String messageId;
		private String clientHandle;
		private String topic;
		private MqttMessage message;

		MemStoredData(String messageId, String topic, MqttMessage message) {
			this.messageId = messageId;
			this.topic = topic;
			this.message = message;
		}

		@Override
		public String getMessageId() {
			return messageId;
		}

		@Override
		public String getClientHandle() {
			return clientHandle;
		}

		@Override
		public String getTopic() {
			return topic;
		}

		@Override
		public MqttMessage getMessage() {
			return message;
		}
	}

	@Override
	public void close() {
        memDb = null;
	}

}