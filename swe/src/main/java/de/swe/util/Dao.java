package de.swe.util;

import static de.swe.util.Constants.UID;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.inject.Named;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import javax.persistence.TypedQuery;

@Named
public class Dao implements Serializable {
	private static final long serialVersionUID = UID;
	
	@PersistenceContext
	protected transient EntityManager em;
	
	public <T> T find(Class<T> clazz, Object id) {
		final T result = em.find(clazz, id);
		
		return result;
	}
	
	public <T> List<T> find(Class<T> clazz, String namedQuery) {
		final List<T> result = em.createNamedQuery(namedQuery, clazz)
				.getResultList();
		
		return result;
	}
	
	public <T> List<T> find(Class<T> clazz, String namedQuery, Map<String, Object> parameters) {
		final TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
		final Set<Entry<String, Object>> paramSet = parameters.entrySet();
		
		for (Entry<String, Object> entry : paramSet) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		return query.getResultList();
	}
	
	public <T> List<T> find(Class<T> clazz, String namedQuery, int resultLimit) {
		final List<T> result = em.createNamedQuery(namedQuery, clazz)
				.setMaxResults(resultLimit)
				.getResultList();
		
		return result;
	}
	
	public <T> List<T> find(Class<T> clazz, String namedQuery, Map<String, Object> parameters, int resultLimit) {
		final TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz)
				.setMaxResults(resultLimit);
		final Set<Entry<String, Object>> paramSet = parameters.entrySet();
		
		for (Entry<String, Object> entry : paramSet) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		return query.getResultList();
	}
	
	public <T> T findSingle(Class<T> clazz, String namedQuery) {
		try {
			return em.createNamedQuery(namedQuery, clazz)
					.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	public <T> T findSingle(Class<T> clazz, String namedQuery, Map<String, Object> parameters) {
		final TypedQuery<T> query = em.createNamedQuery(namedQuery, clazz);
		final Set<Entry<String, Object>> paramSet = parameters.entrySet();
		
		for (Entry<String, Object> entry : paramSet) {
			query.setParameter(entry.getKey(), entry.getValue());
		}
		
		try {
			return query.getSingleResult();
		}
		catch (NoResultException e) {
			return null;
		}
	}

	//Vorsicht wegen SQL-Injection
	public List<?> findUsingSQL(String sql, String resultSetMapping) {
		return em.createNativeQuery(sql, resultSetMapping)
				.getResultList();
	}
	
	public <T> T create(T obj) {
		em.persist(obj);
		
		return obj;
	}
	
	public <T> T update(T obj) {
		return em.merge(obj);
	}
	
	public void delete(Object obj) {
		if (!em.contains(obj)) {
			final Object id = em.getEntityManagerFactory()
					.getPersistenceUnitUtil()
					.getIdentifier(obj);
			em.find(obj.getClass(), id);
		}
		
		em.remove(obj);
	}
	
	public void delete(Class<?> clazz, Object id) {
		final Object obj = em.find(clazz, id);
		em.remove(obj);
	}
	
	public static final class QueryParameter {
		private final Map<String, Object> params;
		
		private QueryParameter(String name, Object value) {
			params = new HashMap<>();
			params.put(name, value);
		}
		
		public static QueryParameter with(String name, Object value) {
			return new QueryParameter(name, value);
		}
		
		public QueryParameter and(String name, Object value) {
			params.put(name, value);
			
			return this;
		}
		
		public Map<String, Object> build() {
			return params;
		}
	}
}
