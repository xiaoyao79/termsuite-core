/*******************************************************************************
 * Copyright 2015-2016 - CNRS (Centre National de Recherche Scientifique)
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 *
 *******************************************************************************/
package eu.project.ttc.engines.exporter;

import java.util.List;

import org.apache.uima.UimaContext;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.descriptor.ConfigurationParameter;
import org.apache.uima.resource.ResourceInitializationException;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import eu.project.ttc.engines.AbstractTermIndexExporter;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.termino.export.VariationExporter;
import eu.project.ttc.utils.TermSuiteConstants;

public class VariationExporterAE extends AbstractTermIndexExporter {

	public static final String VARIATION_TYPES = "VariationTypes";
	@ConfigurationParameter(name = VARIATION_TYPES, mandatory=true)
	private String variationTypeStrings;
	protected List<VariationType> variationTypes;

	@Override
	public void initialize(UimaContext context) throws ResourceInitializationException {
		super.initialize(context);
		List<String> strings = Splitter.on(TermSuiteConstants.COMMA).splitToList(variationTypeStrings);
		variationTypes = Lists.newArrayList();
		for(String vType:strings) 
			variationTypes.add(VariationType.valueOf(vType));
	}
	
	@Override
	public void collectionProcessComplete() throws AnalysisEngineProcessException {
		VariationExporter.export(termIndexResource.getTermIndex(), writer, variationTypes);
	}
}