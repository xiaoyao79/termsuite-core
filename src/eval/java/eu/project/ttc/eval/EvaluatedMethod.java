package eu.project.ttc.eval;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.project.ttc.engines.BilingualAligner;
import eu.project.ttc.engines.BilingualAligner.RequiresSize2Exception;
import eu.project.ttc.engines.BilingualAligner.TranslationCandidate;
import eu.project.ttc.models.Term;

public enum EvaluatedMethod {
	DISTRIBUTIONAL("distrib"),
	COMPOSITIONAL("compo"),
	SEMI_DISTRIBUTIONAL("semi-distrib"),
	DICO("dico"),
	HYBRID("hybrid");

	private static final Logger LOGGER = LoggerFactory.getLogger(EvaluatedMethod.class);
	
	private String dirName;
	
	private EvaluatedMethod(String dirName) {
		this.dirName = dirName;
	}

	public boolean acceptPair(BilingualAligner aligner, Term source, Term target) {
		try {
			switch(this) {
			case DISTRIBUTIONAL: return source.isSingleWord() && target.isSingleWord();
			case COMPOSITIONAL: return aligner.canAlignCompositional(source) && target.isMultiWord();
			case SEMI_DISTRIBUTIONAL: return aligner.canAlignSemiDistributional(source) && target.isMultiWord();
			case DICO: return source.isSingleWord() && target.isSingleWord();
			case HYBRID: return true;
			default:
				throw new UnsupportedOperationException("Method not supported for " + this);
			}
		} catch (RequiresSize2Exception e) {
			LOGGER.warn("Terms of size > 2 temporary not supported by evaluator. Term: " + source);
			return false;
		}
	}

	public List<TranslationCandidate> align(BilingualAligner aligner, Term sourceTerm, int nbCandidates, int minCandidateFrequency) {
		switch(this) {
		case DISTRIBUTIONAL: return aligner.alignDistributional(sourceTerm, nbCandidates, minCandidateFrequency);
		case COMPOSITIONAL: return aligner.alignCompositional(sourceTerm, nbCandidates, minCandidateFrequency);
		case SEMI_DISTRIBUTIONAL: return aligner.alignSemiDistributional(sourceTerm, nbCandidates, minCandidateFrequency);
		case DICO: return aligner.alignDico(sourceTerm, nbCandidates);
		case HYBRID: return aligner.align(sourceTerm, nbCandidates, minCandidateFrequency);
		default:
			throw new UnsupportedOperationException("Method not supported for " + this);
		}
	}
	
	@Override
	public String toString() {
		return this.dirName;
	}
}