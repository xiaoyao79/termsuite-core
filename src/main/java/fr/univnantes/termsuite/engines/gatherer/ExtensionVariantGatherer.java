package fr.univnantes.termsuite.engines.gatherer;

import static java.util.stream.Collectors.toList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;

import com.google.common.base.Stopwatch;

import fr.univnantes.termsuite.engines.SimpleEngine;
import fr.univnantes.termsuite.framework.InjectLogger;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.RelationType;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.Relation;
import fr.univnantes.termsuite.utils.TermHistory;
import fr.univnantes.termsuite.utils.TermUtils;

/**
 * 
 * Infers small-size terms variations on bigger-size extension terms.
 * 
 * When we have:
 * 	 1. horizontal-axis --M--> horizontal axis
 * 	 2. horizontal-axis wind turbine --IS_EXTENSION--> horizontal-axis
 * 	 3. horizontal axis wind turbine --IS_EXTENSION--> horizontal axis
 * 
 * We infer:
 *   horizontal-axis wind turbine --M--> horizontal axis wind turbine
 * 
 * @author Damien Cram
 *
 */
public class ExtensionVariantGatherer extends SimpleEngine {
	@InjectLogger Logger logger;

	
	private Optional<TermHistory> history = Optional.empty();
	
	public ExtensionVariantGatherer setHistory(TermHistory history) {
		this.history = Optional.ofNullable(history);
		return this;
	}
	
	@Override
	public void execute() {
		if(!terminology.extensions().findAny().isPresent())
			logger.warn("Skipping {}. No {} relation found.", this.getClass().getSimpleName(), RelationType.HAS_EXTENSION);
		
		terminology
			.variations()
			.filter(r -> !r.isPropertySet(RelationProperty.IS_INFERED))
			.forEach(r-> r.setProperty(RelationProperty.IS_INFERED, false));
		
		/*
		 * Infer variations for all types but VariationType.SYNTAGMATIC
		 * and VariationType.GRAPHICAL as:
		 *  1- syntagmatic extensions variants are supposed to be exhaustively
		 * listed in lang-multi-word-rule-system.regex resource.
		 *  2- graphical variants are merged later on
		 */
		inferVariations(VariationType.MORPHOLOGICAL);
		inferVariations(VariationType.DERIVATION);
		inferVariations(VariationType.PREFIXATION);
		inferVariations(VariationType.SEMANTIC);
	}


	public void inferVariations(VariationType type) {
		AtomicInteger cnt = new AtomicInteger(0);
		Stopwatch sw = Stopwatch.createStarted();
		terminology.variations()
			.filter(rel -> rel.isPropertySet(RelationProperty.VARIATION_TYPE))
			// Apply to all variations but syntagmatic ones
			.filter(rel -> rel.get(RelationProperty.VARIATION_TYPE) == type)
			.collect(toList())
			.forEach(relation -> {
				Term m1 = relation.getFrom();
				Term m2 = relation.getTo();
				
				List<Relation> m1Extensions = terminology.extensions(m1).collect(toList());
				for(Relation rel1:m1Extensions) {
					Term affix1 = TermUtils.getExtensionAffix(terminology, m1, rel1.getTo());
					if(affix1 == null)
						continue;
					
					List<Relation> m2extensions = terminology.extensions(m2).collect(toList());
					for(Relation rel2:m2extensions) {
							Term affix2 = TermUtils.getExtensionAffix(terminology, m2, rel2.getTo());
							if(affix2 == null)
								continue;

							if(Objects.equals(affix1, affix2)) {
								cnt.incrementAndGet();
								
								if(logger.isTraceEnabled()) 
									logger.trace("Found infered variation {} --> {}", rel1.getTo(), rel2.getTo());
								
								Relation inferedRel = terminology.createVariation(VariationType.INFERENCE, rel1.getTo(), rel2.getTo());
								inferedRel.setProperty(RelationProperty.IS_EXTENSION, false);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.SEMANTIC_SIMILARITY);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.IS_DICO);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.IS_DISTRIBUTIONAL);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.SEMANTIC_SCORE);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.IS_GRAPHICAL);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.IS_PREFIXATION);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.IS_MORPHOLOGICAL);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.IS_SYNTAGMATIC);
								copyRelationPropertyIfSet(relation, inferedRel, RelationProperty.IS_DERIVATION);
								watch(inferedRel, rel1, rel2);
							}
						}
					}
			});
		sw.stop();
		logger.debug("Infered {} variations of type {} in {}", cnt.intValue(), type, sw);
	}

	public void copyRelationPropertyIfSet(Relation baseRelation, Relation inferedRel, RelationProperty property) {
		if(baseRelation.isPropertySet(property))
			inferedRel.setProperty(
					property, 
					baseRelation.get(property));
	}

	private void watch(Relation inferedRel, Relation r1, Relation r2) {
		if(history.isPresent()) {
			if(history.get().isWatched(inferedRel.getTo()))
				history.get().saveEvent(inferedRel.getTo().getGroupingKey(), this.getClass(), String.format("New inbound relation {} infered from {} and {}", inferedRel, r1, r2));
			if(history.get().isWatched(inferedRel.getFrom()))
				history.get().saveEvent(inferedRel.getFrom().getGroupingKey(), this.getClass(), String.format("New outbound relation {} infered from {} and {}", inferedRel, r1, r2));
		}
	}
}
