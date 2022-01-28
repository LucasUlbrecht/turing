/*
 * Copyright (C) 2016-2021 the original author or authors. 
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.viglet.turing.api.sn.search;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.solr.client.solrj.response.SpellCheckResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.viglet.turing.solr.TurSolr;
import com.viglet.turing.solr.TurSolrInstance;
import com.viglet.turing.solr.TurSolrInstanceProcess;
import com.viglet.turing.persistence.model.sn.TurSNSite;
import com.viglet.turing.persistence.repository.sn.TurSNSiteRepository;
import com.viglet.turing.se.TurSEStopword;

import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/sn/{siteName}/ac")
@Tag(name = "Semantic Navigation Auto Complete", description = "Semantic Navigation Auto Complete API")
public class TurSNSiteAutoCompleteAPI {
	private static final Log logger = LogFactory.getLog(TurSNSiteAutoCompleteAPI.class);
	@Autowired
	private TurSolr turSolr;
	@Autowired
	private TurSNSiteRepository turSNSiteRepository;
	@Autowired
	private TurSEStopword turSEStopword;
	@Autowired
	private TurSolrInstanceProcess turSolrInstanceProcess;

	@GetMapping
	public List<String> turSNSiteAutoComplete(@PathVariable String siteName,
			@RequestParam(required = true, name = TurSNParamType.QUERY) String q,
			@RequestParam(required = false, defaultValue = "20", name = TurSNParamType.ROWS) long rows,
			@RequestParam(required = false, name = TurSNParamType.LOCALE) String locale, HttpServletRequest request) {

		SpellCheckResponse turSEResults = executeAutoCompleteFromSE(siteName, locale, q);
		int tokenQSize = q.split(" ").length;
		List<String> termListFormatted = createFormattedList(turSEResults, tokenQSize);
		List<String> termListShrink = removeDuplicatedTerms(termListFormatted, tokenQSize);
		return termListShrink.stream().limit(rows).collect(Collectors.toList());
	}

	private SpellCheckResponse executeAutoCompleteFromSE(String siteName, String locale, String q) {
		TurSNSite turSNSite = turSNSiteRepository.findByName(siteName);
		SpellCheckResponse turSEResults = null;
		Optional<TurSolrInstance> turSolrInstance = turSolrInstanceProcess.initSolrInstance(turSNSite, locale);
		if (turSolrInstance.isPresent()) {
			try {
				turSEResults = turSolr.autoComplete(turSolrInstance.get(), q);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return turSEResults;
	}

	private List<String> createFormattedList(SpellCheckResponse turSEResults, int tokenQSize) {
		List<String> termListFormatted = new ArrayList<>();
		if (hasSuggestions(turSEResults)) {
			List<String> termList = turSEResults.getSuggestions().get(0).getAlternatives();
			FormatteListData formatteListData = new FormatteListData();
			termList.forEach(term -> processTern(tokenQSize, termListFormatted, formatteListData, term));
		}
		return termListFormatted;
	}

	private void processTern(int tokenQSize, List<String> termListFormatted, FormatteListData formatteListData,
			String term) {
		String[] token = term.split(" ");
		String lastToken = token[token.length - 1];
		if (token.length > tokenQSize) {
			if (formatteListData.getPreviousTerm() == null) {
				firstIter(termListFormatted, formatteListData, term, lastToken);
			} else {
				otherIter(termListFormatted, formatteListData, term, lastToken);
			}

		} else {
			finishTermList(termListFormatted, formatteListData, term);
		}
	}

	private void finishTermList(List<String> termListFormatted, FormatteListData formatteListData, String term) {
		termListFormatted.add(term);
		formatteListData.setPreviousTerm(term + "");
	}

	private void otherIter(List<String> termListFormatted, FormatteListData formatteListData, String term,
			String lastToken) {
		if (formatteListData.isPreviousFinishedStopWords()) {
			if (!formatteListData.getStopWords().contains(lastToken)) {
				termListFormatted.add(term);
				formatteListData.setPreviousFinishedStopWords(false);
			}
		} else {
			formatteListData.setPreviousFinishedStopWords(true);
		}
	}

	private void firstIter(List<String> termListFormatted, FormatteListData formatteListData, String term,
			String lastToken) {
		if (!formatteListData.getStopWords().contains(lastToken)
				|| !term.contains(formatteListData.getPreviousTerm())) {
			termListFormatted.add(term);
			formatteListData.setPreviousFinishedStopWords(false);
		} else {
			formatteListData.setPreviousFinishedStopWords(true);
		}
		formatteListData.setPreviousTerm(term);
	}

	private boolean hasSuggestions(SpellCheckResponse turSEResults) {
		return turSEResults != null && turSEResults.getSuggestions() != null
				&& !turSEResults.getSuggestions().isEmpty();
	}

	private List<String> removeDuplicatedTerms(List<String> termListFormatted, int tokenQSize) {
		List<String> termListShrink = new ArrayList<>();
		String previousTerm = null;
		for (String term : termListFormatted) {
			int currentIndex = termListFormatted.indexOf(term);
			String[] token = term.split(" ");
			if ((token.length <= tokenQSize + 1) || previousTerm == null) {
				termListShrink.add(term);
				previousTerm = term;
			} else {
				if (!term.contains(previousTerm)) {
					termListShrink.add(term);
				} else {
					if ((termListFormatted.size() > currentIndex + 1)
							&& (!termListFormatted.get(currentIndex + 1).contains(term))) {
						termListShrink.add(term);
					}
				}
			}
		}
		return termListShrink;
	}

	class FormatteListData {
		private List<String> stopWords = turSEStopword.getStopWords();
		private String previousTerm = null;
		private boolean previousFinishedStopWords = false;

		public List<String> getStopWords() {
			return stopWords;
		}

		public void setStopWords(List<String> stopWords) {
			this.stopWords = stopWords;
		}

		public String getPreviousTerm() {
			return previousTerm;
		}

		public void setPreviousTerm(String previousTerm) {
			this.previousTerm = previousTerm;
		}

		public boolean isPreviousFinishedStopWords() {
			return previousFinishedStopWords;
		}

		public void setPreviousFinishedStopWords(boolean previousFinishedStopWords) {
			this.previousFinishedStopWords = previousFinishedStopWords;
		}

	}
}
