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
package fr.univnantes.termsuite.engines.gatherer;

import java.util.Collection;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;

public class YamlRuleSet  {
	private Multimap<RuleType, VariantRule> variantRules;

	public Collection<VariantRule> getVariantRules(RuleType key) {
		return variantRules.get(key);
	}

	public YamlRuleSet(Iterable<VariantRule> rules) {
		variantRules = HashMultimap.create();
		for(VariantRule rule:rules) 
			variantRules.put(rule.getRuleType(), rule);
	}
	
	public Collection<VariantRule> getVariantRules() {
		return variantRules.values();
	}
	
}