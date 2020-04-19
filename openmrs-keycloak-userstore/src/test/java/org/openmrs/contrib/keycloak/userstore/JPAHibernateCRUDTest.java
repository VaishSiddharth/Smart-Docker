/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.contrib.keycloak.userstore;

import static org.junit.Assert.*;

import javax.persistence.Query;

import org.junit.Test;
import org.openmrs.contrib.keycloak.userstore.models.OpenmrsUserModel;

public class JPAHibernateCRUDTest extends JPAHibernateTest {
	
	@Test
	public void getUserByUsername() {
		OpenmrsUserModel query = em
		        .createQuery("select u from OpenmrsUserModel u where u.username = 'admin'", OpenmrsUserModel.class)
		        .getSingleResult();
		assertEquals("admin", query.getUsername());
		assertTrue("Error, random is too low", query.getUserId() == 152);
	}
	
	@Test
	public void getUserById() {
		OpenmrsUserModel query = em
		        .createQuery("select u from OpenmrsUserModel u where u.userId = '186'", OpenmrsUserModel.class)
		        .getSingleResult();
		assertEquals("Sid", query.getUsername());
	}
	
	@Test
	public void getPasswordAndSalt() {
		Query query = em.createNativeQuery("select password, salt from users u where u.username = 'SidVaish'");
		Object[] result = (Object[]) query.getSingleResult();
		assertEquals("123", result[1].toString());
	}
	
}
