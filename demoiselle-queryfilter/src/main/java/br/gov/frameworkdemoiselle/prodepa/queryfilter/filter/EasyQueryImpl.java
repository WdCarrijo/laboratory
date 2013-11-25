package br.gov.frameworkdemoiselle.prodepa.queryfilter.filter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.BetweenCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.EqualsCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.GreaterOrEqualsCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.GreaterThanCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.InCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.IsEmptyCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.IsNotEmptyCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.IsNotNullCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.IsNullCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.LikeCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.LowerOrEqualsCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.LowerThanCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.NotEqualsCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.NotInCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.NotLikeCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.conditions.commons.AbstractCondition;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.exception.EasyQueryFilterException;
import br.gov.frameworkdemoiselle.prodepa.queryfilter.util.StringUtil;

/**
 * 
 * TODO Descrever essa classe
 * 
 * TODO Refatorar essa classe para que ela fique mais simples
 * 
 * @author thiago
 *
 * @param <T>
 */
@SuppressWarnings("unchecked")
public class EasyQueryImpl<T> implements EasyQuery<T> {

	private Class<T> beanClass;

	private EntityManager em;

	private Query query;
	
	private StringBuilder bQuery;

	private List<AbstractCondition> andConditions;

	private List<AbstractCondition> orConditions;

	private List<String> ascOrderByConditions;

	private List<String> descOrderByConditions;

	private Map<String, Object> params;

	private String rootAlias;

	private List<String> joins;
	private Map<String, String> joinsPaths;

	private Class<T> returnClass;

	private String[] resultColumns;

	// Controles
	private Integer conditionsCount;
	
	private Boolean countingTrue;
	private Boolean distinctTrue;
	private String distinctON = null; //TODO Procurar forma de se fazer isso.

	private static final Boolean toLowerCaseDefaultValeu = false;

	public EasyQueryImpl(Class<T> beanClass, EntityManager em) {
		super();

		this.beanClass = beanClass;
		this.em = em;

		this.rootAlias = beanClass.getSimpleName().substring(0, 1).toLowerCase();

		this.joins = new ArrayList<String>();
		this.joinsPaths = new HashMap<String, String>();
		
		this.andConditions = new ArrayList<AbstractCondition>();
		this.orConditions = new ArrayList<AbstractCondition>();

		this.ascOrderByConditions = new ArrayList<String>();
		this.descOrderByConditions = new ArrayList<String>();

		this.params = new HashMap<String, Object>();
		
		this.countingTrue = false;
		this.distinctTrue = false;
		this.distinctON = null;
		
		this.conditionsCount = 0;

	}
	
	private void initializeControls() {
		this.countingTrue = false;
		//this.distinctTrue = false;
		//this.distinctON = null;
		
		this.conditionsCount = 0;
	}

	public void setResultClass(Class<?> resultClass) {
		this.returnClass = returnClass;
	}

	public void setResultColumns(String[] columns) {
		// TODO Deve haver um modo melhor para converter String[] para List<String>
		initializeControls();
		this.resultColumns = columns;
	}

	public List<T> getResultList() {
		// TODO Caso não haja resultSetColumns ou ???, o resultado será simples
		initializeControls();
		buildQuery();
		return (List<T>) query.getResultList();
	}

	public T getSingleResult() {
		// TODO Caso não haja resultSetColumns ou ???, o resultado será simples
		buildQuery();
		return (T) query.getSingleResult();
	}

	public Long count() {
		this.countingTrue = true;
		buildQuery();
		this.countingTrue = false;
		return (Long) query.getSingleResult();
	}

	public EasyQuery<T> addAnnotatedFilter(Object filter) throws EasyQueryFilterException {
	  
	  new EasyQueryParameterBuild<T>(this, beanClass).addFilters(filter);
		
	  return this;
	}

	/**
	 * AND Clausules
	 */

	public EasyQuery<T> andEquals(String attributeName, Object value) {
		return andEquals(toLowerCaseDefaultValeu, attributeName, value);
	}

