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

    public float queryNorm(float valueForNormalization) {
        return 1.0F;
    }

    //called at indexing time to compute the norm of a document
    public final long computeNorm(FieldInvertState state) {
        final int numTerms = state.getLength();
        return (long)numTerms;
    }

    private static class OverlapStats extends Similarity.SimWeight {
        private final String field;
        private float queryWeight;

        public OverlapStats(String field, float queryWeight) {
            this.field = field;
            this.queryWeight = queryWeight;
        }

        @Override
        public float getValueForNormalization() {
            return 1.0f;
        }

        @Override
        public void normalize(float queryNorm, float topLevelBoost) {
            float w = 1.0f/queryNorm;
            queryWeight = w*w;
        }
    }

    @Override
    public final SimWeight computeWeight(CollectionStatistics collectionStats, TermStatistics... termStats) {
        float numTerms = (float)termStats.length;
        return new OverlapStats(collectionStats.field(), numTerms);
    }

    @Override
    public final SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
        OverlapStats overlapStats = (OverlapStats) stats;
        return new OverlapScorer(overlapStats, context.reader().getNormValues(overlapStats.field));
    }

    private final class OverlapScorer extends SimScorer {
        private final OverlapStats stats;
        private final NumericDocValues norms;

        OverlapScorer(OverlapStats stats, NumericDocValues norms) throws IOException {
            this.stats = stats;
            this.norms = norms;
        }

        @Override
        public float score(int doc, float freq) {
            final float raw = 1.0f; //ignore freq
            long docWeight = norms.get(doc);
            float queryWeight = stats.queryWeight;
            float norm = Math.max((float)docWeight, queryWeight);
//            return norms == null ? raw : raw / norm;
            // TODO: 5/17/17 change it to actual phrase count 
            return 777.0f;
        }

        //we don't use phrases
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
            Explanation fieldNormExpl = Explanation.match(
                    norms != null ? (norms.get(doc)) : 1.0f,
                    "fieldNorm(doc=" + doc + ")");
            float queryWeight = stats.queryWeight;
            Explanation docWeightExpl = Explanation.match(queryWeight, "queryWeight");
            return Explanation.match(1.0f, "singlematch", fieldNormExpl, docWeightExpl);
        }
    }

}
//public class PhraseCountSimilarity extends Similarity {
//    public PhraseCountSimilarity() {
//    }
//
//    /** Implemented as <code>log(1 + (docCount - docFreq + 0.5)/(docFreq + 0.5))</code>. */
//    protected float idf(long docFreq, long docCount) {
//        return (float) Math.log(1 + (docCount - docFreq + 0.5D)/(docFreq + 0.5D));
//    }
//
//    /** Implemented as <code>1 / (distance + 1)</code>. */
//    protected float sloppyFreq(int distance) {
//        return 1.0f / (distance + 1);
//    }
//
//    /** The default implementation returns <code>1</code> */
//    protected float scorePayload(int doc, int start, int end, BytesRef payload) {
//        return 1;
//    }
//
//    /** The default implementation computes the average as <code>sumTotalTermFreq / docCount</code>,
//     * or returns <code>1</code> if the index does not store sumTotalTermFreq:
//     * any field that omits frequency information). */
//    protected float avgFieldLength(CollectionStatistics collectionStats) {
//        final long sumTotalTermFreq = collectionStats.sumTotalTermFreq();
//        if (sumTotalTermFreq <= 0) {
//            return 1f;       // field does not exist, or stat is unsupported
//        } else {
//            final long docCount = collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
//            return (float) (sumTotalTermFreq / (double) docCount);
//        }
//    }
//
//    /** The default implementation encodes <code>boost / sqrt(length)</code>
//     * with {@link SmallFloat#floatToByte315(float)}.  This is compatible with
//     * Lucene's default implementation.  If you change this, then you should
//     * change {@link #decodeNormValue(byte)} to match. */
//    protected byte encodeNormValue(float boost, int fieldLength) {
//        return SmallFloat.floatToByte315(boost / (float) Math.sqrt(fieldLength));
//    }
//
//    /** The default implementation returns <code>1 / f<sup>2</sup></code>
//     * where <code>f</code> is {@link SmallFloat#byte315ToFloat(byte)}. */
//    protected float decodeNormValue(byte b) {
//        return NORM_TABLE[b & 0xFF];
//    }
//
//    /**
//     * True if overlap tokens (tokens with a position of increment of zero) are
//     * discounted from the document's length.
//     */
//    protected boolean discountOverlaps = true;
//
//    /** Sets whether overlap tokens (Tokens with 0 position increment) are
//     *  ignored when computing norm.  By default this is true, meaning overlap
//     *  tokens do not count when computing norms. */
//    public void setDiscountOverlaps(boolean v) {
//        discountOverlaps = v;
//    }
//
//    /**
//     * Returns true if overlap tokens are discounted from the document's length.
//     * @see #setDiscountOverlaps
//     */
//    public boolean getDiscountOverlaps() {
//        return discountOverlaps;
//    }
//
//    /** Cache of decoded bytes. */
//    private static final float[] NORM_TABLE = new float[256];
//
//    static {
//        for (int i = 1; i < 256; i++) {
//            float f = SmallFloat.byte315ToFloat((byte)i);
//            NORM_TABLE[i] = 1.0f / (f*f);
//        }
//        NORM_TABLE[0] = 1.0f / NORM_TABLE[255]; // otherwise inf
//    }
//
//
//    @Override
//    public final long computeNorm(FieldInvertState state) {
//        final int numTerms = discountOverlaps ? state.getLength() - state.getNumOverlap() : state.getLength();
//        return encodeNormValue(state.getBoost(), numTerms);
//    }
//
//    /**
//     * Computes a score factor for a simple term and returns an explanation
//     * for that score factor.
//     *
//     * <p>
//     * The default implementation uses:
//     *
//     * <pre class="prettyprint">
//     * idf(docFreq, docCount);
//     * </pre>
//     *
//     * Note that {@link CollectionStatistics#docCount()} is used instead of
//     * {@link org.apache.lucene.index.IndexReader#numDocs() IndexReader#numDocs()} because also
//     * {@link TermStatistics#docFreq()} is used, and when the latter
//     * is inaccurate, so is {@link CollectionStatistics#docCount()}, and in the same direction.
//     * In addition, {@link CollectionStatistics#docCount()} does not skew when fields are sparse.
//     *
//     * @param collectionStats collection-level statistics
//     * @param termStats term-level statistics for the term
//     * @return an Explain object that includes both an idf score factor
//    and an explanation for the term.
//     */
//    public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats) {
//        final long df = termStats.docFreq();
//        final long docCount = collectionStats.docCount() == -1 ? collectionStats.maxDoc() : collectionStats.docCount();
//        final float idf = idf(df, docCount);
//        return Explanation.match(idf, "idf, computed as log(1 + (docCount - docFreq + 0.5) / (docFreq + 0.5)) from:",
//                Explanation.match(df, "docFreq"),
//                Explanation.match(docCount, "docCount"));
//    }
//
//    /**
//     * Computes a score factor for a phrase.
//     *
//     * <p>
//     * The default implementation sums the idf factor for
//     * each term in the phrase.
//     *
//     * @param collectionStats collection-level statistics
//     * @param termStats term-level statistics for the terms in the phrase
//     * @return an Explain object that includes both an idf
//     *         score factor for the phrase and an explanation
//     *         for each term.
//     */
//    public Explanation idfExplain(CollectionStatistics collectionStats, TermStatistics termStats[]) {
//        double idf = 0d; // sum into a double before casting into a float
//        List<Explanation> details = new ArrayList<>();
//        for (final TermStatistics stat : termStats ) {
//            Explanation idfExplain = idfExplain(collectionStats, stat);
//            details.add(idfExplain);
//            idf += idfExplain.getValue();
//        }
//        return Explanation.match((float) idf, "idf(), sum of:", details);
//    }
//
//    @Override
//    public final SimWeight computeWeight(CollectionStatistics collectionStats, TermStatistics... termStats) {
//        Explanation idf = termStats.length == 1 ? idfExplain(collectionStats, termStats[0]) : idfExplain(collectionStats, termStats);
//
//        float avgdl = avgFieldLength(collectionStats);
//
//        // compute freq-independent part of bm25 equation across all norm values
//        float cache[] = new float[256];
//        for (int i = 0; i < cache.length; i++) {
//            cache[i] = k1 * ((1 - b) + b * decodeNormValue((byte)i) / avgdl);
//        }
//        return new org.apache.lucene.search.similarities.BM25Similarity.BM25Stats(collectionStats.field(), idf, avgdl, cache);
//    }
//
//    @Override
//    public final SimScorer simScorer(SimWeight stats, LeafReaderContext context) throws IOException {
//        org.apache.lucene.search.similarities.BM25Similarity.BM25Stats bm25stats = (org.apache.lucene.search.similarities.BM25Similarity.BM25Stats) stats;
//        return new org.apache.lucene.search.similarities.BM25Similarity.BM25DocScorer(bm25stats, context.reader().getNormValues(bm25stats.field));
//    }
//
//    private class BM25DocScorer extends SimScorer {
//        private final org.apache.lucene.search.similarities.BM25Similarity.BM25Stats stats;
//        private final float weightValue; // boost * idf * (k1 + 1)
//        private final NumericDocValues norms;
//        private final float[] cache;
//
//        BM25DocScorer(org.apache.lucene.search.similarities.BM25Similarity.BM25Stats stats, NumericDocValues norms) throws IOException {
//            this.stats = stats;
//            this.weightValue = stats.weight * (k1 + 1);
//            this.cache = stats.cache;
//            this.norms = norms;
//        }
//
//        @Override
//        public float score(int doc, float freq) {
//            // if there are no norms, we act as if b=0
//            float norm = norms == null ? k1 : cache[(byte)norms.get(doc) & 0xFF];
//            return weightValue * freq / (freq + norm);
//        }
//
//        @Override
//        public Explanation explain(int doc, Explanation freq) {
//            return explainScore(doc, freq, stats, norms);
//        }
//
//        @Override
//        public float computeSlopFactor(int distance) {
//            return sloppyFreq(distance);
//        }
//
//        @Override
//        public float computePayloadFactor(int doc, int start, int end, BytesRef payload) {
//            return scorePayload(doc, start, end, payload);
//        }
//    }
//
//    /** Collection statistics for the BM25 model. */
//    private static class BM25Stats extends SimWeight {
//        /** BM25's idf */
//        private final Explanation idf;
//        /** The average document length. */
//        private final float avgdl;
//        /** query boost */
//        private float boost;
//        /** weight (idf * boost) */
//        private float weight;
//        /** field name, for pulling norms */
//        private final String field;
//        /** precomputed norm[256] with k1 * ((1 - b) + b * dl / avgdl) */
//        private final float cache[];
//
//        BM25Stats(String field, Explanation idf, float avgdl, float cache[]) {
//            this.field = field;
//            this.idf = idf;
//            this.avgdl = avgdl;
//            this.cache = cache;
//            normalize(1f, 1f);
//        }
//
//        @Override
//        public float getValueForNormalization() {
//            // we return a TF-IDF like normalization to be nice, but we don't actually normalize ourselves.
//            return weight * weight;
//        }
//
//        @Override
//        public void normalize(float queryNorm, float boost) {
//            // we don't normalize with queryNorm at all, we just capture the top-level boost
//            this.boost = boost;
//            this.weight = idf.getValue() * boost;
//        }
//    }
//
//    private Explanation explainTFNorm(int doc, Explanation freq, org.apache.lucene.search.similarities.BM25Similarity.BM25Stats stats, NumericDocValues norms) {
//        List<Explanation> subs = new ArrayList<>();
//        subs.add(freq);
//        subs.add(Explanation.match(k1, "parameter k1"));
//        if (norms == null) {
//            subs.add(Explanation.match(0, "parameter b (norms omitted for field)"));
//            return Explanation.match(
//                    (freq.getValue() * (k1 + 1)) / (freq.getValue() + k1),
//                    "tfNorm, computed as (freq * (k1 + 1)) / (freq + k1) from:", subs);
//        } else {
//            float doclen = decodeNormValue((byte)norms.get(doc));
//            subs.add(Explanation.match(b, "parameter b"));
//            subs.add(Explanation.match(stats.avgdl, "avgFieldLength"));
//            subs.add(Explanation.match(doclen, "fieldLength"));
//            return Explanation.match(
//                    (freq.getValue() * (k1 + 1)) / (freq.getValue() + k1 * (1 - b + b * doclen/stats.avgdl)),
//                    "tfNorm, computed as (freq * (k1 + 1)) / (freq + k1 * (1 - b + b * fieldLength / avgFieldLength)) from:", subs);
//        }
//    }
//
//    private Explanation explainScore(int doc, Explanation freq, org.apache.lucene.search.similarities.BM25Similarity.BM25Stats stats, NumericDocValues norms) {
//        Explanation boostExpl = Explanation.match(stats.boost, "boost");
//        List<Explanation> subs = new ArrayList<>();
//        if (boostExpl.getValue() != 1.0f)
//            subs.add(boostExpl);
//        subs.add(stats.idf);
//        Explanation tfNormExpl = explainTFNorm(doc, freq, stats, norms);
//        subs.add(tfNormExpl);
//        return Explanation.match(
//                boostExpl.getValue() * stats.idf.getValue() * tfNormExpl.getValue(),
//                "score(doc="+doc+",freq="+freq+"), product of:", subs);
//    }
//
//    @Override
//    public String toString() {
//        return "BM25(k1=" + k1 + ",b=" + b + ")";
//    }
//
//    /**
//     * Returns the <code>k1</code> parameter
//     * @see #BM25Similarity(float, float)
//     */
//    public final float getK1() {
//        return k1;
//    }
//
//    /**
//     * Returns the <code>b</code> parameter
//     * @see #BM25Similarity(float, float)
//     */
//    public final float getB() {
//        return b;
//    }
//}
