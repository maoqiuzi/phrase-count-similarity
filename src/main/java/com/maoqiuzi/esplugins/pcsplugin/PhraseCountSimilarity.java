package com.maoqiuzi.esplugins.pcsplugin;

import org.apache.lucene.index.FieldInvertState;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.NumericDocValues;
import org.apache.lucene.search.CollectionStatistics;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.TermStatistics;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;

/**
 * Created by maoqiuzi on 5/15/17.
 */

public class PhraseCountSimilarity extends Similarity {

    // don't use queryNorm
    public float queryNorm(float valueForNormalization) {
        return 1.0F;
    }

    //called at indexing time to compute the norm of a document
    // don't use norm
    public final long computeNorm(FieldInvertState state) {
        return (long)1;
    }

    private static class PhraseCountStats extends Similarity.SimWeight {

        @Override
        public float getValueForNormalization() {
            return 1.0f;
        }

        @Override
        public void normalize(float queryNorm, float topLevelBoost) {;}
    }

    @Override
    public final SimWeight computeWeight(CollectionStatistics collectionStats, TermStatistics... termStats) {
        float numTerms = (float)termStats.length;
        return new PhraseCountStats();
    }

    @Override
    public final SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
        return new PhraseCountScorer();
    }

    private final class PhraseCountScorer extends SimScorer {

        @Override
        public float score(int doc, float freq) {
            return freq;
        }

        //we need to set this to 1 in order to not discriminate sloppy phrase
        @Override
        public float computeSlopFactor(int distance) {
            return 1.0f;
        }

        //we don't use payload
        @Override
        public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
            return 1.0f;
        }

        @Override
        public Explanation explain(int doc, Explanation freq) {
            return Explanation.match(freq.getValue(), "phrase frequency");
        }
    }

}