	public EasyQuery<T> andEquals(boolean toLowerCase, String attributeName, Object value) {
		andConditions.add(new EqualsCondition(getAttributePath(attributeName), value, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andNotEquals(String attributeName, Object value) {
		return andNotEquals(toLowerCaseDefaultValeu, attributeName, value);
	}

	public EasyQuery<T> andNotEquals(boolean toLowerCase, String attributeName, Object value) {
		andConditions.add(new NotEqualsCondition(getAttributePath(attributeName), value, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andGreaterThan(String attributeName, Object value) {
		return andGreaterThan(toLowerCaseDefaultValeu, attributeName, value);
	}

	public EasyQuery<T> andGreaterThan(boolean toLowerCase, String attributeName, Object value) {
		andConditions.add(new GreaterThanCondition(getAttributePath(attributeName), value, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andGreaterOrEqualTo(String attributeName, Object value) {
		return andGreaterOrEqualTo(toLowerCaseDefaultValeu, attributeName, value);
	}

	public EasyQuery<T> andGreaterOrEqualTo(boolean toLowerCase, String attributeName, Object value) {
		andConditions.add(new GreaterOrEqualsCondition(getAttributePath(attributeName), value, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andLessThan(String attributeName, Object value) {
		return andLessThan(toLowerCaseDefaultValeu, attributeName, value);
	}

	public EasyQuery<T> andLessThan(boolean toLowerCase, String attributeName, Object value) {
		andConditions.add(new LowerThanCondition(getAttributePath(attributeName), value, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andLessOrEqualTo(String attributeName, Object value) {
		return andLessOrEqualTo(toLowerCaseDefaultValeu, attributeName, value);
	}

	public EasyQuery<T> andLessOrEqualTo(boolean toLowerCase, String attributeName, Object value) {
		andConditions.add(new LowerOrEqualsCondition(getAttributePath(attributeName), value, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andBetween(String attributeName, Object valueA, Object valueB) {
		return andBetween(toLowerCaseDefaultValeu, attributeName, valueA, valueB);
	}

	public EasyQuery<T> andBetween(boolean toLowerCase, String attributeName, Object valueA, Object valueB) {
		andConditions.add(new BetweenCondition(getAttributePath(attributeName), valueA, valueB, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andIsNull(String attributeName) {
		andConditions.add(new IsNullCondition(getAttributePath(attributeName), this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andIsNotNull(String attributeName) {
		andConditions.add(new IsNotNullCondition(getAttributePath(attributeName), this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andCollectionIsEmpty(String collectionName) {
		andConditions.add(new IsEmptyCondition(getAttributePath(collectionName), this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andCollectionIsNotEmpty(String collectionName) {
		andConditions.add(new IsNotEmptyCondition(getAttributePath(collectionName), this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andStringLike(String attributeName, String value) {
		return andStringLike(toLowerCaseDefaultValeu, attributeName, value);
	}

	public EasyQuery<T> andStringLike(boolean toLowerCase, String attributeName, String value) {
		andConditions.add(new LikeCondition(getAttributePath(attributeName), value, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andStringNotLike(String attributeName, String value) {
		return andStringNotLike(toLowerCaseDefaultValeu, attributeName, value);
	}

	public EasyQuery<T> andStringNotLike(boolean toLowerCase, String attributeName, String value) {
		andConditions.add(new NotLikeCondition(getAttributePath(attributeName), value, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andStringIn(String attributeName, List<String> values) {
		return andStringIn(toLowerCaseDefaultValeu, attributeName, values);
	}

	public EasyQuery<T> andStringIn(boolean toLowerCase, String attributeName, List<String> values) {
		// TODO Acho que falta algo aki
		andConditions.add(new InCondition(getAttributePath(attributeName), values, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> andStringNotIn(String attributeName, List<String> values) {
		return andStringNotIn(toLowerCaseDefaultValeu, attributeName, values);
	}

	public EasyQuery<T> andStringNotIn(boolean toLowerCase, String attributeName, List<String> values) {
		andConditions.add(new NotInCondition(getAttributePath(attributeName), values, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}

	public EasyQuery<T> addAndSeparatedByOr(int index, String attributeName, Object value) {
		// TODO Auto-generated method stub
		return this;
	}

	public EasyQuery<T> addAndSeparatedByOr(boolean toLowerCase, int index, String attributeName, Object value) {
		// TODO Auto-generated method stub
		return this;
	}

	/**
	 * OR Clausules
	 */

	public EasyQuery<T> orEquals(String attributeName, Object... values) {
		orEquals(toLowerCaseDefaultValeu, attributeName, values);
		return this;
	}

	public EasyQuery<T> orEquals(boolean toLowerCase, String attributeName, Object... value) {
		for (Object object : value) {
			orConditions.add(new EqualsCondition(getAttributePath(attributeName), object, toLowerCase, this.conditionsCount));
			this.conditionsCount ++;
		}
		return this;
	}

	public EasyQuery<T> orEquals(int index, String attributeName, Object... values) {
		// TODO Auto-generated method stub
		//TODO copiar logica do orEquals
		return this;
	}

	public EasyQuery<T> orEquals(boolean toLowerCase, int index, String attributeName, Object... values) {
		// TODO Auto-generated method stub
		// orConditions.put(OrConditionType.EQUAL, new Condition(attributeName, value));
		//TODO copiar logica do orEquals
		return this;
	}

	public EasyQuery<T> orNotEquals(String attributeName, Object... values) {
		orNotEquals(toLowerCaseDefaultValeu, attributeName, values);
		//TODO copiar logica do orEquals
		return this;
	}

	public EasyQuery<T> orNotEquals(boolean toLowerCase, String attributeName, Object... values) {
		orConditions.add(new NotEqualsCondition(getAttributePath(attributeName), values, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}
	
	public EasyQuery<T> orIn(String attributeName, List<?> values) {
		orIn(toLowerCaseDefaultValeu, attributeName, values);
		return this;
	}
	
	public EasyQuery<T> orIn(boolean toLowerCase, String attributeName, List<?> values) {
		orConditions.add(new InCondition(getAttributePath(attributeName), values, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}
	
	public EasyQuery<T> orNotIn(String attributeName, List<?> values) {
		orNotIn(toLowerCaseDefaultValeu, attributeName, values);
		return this;
	}
	  
	public EasyQuery<T> orNotIn(boolean toLowerCase, String attributeName, List<?> values) {
		orConditions.add(new InCondition(getAttributePath(attributeName), values, toLowerCase, this.conditionsCount));
		this.conditionsCount ++;
		return this;
	}
	  

	/**
	 * JOINS Clausules
	 */

	public EasyQuery<T> innerJoin(String joinName) {
		//TODO Esta bem complicado...........
		
		this.joins.add(" INNER JOIN " + this.rootAlias + "." + joinName + " " + StringUtil.castToAliasName(joinName.toString()));
		
		this.joinsPaths.put(joinName.toString(), StringUtil.castToAliasName(joinName.toString()));
		
		/*
		String[] paths = joinName.split("\\.");
		
		for (int i = 0; i < paths.length; i++) {
			
			StringBuilder joinB = new StringBuilder();
			for (int j = 0; j <= i; j++) {
				joinB.append(paths[j]);
				joinB.append(".");
			}
			
			joinB.delete(joinB.length() -1 , joinB.length());
			
			String join = null;
			if(this.joinsPaths.containsKey(joinB.toString())) {
				join = this.joinsPaths.get(joinB).toString();
			} else {
				join = joinB.toString();
			}
			
			this.joins.add(" INNER JOIN " + this.rootAlias + "." + join + " " + StringUtil.castToAliasName(join.toString()));
			
			this.joinsPaths.put(join.toString(), StringUtil.castToAliasName(join.toString()));
		}
		*/
		
		return this;
	}

	public EasyQuery<T> leftJoin(String joinName) {
		
		//TODO Compartilhar logica com INNER JOIN
		
		this.joins.add(" LEFT JOIN " + this.rootAlias + "." + joinName + " " + StringUtil.castToAliasName(joinName.toString()));
		
		this.joinsPaths.put(joinName.toString(), StringUtil.castToAliasName(joinName.toString()));
		
		return this;
	}

	public EasyQuery<T> innerJoinFetch(String joinName) {
		
		//TODO Compartilhar logica com INNER JOIN
		
		this.joins.add(" INNER JOIN FETCH " + this.rootAlias + "." + joinName + " " + StringUtil.castToAliasName(joinName.toString()));
		
		this.joinsPaths.put(joinName.toString(), StringUtil.castToAliasName(joinName.toString()));
		return this;
	}

	public EasyQuery<T> leftJoinFetch(String joinName) {
		
		//TODO Compartilhar logica com INNER JOIN
		this.joins.add(" LEFT JOIN FETCH " + this.rootAlias + "." + joinName + " " + StringUtil.castToAliasName(joinName.toString()));
		
		this.joinsPaths.put(joinName.toString(), StringUtil.castToAliasName(joinName.toString()));
		return this;
	}


	/**
	 * ORDER Clausules
	 */

	public EasyQuery<T> orderByAsc(String attributeName) {
		ascOrderByConditions.add(attributeName);
		return this;
	}

	public EasyQuery<T> orderByDesc(String attributeName) {
		descOrderByConditions.add(attributeName);
		return this;
	}

	/**
	 * OTHER Clausules
	 */

	public EasyQuery<T> setDistinctTrue() {
		this.distinctTrue = true;
		return this;
	}

	public EasyQuery<T> setFirstResult(int firstResult) {
		// TODO Auto-generated method stub
		return this;
	}

	public EasyQuery<T> setMaxResults(int maxResults) {
		// TODO Auto-generated method stub
		return this;
	}

	/**
	 * Build Query
	 */

	private void buildQuery() {

		query = em.createQuery(fillStringQuery());
		putParams();
		// TODO Alguma coisa mais aki ?
	}

	private String fillStringQuery() {

		this.bQuery = new StringBuilder();

		buildSelect();
		
		buildFrom();
		
		buildJoins();
		
		buidAnds();

		buildOrs();

		buildOrderes();

		System.out.println(this.bQuery.toString());
		return this.bQuery.toString();
	}

	private void buildSelect() {
		
		if (this.returnClass == null && this.resultColumns == null) {

			if(this.countingTrue) {
				this.bQuery.append("SELECT COUNT( " + this.rootAlias + ") ");
			} else 
			if(this.distinctTrue) {
				this.bQuery.append("SELECT DISTINCT " + this.rootAlias);
			} else {
				this.bQuery.append("SELECT " + this.rootAlias);
			}

		} else {

			if (this.returnClass != null) {

				// Somente a classe especial foi definida >> delegar para a JPA createQuery("", Class)

			} else {
				// As colunas do retorno foram definidas
				if(this.countingTrue) {
					this.bQuery.append("SELECT COUNT( " + this.rootAlias + ") ");
				} else
				if(this.distinctTrue) {
					this.bQuery.append("SELECT DISTINCT ");
				} else {
					this.bQuery.append("SELECT ");
				}
				String v = "";
				for (String col : this.resultColumns) {
					this.bQuery.append(col + v);
					v = ",";
				}
			}
		}
	}

	private void buildFrom() {
		this.bQuery.append(" FROM " + this.beanClass.getSimpleName() + " " + this.rootAlias);
	}

	private void buildJoins() {
		for (String join : this.joins) {
			
			this.bQuery.append(" " + join);
			
		}
	}
	
	private void buidAnds() {

		for (AbstractCondition c : andConditions) {
			
			if (c.getSequence() == 0) {
				this.bQuery.append(" WHERE ");
			} else {
				this.bQuery.append(" AND ");
			}
			
			this.bQuery.append(c.getFragment());
			this.params.putAll(c.getFragmentParams());
		}

	}

	private void buildOrs() {
		Boolean envolver = andConditions.size() > 0 && orConditions.size() > 0; 
		if(envolver) {
			this.bQuery.append(" ( ");
		}
		
		for (AbstractCondition c : orConditions) {
			
			if (c.getSequence() == 0) {
				this.bQuery.append(" WHERE ");
			} else {
				this.bQuery.append(" OR ");
			}
			
			this.bQuery.append(c.getFragment());
			this.params.putAll(c.getFragmentParams());
		}
		if(envolver) {
			this.bQuery.append(" ) ");
		}
	}

	private void buildOrderes() {
		
		if(!ascOrderByConditions.isEmpty() || !descOrderByConditions.isEmpty()) {
			this.bQuery.append(" ORDER ");
		}
		
		for (String o : ascOrderByConditions) {
			this.bQuery.append(o + " ASC, ");
		}
		for (String o : descOrderByConditions) {
			this.bQuery.append(o + " DESC, ");
		}
		
		if(!ascOrderByConditions.isEmpty() || !descOrderByConditions.isEmpty()) {
			this.bQuery.delete(this.bQuery.length() - 1, this.bQuery.length());
		}
		
	}
	
	private String getAttributePath(String attribute) {
		
		if(attribute.contains(".")) {

			String path = attribute.substring(0, attribute.lastIndexOf("."));
			
			if(this.joinsPaths.containsKey(path)) {
				return attribute.replace(path, this.joinsPaths.get(path));
			} else {
				throw new IllegalArgumentException("O join \"" + path + "\" não exite");
			}
			
		} else {
			return this.rootAlias + "." + attribute;
		}
		
	}
	
	private void putParams() {
		for(String p : this.params.keySet()) {
			
			//TODO Tratar tipo: Data, etc
			query.setParameter(p, this.params.get(p));
		}
	}

	/*private class Condition {

		private AndConditionType type;

		private String attribute;

		private Object value;

		private Object valueB;

		private Boolean toLowerCase;

		public Condition(AndConditionType type, String attribute, Object value, Boolean toLowerCase) {
			super();
			this.toLowerCase = toLowerCase;
			this.attribute = attribute;
			this.value = value;
			this.type = type;
		}

		public Condition(AndConditionType type, String attribute, Object value, Object valueB, Boolean toLowerCase) {
			super();
			this.toLowerCase = toLowerCase;
			this.attribute = attribute;
			this.value = value;
			this.type = type;
		}

		public Boolean getToLowerCase() {
			return toLowerCase;
		}

		public String getAttribute() {
			return attribute;
		}

		public Object getValue() {
			return value;
		}

		public AndConditionType getType() {
			return type;
		}

		public Object getValueB() {
			return valueB;
		}
	}*/

	@Override
	public String toString() {
		
		return super.toString();
	}
	
}
