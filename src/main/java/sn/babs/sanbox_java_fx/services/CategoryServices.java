package sn.babs.sanbox_java_fx.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import sn.babs.sanbox_java_fx.entities.Category;
import sn.babs.sanbox_java_fx.utils.JPAUtils;

import java.util.List;

public class CategoryServices {

    public List<Category> getAll() {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Category> query = em.createQuery("SELECT c FROM Category c", Category.class);
            return query.getResultList();
        }
    }

    public Category findById(Long id) {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            return em.find(Category.class, id);
        }
    }

    public Category save(Category category) {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            try {
                if (category.getId() == null) {
                    em.persist(category);
                } else {
                    category = em.merge(category);
                }
                em.getTransaction().commit();
                return category;
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }

    public void delete(Long id) {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            try {
                Category category = em.find(Category.class, id);
                if (category != null) {
                    em.remove(category);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        }
    }
}