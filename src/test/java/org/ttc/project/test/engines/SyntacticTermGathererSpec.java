package org.ttc.project.test.engines;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import org.apache.uima.analysis_engine.AnalysisEngine;
import org.apache.uima.analysis_engine.AnalysisEngineDescription;
import org.apache.uima.analysis_engine.AnalysisEngineProcessException;
import org.apache.uima.fit.factory.AnalysisEngineFactory;
import org.apache.uima.fit.factory.ExternalResourceFactory;
import org.apache.uima.resource.ExternalResourceDescription;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.util.InvalidXMLException;
import org.junit.Before;
import org.junit.Test;
import org.ttc.project.Fixtures;
import org.ttc.project.TermFactory;

import eu.project.ttc.engines.SyntacticTermGatherer;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.VariationType;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.resources.MemoryTermIndexManager;
import eu.project.ttc.resources.TermIndexResource;
import eu.project.ttc.resources.YamlVariantRules;

public class SyntacticTermGathererSpec {
	private MemoryTermIndex termIndex;
	private Term machine_synchrone;
	private Term machine_asynchrone;
	private Term synchrone;
	private Term asynchrone;
	private Term stator;
	private Term phase_statorique;
	private Term statorique;
	private Term phase_du_stator;
	private Term geothermie_hydraulique_solaire;
	private Term geothermie_hydraulique;
	private Term hormonosensibilite;
	private Term sensible_aux_hormones;
	
	private AnalysisEngine ae;
	
	@Before
	public void set() throws Exception {
		this.termIndex = Fixtures.termIndex();
		makeAE();
		populateTermIndex(new TermFactory(termIndex));
		ae.collectionProcessComplete();
	}

	private void populateTermIndex(TermFactory termFactory) {
		
		this.machine_synchrone = termFactory.create("N:machine|machin", "A:synchrone|synchro");
		this.machine_asynchrone = termFactory.create("N:machine|machin", "A:asynchrone|asynchro");
		this.synchrone = termFactory.create("A:synchrone|synchron");
		this.asynchrone = termFactory.create("A:asynchrone|asynchron");

		this.stator = termFactory.create("N:stator|stator");
		this.statorique = termFactory.create("A:statorique|statoric");
		this.phase_statorique = termFactory.create("N:phase|phas", "A:statorique|statoric");
		this.phase_du_stator = termFactory.create("N:phase|phas", "P:de|de", "N:stator|stator");

		this.geothermie_hydraulique_solaire = termFactory.create(
				"N:geothermie|géotherm", "A:hydraulique|hydraulic", "A:solaire|solair");
		this.geothermie_hydraulique = termFactory.create(
				"N:geothermie|géotherm", "A:hydraulique|hydraulic");

		this.hormonosensibilite = termFactory.create(
				"N:hormonosensibilité|hormonosensibilit");
		this.sensible_aux_hormones = termFactory.create("N:sensibilité|sensibilité", "P:à|à", "N:hormone|hormone");
		termFactory.wordComposition(CompoundType.NATIVE, "hormonosensibilité", "hormono|hormone", "sensibilité|sensibilité");
		
		termFactory.addPrefix(this.asynchrone, this.synchrone);
		termFactory.addDerivesInto("N A", this.stator, this.statorique);
	}

	private void makeAE() throws ResourceInitializationException, InvalidXMLException, ClassNotFoundException {
		MemoryTermIndexManager manager = MemoryTermIndexManager.getInstance();
		manager.clear();
		manager.register(termIndex);
		AnalysisEngineDescription aeDesc = AnalysisEngineFactory.createEngineDescription(
				SyntacticTermGatherer.class
			);
		

		/*
		 * The term index resource
		 */
		ExternalResourceDescription termIndexDesc = ExternalResourceFactory.createExternalResourceDescription(
				TermIndexResource.TERM_INDEX,
				TermIndexResource.class, 
				this.termIndex.getName()
		);
		ExternalResourceFactory.bindResource(aeDesc, termIndexDesc);

		/*
		 * The rule list resources
		 */
		ExternalResourceDescription rulesDesc = ExternalResourceFactory.createExternalResourceDescription(
				SyntacticTermGatherer.YAML_VARIANT_RULES,
				YamlVariantRules.class, 
				"file:org/project/ttc/test/resources/variant-rules.yaml"
		);
		ExternalResourceFactory.bindResource(aeDesc, rulesDesc);
		
		ae = AnalysisEngineFactory.createEngine(aeDesc);
	}
	
	@Test
	public void testProcessDefault() throws AnalysisEngineProcessException{
		assertThat(this.geothermie_hydraulique.getVariations())
			.hasSize(1)
			.extracting("variationType", "variant")
			.contains(tuple(VariationType.SYNTACTICAL, this.geothermie_hydraulique_solaire));
		
		assertThat(this.geothermie_hydraulique_solaire.getVariations())
			.hasSize(0);
	}

	
	@Test
	public void testProcessPrefix() throws AnalysisEngineProcessException{
		assertThat(this.machine_synchrone.getVariations())
			.hasSize(1)
			.extracting("variationType", "info", "variant")
			.contains(tuple(VariationType.SYNTACTICAL, "NA-NprefA", this.machine_asynchrone));
		
		assertThat(this.machine_asynchrone.getVariations())
			.hasSize(0);
	}

	@Test
	public void testProcessCompoundRules() throws AnalysisEngineProcessException{
		assertThat(this.hormonosensibilite.getVariations())
			.hasSize(1)
			.extracting("variationType", "info", "variant")
			.contains(tuple(VariationType.MORPHOLOGICAL, "M-PI-EN-P", this.sensible_aux_hormones));
	}

	
	@Test
	public void testProcessDerivation() throws AnalysisEngineProcessException{
		assertThat(this.phase_du_stator.getVariations())
			.hasSize(1)
			.extracting("variationType", "info", "variant")
			.contains(tuple(VariationType.SYNTACTICAL, "S-R2D-NPN", this.phase_statorique));
		assertThat(this.phase_statorique.getVariations())
			.hasSize(0);
		
	}

}
