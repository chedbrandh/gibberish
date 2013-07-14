package com.chedbrandh.gibberish.example;

import com.chedbrandh.gibberish.IndexTranslator;
import com.chedbrandh.gibberish.PhraseTranslator;
import com.chedbrandh.gibberish.WordProviderSequence;
import com.chedbrandh.gibberish.dataloading.SchemaReader;
import com.chedbrandh.gibberish.exceptions.IllegalPhraseException;
import com.chedbrandh.gibberish.exceptions.IllegalWordException;
import com.chedbrandh.gibberish.exceptions.WordIndexOutOfBoundsException;
import com.google.common.collect.ImmutableList;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Random;

/**
 * Comprehensive example main class for demonstrating most functionality that
 * the library provides.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class ComprehensiveExampleMain {

    public static void main(String[] args) throws Exception {

        // read schema file
        InputStream schemaInputStream =
                ClassLoader.getSystemResourceAsStream("examples/schema_examples.yml");

        // create schema reader
        SchemaReader schemaReader = new SchemaReader(schemaInputStream);

        // get translator from schema
        PhraseTranslator translator = schemaReader.getTranslators().get("animal_sentence_short");

        // get translator WordProviderSequence used for translating
        WordProviderSequence wordProviderSequence = translator.getWordProviderSequence();

        // print the first word of each WordProvider in the WordProviderSequence
        System.out.println("WordProviderSequence first words: " +
                wordProviderSequence.getWords(ImmutableList.of(0, 0, 0)));

        // print the indices of some word in the WordProviderSequence
        System.out.println("Some WordProviderSequence indices: " +
                wordProviderSequence.getIndices(ImmutableList.of("27", "herrings", "Sweden")));

        // print the WordProviderSequence bit coverage. this is the
        // number of bits that could be used for translation given
        // the number of words loaded and the phrase format.
        System.out.println("WordProviderSequence bit coverage: " +
                wordProviderSequence.bitCoverage());

        // print the checksum of the WordProviderSequence. If the word files
        // are modified this checksum will change, and so will translations.
        System.out.println("WordProviderSequence checksum: " +
                wordProviderSequence.computeChecksum());

        // get translator IndexTranslator used for translating
        IndexTranslator indexTranslator = translator.getIndexTranslator();

        // print the IndexTranslator bit coverage. this is the the number
        // of bits that are used for translation as specified by the schema.
        int numBits = indexTranslator.bitCoverage();
        System.out.println("IndexTranslator bit coverage: " + numBits);

        // print the IndexTranslator bit distribution. this is the list
        // of the number of bits that each WordProvider in the
        // WordProviderSequence was assigned by the optimizer.
        System.out.println("IndexTranslator bit distribution: " +
                indexTranslator.bitDistribution());

        // get some random bytes
        byte[] randomBytes = new byte[10];
        new Random().nextBytes(randomBytes);

        // translate bytes into a phrase and print
        String phrase = translator.fromBytes(randomBytes, 0, numBits);
        System.out.println("Generated random phrase: " + phrase);

        // translate the phrase back into bytes
        byte[] translatedBytes = new byte[10];
        translator.toBytes(translatedBytes, phrase, 0, numBits);

        // only the first translator.numBits() number of bits in the array of
        // random bytes were used to translate into a phrase, so when translated
        // back to bytes only these bits can be expected to compare equal.
        System.out.println("The first " + numBits + " bits in the " +
                "original byte array and the translated byte array should be equal.");
        System.out.println("Original bytes array: " + Arrays.toString(randomBytes));
        System.out.println("Translated bytes array: " + Arrays.toString(translatedBytes));

        // the byte arrays may not be identical but translating the new byte
        // array back to a phrase should return an identical phrase.
        String retranslatedPhrase = translator.fromBytes(translatedBytes, 0, numBits);
        System.out.println("Original and re-translated phrase are identical: " +
                phrase.equals(retranslatedPhrase));

        // translating a phrase on the wrong format or with unknown words will throw an exception

        // this legal phrase translates just fine
        translator.toLong("5 deer live in Iraq.");

        // this phrase does not have the expected " live in " separator
        try {
            translator.toLong("5 deer work in Iraq.");
        } catch(IllegalPhraseException e) {
            System.out.println(e.getMessage());
        }

        // this phrase uses the animal word "salamanders" which is at an index greater
        // than what the number of bits used for the "animals" word provider allows.
        try {
            translator.toLong("5 salamanders live in Iraq.");
        } catch(WordIndexOutOfBoundsException e) {
            System.out.println(e.getMessage());
        }

        // this phrase contains the unknown word "unicorn"
        try {
            translator.toLong("5 unicorn live in Iraq.");
        } catch(IllegalWordException e) {
            System.out.println(e.getMessage());
        }
    }
}
