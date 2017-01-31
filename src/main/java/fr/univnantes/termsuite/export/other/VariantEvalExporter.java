package fr.univnantes.termsuite.export.other;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.google.common.collect.Lists;
import com.google.inject.Inject;

import fr.univnantes.termsuite.api.TermSuiteException;
import fr.univnantes.termsuite.export.TerminologyExporter;
import fr.univnantes.termsuite.framework.service.TerminologyService;
import fr.univnantes.termsuite.model.OccurrenceStore;
import fr.univnantes.termsuite.model.RelationProperty;
import fr.univnantes.termsuite.model.Term;
import fr.univnantes.termsuite.model.TermOccurrence;
import fr.univnantes.termsuite.model.TermRelation;
import fr.univnantes.termsuite.utils.TermOccurrenceUtils;

public class VariantEvalExporter implements TerminologyExporter {

	@Inject
	private TerminologyService termino;
	
	@Inject
	private Writer writer;

	@Inject
	private OccurrenceStore occurrenceStore;
	
	private int nbVariantsPerTerm;
	private int contextSize;
	private int nbExampleOccurrences;
	private int topN;
	
	public void export() {
		try {
			AtomicInteger rank = new AtomicInteger(0);
			AtomicInteger variantCnt = new AtomicInteger(0);
			for(Term t:termino.getTerms()) {
				if(!termino.outboundRelations(t).findAny().isPresent())
					continue;
				printBase(rank.incrementAndGet(), t);
				AtomicInteger variantRank = new AtomicInteger(0);
				termino.variationsFrom(t).forEach(variation -> {
					if(variantRank.intValue() >= nbVariantsPerTerm)
						return;
					variantCnt.getAndIncrement();
					variantRank.getAndIncrement();
					try {
						printVariation(rank.intValue(), variantRank.intValue(), variation);
						printTermOccurrences(variation.getTo());
					} catch(IOException e) {
						throw new TermSuiteException(e);
					}
				});
				
				if(variantCnt.intValue()>this.topN)
					break;
			}

		} catch (IOException e) {
			throw new TermSuiteException(e);
		}
	}
	
	private void printVariation(int termRank, int variantRank, TermRelation variation) throws IOException {
		Term variant = variation.getTo();
		String pilot = variant.getPilot();
		writer.write(Integer.toString(termRank));
		writer.write("\t");
		writer.write("V_" + Integer.toString(variantRank));
		writer.write("\t");
		writer.write(String.format("<%s>", variation.getPropertyStringValue(RelationProperty.VARIATION_RULE, "[]")));
		writer.write("\t");
		writer.write(String.format("%s (%d)", pilot, variant.getFrequency()));
		writer.write("\t");
		writer.write(String.format("[%s]", variant.getGroupingKey()));
		writer.write("\t");
		writer.write("{is_variant: _0_or_1_, variant_type: _syn_termino_other_}");
		writer.write("\n");		
	}

	private void printBase(int rank, Term t) throws IOException {
		writer.write(Integer.toString(rank));
		writer.write("\t");
		writer.write("T");
		writer.write("\t");
		writer.write(t.getPilot());
		writer.write("\t");
		writer.write(String.format("[%s]", t.getGroupingKey()));
		writer.write("\n");
	}

	private void printTermOccurrences(Term term) throws IOException {
		List<TermOccurrence> occurrences = Lists.newArrayList(occurrenceStore.getOccurrences(term));
		Collections.shuffle(occurrences);
		int occCnt = 0;
		for(TermOccurrence occurrence:occurrences) {
			if(occCnt > this.nbExampleOccurrences)
				break;
			printOccurrence(occurrence);
			occCnt++;
		}
	}

	private void printOccurrence(TermOccurrence occurrence) throws IOException {
		writer.write("#\t\t  ...");
		String textualContext = TermOccurrenceUtils.getTextualContext(occurrence, contextSize);
		writer.write(textualContext);
		writer.write("\n");
	}

}