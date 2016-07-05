package eu.project.ttc.test.unit.selectors;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

import com.google.common.collect.Lists;

import eu.project.ttc.engines.desc.Lang;
import eu.project.ttc.models.CompoundType;
import eu.project.ttc.models.Term;
import eu.project.ttc.models.TermIndex;
import eu.project.ttc.models.index.MemoryTermIndex;
import eu.project.ttc.models.index.TermValueProvider;
import eu.project.ttc.models.index.TermValueProviders;
import eu.project.ttc.models.occstore.MemoryOccurrenceStore;
import eu.project.ttc.test.unit.TermFactory;

public class TermClassProvidersSpec {
	
	private Term machine_synchrone, synchrone, asynchrone, machine_statorique, machine_de_stator, statorique,
				stator, machine_synchrone_de_stator, hommegrenouille, hommegrenouille_de_stator, term11, aveccapitale;

	private TermIndex termIndex ;
	
	@Before
	public void init() {
		termIndex = new MemoryTermIndex("Test", Lang.FR, new MemoryOccurrenceStore());
		populateTermIndex(new TermFactory(termIndex));
	}
	
	private void populateTermIndex(TermFactory termFactory) {
		
		this.machine_synchrone = termFactory.create("N:machine|machin", "A:synchrone|synchro");
		this.synchrone = termFactory.create("A:synchrone|synchron");
		this.asynchrone = termFactory.create("A:asynchrone|asynchron");
		this.machine_statorique = termFactory.create("N:machine|machin", "A:statorique|statoric");
		this.machine_de_stator = termFactory.create("N:machine|machin", "P:de|de", "N:stator|stator");
		this.statorique = termFactory.create("A:statorique|statoric");
		this.stator = termFactory.create("N:stator|stator");
		this.machine_synchrone_de_stator = termFactory.create("N:machine|machin", "A:synchrone|synchron", "P:de|de", "N:stator|stator");
		this.hommegrenouille = termFactory.create("N:homme-grenouille|homme-grenouille");
		this.hommegrenouille_de_stator = termFactory.create("N:homme-grenouille|homme-grenouille", "P:de|de", "N:stator|stator");
		this.term11 = termFactory.create("N:machine|machin");
		this.aveccapitale = termFactory.create("N:Aveccapitale|Aveccapital");
		termFactory.wordComposition(CompoundType.NATIVE, "homme-grenouille", "homme|homme", "grenouille|grenouille");
		termFactory.addPrefix(this.asynchrone, this.synchrone);
		termFactory.addDerivesInto("N A", this.stator, this.statorique);
	}

	@Test
	public void testWordLemmaLemmaProvider() {
		TermValueProvider provider = TermValueProviders.WORD_LEMMA_LEMMA_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(1)
			.contains("machine+synchrone");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(1).contains("machine+statorique");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(1).contains("machine+stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(3).contains("machine+stator", "machine+synchrone", "stator+synchrone");
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1).contains("grenouille+homme");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(4).contains(
					"homme-grenouille+stator", "grenouille+homme", "grenouille+stator", "homme+stator");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(0);
	}

	@Test
	public void testWordLemmaStemProvider() {
		TermValueProvider provider = TermValueProviders.WORD_LEMMA_STEM_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(1)
			.contains("machine+synchro");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(1).contains("machine+statoric");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(1).contains("machine+stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(3).contains("machine+stator", "machine+synchro", "stator+synchro");
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(1).contains("homme-grenouille+stator");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(0);
	}

	@Test
	public void testTermLemmaLowerCaseProvider() {
		TermValueProvider provider = TermValueProviders.TERM_LEMMA_LOWER_CASE_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(1)
			.contains("machine synchrone");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(1).contains("machine statorique");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(1).contains("machine de stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(1).contains("machine synchrone de stator");
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(1).contains("homme-grenouille de stator");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(1).contains("aveccapitale");
	}

	@Test
	public void testWordNoClassProvider() {
		TermValueProvider provider = TermValueProviders.TERM_NOCLASS_PROVIDER;
		for(Term t:Lists.newArrayList(machine_synchrone, synchrone, asynchrone, machine_statorique, machine_de_stator, statorique, stator, machine_synchrone_de_stator, hommegrenouille, hommegrenouille_de_stator, term11))
			assertThat(provider.getClasses(termIndex, t))
				.hasSize(1)
				.contains("noclass");
	}

	@Test
	public void testSingleWordLemmaProvider() {
		TermValueProvider provider = TermValueProviders.TERM_SINGLE_WORD_LEMMA_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(0);
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(1).contains("Aveccapitale");
	}

	@Test
	public void testWordLemmaProvider() {
		TermValueProvider provider = TermValueProviders.WORD_LEMMA_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(2)
			.contains("machine", "synchrone");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(2).contains("machine", "statorique");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(3).contains("machine", "de", "stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(4).contains("machine", "synchrone", "de", "stator");
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(3).contains("homme-grenouille", "de", "stator");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(1).contains("aveccapitale");
	}
	
	@Test
	public void testWordLemmaIfSWTProvider() {
		TermValueProvider provider = TermValueProviders.WORD_LEMMA_IF_SWT_PROVIDER;
		assertThat(provider.getClasses(termIndex, machine_synchrone))
			.hasSize(2)
			.contains("machine", "synchrone");
		assertThat(provider.getClasses(termIndex, synchrone))
			.hasSize(1).contains("synchrone");
		assertThat(provider.getClasses(termIndex, asynchrone))
			.hasSize(1).contains("asynchrone");
		assertThat(provider.getClasses(termIndex, machine_statorique))
			.hasSize(2).contains("machine", "statorique");
		assertThat(provider.getClasses(termIndex, machine_de_stator))
			.hasSize(2).contains("machine", "stator");
		assertThat(provider.getClasses(termIndex, statorique))
			.hasSize(1).contains("statorique");
		assertThat(provider.getClasses(termIndex, stator))
			.hasSize(1).contains("stator");
		assertThat(provider.getClasses(termIndex, machine_synchrone_de_stator))
			.hasSize(3).contains("machine", "synchrone", "stator");
		assertThat(provider.getClasses(termIndex, hommegrenouille))
			.hasSize(1).contains("homme-grenouille");
		assertThat(provider.getClasses(termIndex, hommegrenouille_de_stator))
			.hasSize(2).contains("homme-grenouille", "stator");
		assertThat(provider.getClasses(termIndex, aveccapitale))
			.hasSize(1).contains("Aveccapitale");

	}
}
