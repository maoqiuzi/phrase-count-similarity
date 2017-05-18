package com.maoqiuzi.esplugins.pcsplugin;

import org.elasticsearch.index.IndexModule;
import org.elasticsearch.plugins.Plugin;

/**
 * Created by maoqiuzi on 5/15/17.
 */
public class PhraseCountSimilarityPlugin extends Plugin {

    @Override
    public void onIndexModule(IndexModule module) {
        module.addSimilarity("phrase-count-similarity", PhraseCountSimilarityProvider::new);
    }
}
