/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.keycloak.data;

import javax.persistence.EntityManager;
import javax.persistence.Query;
import javax.persistence.TypedQuery;

import java.util.Arrays;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.openmrs.keycloak.models.OpenmrsUserModel;

public class UserDao {
	
	private final EntityManager em;
	
	public UserDao(EntityManager em) {
		this.em = em;
	}
	
	public OpenmrsUserModel getOpenmrsUserByUsername(String username) {
		TypedQuery<OpenmrsUserModel> query = em.createQuery("select u from OpenmrsUserModel u where u.username = :username",
		    OpenmrsUserModel.class);
		query.setParameter("username", username);
		return query.getSingleResult();
	}
	
	public OpenmrsUserModel getOpenmrsUserByUserId(Integer userId) {
		TypedQuery<OpenmrsUserModel> query = em.createQuery("select u from OpenmrsUserModel u where u.userId = :userId",
		    OpenmrsUserModel.class);
		query.setParameter("userId", userId);
		return query.getSingleResult();
	}
	
	public OpenmrsUserModel getOpenmrsUserByEmail(String email) throws NotImplementedException {
		TypedQuery<OpenmrsUserModel> query = em.createQuery("select u from OpenmrsUserModel u where u.email = :email",
		    OpenmrsUserModel.class);
		query.setParameter("userId", email);
		return query.getSingleResult();
	}
	
	public String[] getUserPasswordAndSaltOnRecord(org.keycloak.models.UserModel userModel) {
		String username = userModel.getUsername();
		if (StringUtils.isBlank(username)) {
			throw new IllegalArgumentException("Username cannot be blank");
		}
		
		Query query = em.createNativeQuery("select password, salt from users u where u.username = :username");
		query.setParameter("username", userModel.getUsername());
		return Arrays.stream((Object[]) query.getSingleResult()).map(Object::toString).toArray(String[]::new);
	}
}
