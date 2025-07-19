package sn.babs.sanbox_java_fx.services;

import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import sn.babs.sanbox_java_fx.entities.Product;
import sn.babs.sanbox_java_fx.entities.Category;
import sn.babs.sanbox_java_fx.utils.JPAUtils;

import java.util.List;

public class ProductServices {

    public List<Product> getAll() {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Product> query = em.createQuery(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category ORDER BY p.libelle",
                    Product.class
            );
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des produits: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de récupérer les produits", e);
        }
    }

    public Product findById(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID ne peut pas être null");
        }

        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            return em.find(Product.class, id);
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche du produit: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de trouver le produit", e);
        }
    }

    public Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Le produit ne peut pas être null");
        }

        // Validation basique
        validateProduct(product);

        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            try {
                // S'assurer que la catégorie est attachée à l'EntityManager
                if (product.getCategory() != null && product.getCategory().getId() != null) {
                    Category managedCategory = em.find(Category.class, product.getCategory().getId());
                    if (managedCategory != null) {
                        product.setCategory(managedCategory);
                    }
                }

                if (product.getId() == null) {
                    em.persist(product);
                } else {
                    product = em.merge(product);
                }
                em.getTransaction().commit();
                return product;
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la sauvegarde du produit: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de sauvegarder le produit", e);
        }
    }

    public void delete(Long id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID ne peut pas être null");
        }

        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            em.getTransaction().begin();
            try {
                Product product = em.find(Product.class, id);
                if (product != null) {
                    em.remove(product);
                }
                em.getTransaction().commit();
            } catch (Exception e) {
                em.getTransaction().rollback();
                throw e;
            }
        } catch (Exception e) {
            System.err.println("Erreur lors de la suppression du produit: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de supprimer le produit", e);
        }
    }

    public List<Product> searchByTerm(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAll();
        }

        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Product> query = em.createQuery(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category c " +
                            "WHERE LOWER(p.code) LIKE LOWER(:term) " +
                            "OR LOWER(p.libelle) LIKE LOWER(:term) " +
                            "OR LOWER(c.libelle) LIKE LOWER(:term) " +
                            "ORDER BY p.libelle",
                    Product.class
            );
            query.setParameter("term", "%" + searchTerm.trim() + "%");
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la recherche des produits: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de rechercher les produits", e);
        }
    }

    public List<Product> findByCategory(Long categoryId) {
        if (categoryId == null) {
            throw new IllegalArgumentException("L'ID de catégorie ne peut pas être null");
        }

        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Product> query = em.createQuery(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.category.id = :categoryId ORDER BY p.libelle",
                    Product.class
            );
            query.setParameter("categoryId", categoryId);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des produits par catégorie: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de récupérer les produits par catégorie", e);
        }
    }

    public boolean existsByCode(String code) {
        if (code == null || code.trim().isEmpty()) {
            return false;
        }

        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(p) FROM Product p WHERE LOWER(p.code) = LOWER(:code)",
                    Long.class
            );
            query.setParameter("code", code.trim());
            return query.getSingleResult() > 0;
        } catch (Exception e) {
            System.err.println("Erreur lors de la vérification de l'existence du code: " + e.getMessage());
            return false;
        }
    }

    public List<Product> findLowStockProducts(int threshold) {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Product> query = em.createQuery(
                    "SELECT p FROM Product p LEFT JOIN FETCH p.category WHERE p.quantite <= :threshold ORDER BY p.quantite ASC",
                    Product.class
            );
            query.setParameter("threshold", threshold);
            return query.getResultList();
        } catch (Exception e) {
            System.err.println("Erreur lors de la récupération des produits en rupture: " + e.getMessage());
            e.printStackTrace();
            throw new RuntimeException("Impossible de récupérer les produits en rupture", e);
        }
    }

    public double getTotalInventoryValue() {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Double> query = em.createQuery(
                    "SELECT COALESCE(SUM(p.prix * p.quantite), 0.0) FROM Product p",
                    Double.class
            );
            return query.getSingleResult();
        } catch (Exception e) {
            System.err.println("Erreur lors du calcul de la valeur totale du stock: " + e.getMessage());
            e.printStackTrace();
            return 0.0;
        }
    }

    public long getTotalProductsCount() {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery("SELECT COUNT(p) FROM Product p", Long.class);
            return query.getSingleResult();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des produits: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    public long getLowStockCount(int threshold) {
        try (EntityManager em = JPAUtils.getEntityManagerFactory().createEntityManager()) {
            TypedQuery<Long> query = em.createQuery(
                    "SELECT COUNT(p) FROM Product p WHERE p.quantite <= :threshold",
                    Long.class
            );
            query.setParameter("threshold", threshold);
            return query.getSingleResult();
        } catch (Exception e) {
            System.err.println("Erreur lors du comptage des produits en rupture: " + e.getMessage());
            e.printStackTrace();
            return 0;
        }
    }

    private void validateProduct(Product product) {
        // Validation du libellé
        if (product.getLibelle() == null || product.getLibelle().trim().isEmpty()) {
            throw new IllegalArgumentException("Le libellé du produit est obligatoire");
        }

        // Validation du prix
        if (product.getPrix() < 0) {
            throw new IllegalArgumentException("Le prix ne peut pas être négatif");
        }

        // Validation de la quantité
        if (product.getQuantite() < 0) {
            throw new IllegalArgumentException("La quantité ne peut pas être négative");
        }

        // Validation de la catégorie
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("La catégorie est obligatoire");
        }

        // Nettoyer les données
        product.setLibelle(product.getLibelle().trim());
        if (product.getCode() != null) {
            product.setCode(product.getCode().trim().toUpperCase());
        }
        if (product.getDescription() != null) {
            product.setDescription(product.getDescription().trim());
            if (product.getDescription().isEmpty()) {
                product.setDescription(null);
            }
        }
    }
}