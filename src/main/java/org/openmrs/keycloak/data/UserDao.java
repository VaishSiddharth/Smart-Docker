package org.openmrs.keycloak.data;

import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.openmrs.keycloak.models.OpenmrsUserModel;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import java.util.Arrays;
import java.util.stream.Collectors;

public class UserDao {

    private final EntityManager em;

    public UserDao(EntityManager em) {
        this.em = em;
    }

    public OpenmrsUserModel getOpenmrsUserByUsername(String username) {
        TypedQuery<OpenmrsUserModel> query = em.createQuery("select u from OpenmrsUserModel u where u.username = :username", OpenmrsUserModel.class);
        query.setParameter("username", username);
        return query.getSingleResult();
    }

    public OpenmrsUserModel getOpenmrsUserByUserId(Integer userId) {
        TypedQuery<OpenmrsUserModel> query = em.createQuery("select u from OpenmrsUserModel u where u.userId = :userId", OpenmrsUserModel.class);
        query.setParameter("userId", userId);
        return query.getSingleResult();
    }

    public OpenmrsUserModel getOpenmrsUserByEmail(String email) throws NotImplementedException {
        TypedQuery<OpenmrsUserModel> query = em.createQuery("select u from OpenmrsUserModel u where u.email = :email", OpenmrsUserModel.class);
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
