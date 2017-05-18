package com.maoqiuzi.esplugins.pcsplugin;

import org.apache.lucene.search.similarities.Similarity;
import org.elasticsearch.common.inject.Inject;
import org.elasticsearch.common.inject.assistedinject.Assisted;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.similarity.AbstractSimilarityProvider;

/**
 * Created by maoqiuzi on 5/15/17.
 */
public class PhraseCountSimilarityProvider extends AbstractSimilarityProvider {

    private PhraseCountSimilarity similarity;

    @Inject
    public PhraseCountSimilarityProvider(@Assisted String name, @Assisted Settings settings) {
        super(name);
        this.similarity = new PhraseCountSimilarity();
    }

    public Similarity get() {
        return similarity;
    }
}
