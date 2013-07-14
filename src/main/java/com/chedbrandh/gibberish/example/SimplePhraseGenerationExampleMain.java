package com.chedbrandh.gibberish.example;

import com.chedbrandh.gibberish.PhraseTranslator;
import com.chedbrandh.gibberish.dataloading.SchemaReader;

import java.io.InputStream;
import java.util.Random;


/**
 * Example main class for demonstrating schema and translator basics, and how
 * to generate some random phrases.
 *
 * @author Christofer Hedbrandh (chedbrandh@gmail.com)
 * @since 1.0
 */
public class SimplePhraseGenerationExampleMain {

    private static final Random RANDOM = new Random();

    public static void main(String[] args) throws Exception {

        // read schema file
        InputStream schemaInputStream =
                ClassLoader.getSystemResourceAsStream("examples/schema_examples.yml");

        // create schema reader
        SchemaReader schemaReader = new SchemaReader(schemaInputStream);

        // get translator from schema
        PhraseTranslator translator = schemaReader.getTranslators().get("animal_sentence");

        // print some random phrases
        for (int i = 0; i < 10; i++) {
            System.out.println(translator.fromLong(Math.abs(RANDOM.nextLong())));
        }

        // get translator for long phrases
        PhraseTranslator translatorLong =
                schemaReader.getTranslators().get("animal_sentence_long");

        // get number of bits used for translating long phrases
        int bitCoverage = translatorLong.getIndexTranslator().bitCoverage();

        // create byte array long enough to provide bits needed for translation
        byte[] bytes = new byte[bitCoverage / Byte.SIZE + 1];

        // print some random long phrases
        for (int i = 0; i < 10; i++) {
            RANDOM.nextBytes(bytes);
            System.out.println(translatorLong.fromBytes(bytes, 0, bitCoverage));
        }
    }
}
