package com.marklogic.client.config.support;

import java.util.Arrays;
import java.util.List;

import com.marklogic.client.config.QueryOptions.FragmentScope;

/**
 * Interface that wraps building configuration options for a QueryOptions
 * object. It consists of a bunch of getters in order to access state, and a
 * range of fluent setters that are used to successively build up a
 * configuration.
 */
public class QueryOptionsConfiguration {
	private Integer concurrencyLevel;

	private Boolean debug;

	private List<Long> forests;

	private FragmentScope fragmentScope;

	private Long pageLength;

	private Boolean returnAggregates;

	private Boolean returnConstraints;

	private Boolean returnFacets;

	private Boolean returnFrequencies;

	private Boolean returnMetrics;

	private Boolean returnPlan;

	private Boolean returnQtext;

	private Boolean returnQuery;

	private Boolean returnResults;

	private Boolean returnSimilar;

	private Boolean returnValues;

	private List<String> searchOptions;

	private Double qualityWeight;


	public Integer getConcurrencyLevel() {
		return concurrencyLevel;
	}

	public Boolean getDebug() {
		return debug;
	}

	public List<Long> getForests() {
		return forests;
	}

	public FragmentScope getFragmentScope() {
		return fragmentScope;
	}

	public Long getPageLength() {
		return pageLength;
	}

	public Boolean getReturnAggregates() {
		return returnAggregates;
	}

	public Boolean getReturnConstraints() {
		return returnConstraints;
	}

	public Boolean getReturnFacets() {
		return returnFacets;
	}

	public Boolean getReturnFrequencies() {
		return returnFrequencies;
	}

	public Boolean getReturnMetrics() {
		return returnMetrics;
	}

	public Boolean getReturnPlan() {
		return returnPlan;
	}

	public Boolean getReturnQtext() {
		return returnQtext;
	}

	public Boolean getReturnQuery() {
		return returnQuery;
	}

	public Boolean getReturnResults() {
		return returnResults;
	}

	public Boolean getReturnSimilar() {
		return returnSimilar;
	}

	public Boolean getReturnValues() {
		return returnValues;
	}

	public List<String> getSearchOptions() {
		return searchOptions;
	}

	public QueryOptionsConfiguration concurrencyLevel(Integer concurrencyLevel) {
        this.concurrencyLevel = concurrencyLevel;
        return this;
    }

	public QueryOptionsConfiguration debug(Boolean debug) {
        this.debug = debug;
        return this;
    }

	public QueryOptionsConfiguration forests(Long... forestIds) {
        this.forests = Arrays.asList(forestIds);
        return this;
    }

	public QueryOptionsConfiguration fragmentScope(FragmentScope scope) {
        this.fragmentScope = scope;
        return this;
    }

	public QueryOptionsConfiguration pageLength(Long pageLength) {
        this.pageLength = pageLength;
        return this;
    }

	public QueryOptionsConfiguration qualityWeight(Double qualityWeight) {
        this.qualityWeight = qualityWeight;
        return this;
    }

	public QueryOptionsConfiguration returnAggregates(Boolean returnAggregates) {
        this.returnAggregates = returnAggregates;
        return this;
    }

	public QueryOptionsConfiguration returnConstraints(Boolean returnConstraints) {
        this.returnConstraints = returnConstraints;
        return this;
    }

	public QueryOptionsConfiguration returnFacets(Boolean returnFacets) {
        this.returnFacets = returnFacets;
        return this;
    }

	public QueryOptionsConfiguration returnFrequencies(Boolean returnFrequencies) {
        this.returnFrequencies = returnFrequencies;
        return this;
    }

	public QueryOptionsConfiguration returnMetrics(Boolean returnMetrics) {
        this.returnMetrics = returnMetrics;
        return this;
    }

	public QueryOptionsConfiguration returnPlan(Boolean returnPlan) {
        this.returnPlan = returnPlan;
        return this;
    }

	public QueryOptionsConfiguration returnQtext(Boolean returnQtext) {
        this.returnQtext = returnQtext;
        return this;
    }

	public QueryOptionsConfiguration returnQuery(Boolean returnQuery) {
        this.returnQuery = returnQuery;
        return this;
    }

	public QueryOptionsConfiguration returnResults(Boolean returnResults) {
        this.returnResults = returnResults;
        return this;
    }

	public QueryOptionsConfiguration returnSimilar(Boolean returnSimilar) {
        this.returnSimilar = returnSimilar;
        return this;
    }

	public QueryOptionsConfiguration returnValues(Boolean returnValues) {
        this.returnValues = returnValues;
        return this;
    }

	
	public QueryOptionsConfiguration searchOptions(String... options) {
        this.searchOptions = Arrays.asList(options);
        return this;
    }

	public void setConcurrencyLevel(Integer concurrencyLevel) {
		this.concurrencyLevel = concurrencyLevel;
	}

	public void setDebug(Boolean debug) {
		this.debug = debug;
	}

	public void setForests(List<Long> forests) {
		this.forests = forests;
	}

	public void setFragmentScope(FragmentScope fragmentScope) {
		this.fragmentScope = fragmentScope;
	}

	public void setPageLength(Long pageLength) {
		this.pageLength = pageLength;
	}

	public void setReturnAggregates(Boolean returnAggregates) {
		this.returnAggregates = returnAggregates;
	}

	public void setReturnConstraints(Boolean returnConstraints) {
		this.returnConstraints = returnConstraints;
	}

	public void setReturnFacets(Boolean returnFacets) {
		this.returnFacets = returnFacets;
	}

	public void setReturnFrequencies(Boolean returnFrequencies) {
		this.returnFrequencies = returnFrequencies;
	}

	public void setReturnMetrics(Boolean returnMetrics) {
		this.returnMetrics = returnMetrics;
	}

	public void setReturnPlan(Boolean returnPlan) {
		this.returnPlan = returnPlan;
	}

	public void setReturnQtext(Boolean returnQtext) {
		this.returnQtext = returnQtext;
	}

	public void setReturnQuery(Boolean returnQuery) {
		this.returnQuery = returnQuery;
	}

	public void setReturnResults(Boolean returnResults) {
		this.returnResults = returnResults;
	}

	public void setReturnSimilar(Boolean returnSimilar) {
		this.returnSimilar = returnSimilar;
	}

	public void setReturnValues(Boolean returnValues) {
		this.returnValues = returnValues;
	}

	public void setSearchOptions(List<String> searchOptions) {
		this.searchOptions = searchOptions;
	}

	public Double getQualityWeight() {
		return qualityWeight;
	}
}