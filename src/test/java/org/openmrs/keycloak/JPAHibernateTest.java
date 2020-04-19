/*
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.keycloak;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.tools.RunScript;
import org.hibernate.Session;
import org.hibernate.jdbc.Work;
import org.junit.AfterClass;
import org.junit.BeforeClass;

public class JPAHibernateTest {
	
	protected static EntityManagerFactory emf;
	
	protected static EntityManager em;
	
	@BeforeClass
	public static void init() throws FileNotFoundException, SQLException {
		emf = Persistence.createEntityManagerFactory("openmrs-persistence-test");
		em = emf.createEntityManager();
		
		Session session = em.unwrap(Session.class);
		session.doWork(new Work() {
			
			@Override
			public void execute(Connection connection) throws SQLException {
				try {
					File script = new File(getClass().getResource("/data.sql").getFile());
					RunScript.execute(connection, new FileReader(script));
				}
				catch (FileNotFoundException e) {
					throw new RuntimeException("could not initialize with script");
				}
			}
		});
	}
	
	@AfterClass
	public static void tearDown() {
		em.clear();
		em.close();
		emf.close();
	}
}
