/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.keycloak.provider;

import javax.persistence.PersistenceException;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import lombok.AccessLevel;
import lombok.Setter;
import org.keycloak.component.ComponentModel;
import org.keycloak.credential.CredentialInput;
import org.keycloak.credential.CredentialInputValidator;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserCredentialModel;
import org.keycloak.models.UserModel;
import org.keycloak.models.credential.PasswordCredentialModel;
import org.keycloak.storage.UserStorageProvider;
import org.keycloak.storage.user.UserLookupProvider;
import org.openmrs.keycloak.data.UserAdapter;
import org.openmrs.keycloak.data.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Setter(AccessLevel.PACKAGE)
public class OpenmrsAuthenticator implements UserLookupProvider, CredentialInputValidator, UserStorageProvider {
	
	protected static final MessageDigest MESSAGE_DIGEST;
	
	static {
		try {
			MESSAGE_DIGEST = MessageDigest.getInstance("SHA-512");
		}
		catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException(e);
		}
	}
	
	protected KeycloakSession session;
	
	protected ComponentModel model;
	
	protected UserDao userDao;
	
	private static final Logger log = LoggerFactory.getLogger(OpenmrsAuthenticator.class);
	
	public OpenmrsAuthenticator(KeycloakSession session, ComponentModel model, UserDao userDao) {
		this.session = session;
		this.model = model;
		this.userDao = userDao;
	}
	
	@Override
	public UserModel getUserById(String id, RealmModel realmModel) {
		return new UserAdapter(session, realmModel, model, userDao.getOpenmrsUserByUserId(Integer.parseInt(id)));
	}
	
	@Override
	public UserModel getUserByUsername(String username, RealmModel realmModel) {
		return new UserAdapter(session, realmModel, model, userDao.getOpenmrsUserByUsername(username));
	}
	
	@Override
	public UserModel getUserByEmail(String email, RealmModel realmModel) {
		return new UserAdapter(session, realmModel, model, userDao.getOpenmrsUserByEmail(email));
	}
	
	@Override
	public boolean supportsCredentialType(String credentialType) {
		return credentialType.equals(PasswordCredentialModel.TYPE);
	}
	
	@Override
	public boolean isConfiguredFor(RealmModel realmModel, UserModel userModel, String credentialType) {
		return credentialType.equals(PasswordCredentialModel.TYPE);
	}
	
	@Override
	public boolean isValid(RealmModel realmModel, UserModel userModel, CredentialInput credentialInput) {
		if (!((credentialInput instanceof UserCredentialModel) && !supportsCredentialType(credentialInput.getType()))) {
			return false;
		}
		
		String[] passwordAndSalt;
		try {
			passwordAndSalt = userDao.getUserPasswordAndSaltOnRecord(userModel);
		}
		catch (PersistenceException e) {
			log.error("Caught exception while fetching password and salt from database", e);
			return false;
		}
		
		String passwordOnRecord = passwordAndSalt[0];
		String saltOnRecord = passwordAndSalt[1];
		String currentPassword = credentialInput.getChallengeResponse();
		
		if (passwordOnRecord == null || saltOnRecord == null || currentPassword == null) {
			return false;
		}
		
		String passwordToHash = currentPassword + saltOnRecord;
		byte[] input = passwordToHash.getBytes(StandardCharsets.UTF_8);
		return passwordOnRecord.equals(hexString(MESSAGE_DIGEST.digest(input)));
	}
	
	private String hexString(byte[] block) {
		StringBuilder buf = new StringBuilder();
		char[] hexChars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };
		int high;
		int low;
		for (byte aBlock : block) {
			high = ((aBlock & 0xf0) >> 4);
			low = (aBlock & 0x0f);
			buf.append(hexChars[high]);
			buf.append(hexChars[low]);
		}
		
		return buf.toString();
	}
	
	@Override
	public void close() {
		
	}
}
