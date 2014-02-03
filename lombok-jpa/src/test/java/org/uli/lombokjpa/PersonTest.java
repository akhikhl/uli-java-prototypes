/**
 * 
 */
package org.uli.lombokjpa;

import static org.junit.Assert.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;


/**
 * @author uli
 *
 */
public class PersonTest {
    private static final String PERSISTENCE_UNIT_NAME = "jpa";
    private static EntityManagerFactory entityManagerFactory;

    private static Map<Integer, LombokPerson> lombokPersons = new HashMap<Integer, LombokPerson>();
    private static Map<Integer, TraditionalPerson> traditionalPersons = new HashMap<Integer, TraditionalPerson>();

    @BeforeClass
    static public void initEm() {
        Map<String,String> persistenceProperties = new HashMap<String,String>();
        persistenceProperties.put("javax.persistence.jdbc.driver", "org.h2.Driver");
        persistenceProperties.put("javax.persistence.jdbc.url","jdbc:h2:h2Db"); 
        entityManagerFactory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME, persistenceProperties);
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query delete = entityManager.createQuery("delete from TraditionalPerson p");
	entityManager.getTransaction().begin();
        delete.executeUpdate();
        entityManager.getTransaction().commit();
        entityManager.close();
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select p from LombokPerson p");

        if (query.getResultList().isEmpty()) {
            // Create new lombok persons
            entityManager.getTransaction().begin();
            for (int i=0; i<40; i++) {
                LombokPerson person = new LombokPerson();
                person.setFirstName("firstName-"+i);
                person.setLastName("lastName-"+i);
                entityManager.persist(person);
                lombokPersons.put(person.getPersonId(), person);
            }
            entityManager.getTransaction().commit();
        }
        entityManager.close();
    }

    @Test
    public void testNotEmpty() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        Query query = entityManager.createQuery("select p from LombokPerson p");
        @SuppressWarnings("unchecked")
        List<Object> resultList = query.getResultList();
        assertFalse(resultList.isEmpty());
        assertEquals(lombokPersons.size(), resultList.size());
        entityManager.close();
    }
    
    @Test
    public void testFind() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        for (LombokPerson p : lombokPersons.values()) {
            Integer personId = p.getPersonId();
            TraditionalPerson dbPerson = entityManager.find(TraditionalPerson.class, personId);
            assertEquals("Person-"+personId+", personId:",  personId, dbPerson.getPersonId());
            assertEquals("Person-"+personId+", firstName:", p.getFirstName(), dbPerson.getFirstName());
            assertEquals("Person-"+personId+", lastName:",  p.getLastName(),  dbPerson.getLastName());
        }
        entityManager.close();
    }

    @Test
    public void testGetReference() {
        EntityManager entityManager = entityManagerFactory.createEntityManager();
        for (LombokPerson p : lombokPersons.values()) {
            Integer personId = p.getPersonId();
            TraditionalPerson dbPerson = entityManager.getReference(TraditionalPerson.class, personId);
            assertEquals("Person-"+personId+", personId:",  personId, dbPerson.getPersonId());
            assertEquals("Person-"+personId+", firstName:", p.getFirstName(), dbPerson.getFirstName());
            assertEquals("Person-"+personId+", lastName:",  p.getLastName(),  dbPerson.getLastName());
        }
        entityManager.close();
    }
}
