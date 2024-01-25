package io.pkb;

import javax.persistence.Persistence;
import java.util.HashSet;

public class Main {
    public static void main(String[] args) {
        // A SessionFactory is set up once for an application!
        var entityManagerFactory = Persistence.createEntityManagerFactory("org.hibernate.tutorial.jpa");
        {
            var em = entityManagerFactory.createEntityManager();
            em.getTransaction().begin();
            var p1 = new Parent();
            var c1 = new Child();
            c1.setName("Dave");
            c1.setParent(p1);
            var c2 = new Child();
            c2.setName("Jane");
            c2.setParent(p1);
            var children = new HashSet<Child>();
            children.add(c1);
            children.add(c2);
            p1.setChildren(children);
            em.persist(p1);
            em.getTransaction().commit();
            em.close();
        }

        {
            var em = entityManagerFactory.createEntityManager();
            var eg = em.createEntityGraph(Parent.class);
            eg.addAttributeNodes("children");
            
            var p1 = em.createQuery("select p from Parent p join p.children c on c.name = 'Dave'", Parent.class)
                    // Removing this line fixes the problem
                    .setHint("javax.persistence.loadgraph", eg)
                    .getSingleResult();
            // Logs 1, should be 2
            System.out.printf("p1.children.size() %d\n", p1.getChildren().size());
            
            // Subsequent cache-hit still has only 1 child, so cache is poisoned
            var id = p1.getId();
            var p1_p = em.find(Parent.class,id);
            System.out.printf("p1_p.children.size() %d\n", p1_p.getChildren().size());
            em.close();
        }
    }
}