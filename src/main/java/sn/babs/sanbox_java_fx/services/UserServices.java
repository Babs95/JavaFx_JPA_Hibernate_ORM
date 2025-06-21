package sn.babs.sanbox_java_fx.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.NoResultException;
import jakarta.persistence.TypedQuery;
import sn.babs.sanbox_java_fx.entities.User;
import sn.babs.sanbox_java_fx.utils.JPAUtils;

public class UserServices {
    /**
     * Recherche un utilisateur par son email
     */
    public User findByEmail(String email) {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email", User.class);
            query.setParameter("email", email);

            try {
                return query.getSingleResult();
            } catch (NoResultException e) {
                return null; // Aucun utilisateur trouvé
            }
        }
    }

    /**
     * Authentifie un utilisateur avec email et mot de passe
     */
    public User authenticate(String email, String password) {
        if (email == null || password == null || email.trim().isEmpty() || password.trim().isEmpty()) {
            return null;
        }

        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<User> query = em.createQuery(
                    "SELECT u FROM User u WHERE u.email = :email AND u.password = :password", User.class);
            query.setParameter("email", email.trim());
            query.setParameter("password", password.trim());

            try {
                return query.getSingleResult();
            } catch (NoResultException e) {
                return null; // Aucun utilisateur trouvé avec ces credentials
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de l'authentification : " + e.getMessage());
            return null;
        }
    }

    /**
     * Sauvegarde un nouvel utilisateur
     */
    public User save(User user) {
        EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager();
        EntityTransaction transaction = em.getTransaction();

        try {
            transaction.begin();
            em.persist(user);
            transaction.commit();
            return user;
        } catch (Exception e) {
            if (transaction.isActive()) {
                transaction.rollback();
            }
            throw new RuntimeException("Erreur lors de la sauvegarde de l'utilisateur", e);
        } finally {
            em.close();
        }
    }

    /**
     * Vérifie si un utilisateur existe par son email
     */
    public boolean existsByEmail(String email) {
        return findByEmail(email) != null;
    }

    /**
     * Crée l'utilisateur admin par défaut s'il n'existe pas
     */
    public void createDefaultAdminIfNotExists() {
        String adminEmail = "admin-sunu-epicerie@yopmail.com";
        String adminPassword = "admin123"; // À changer selon vos besoins

        if (!existsByEmail(adminEmail)) {
            User adminUser = new User(adminEmail, adminPassword);
            save(adminUser);
            System.out.println("Utilisateur admin créé avec succès : " + adminEmail);
        } else {
            System.out.println("Utilisateur admin existe déjà : " + adminEmail);
        }
    }

}
