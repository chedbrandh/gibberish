package com.chedbrandh.gibberish.dataloading;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;

public class SchemaTest {

    private static final String SCHEMA_RESOURCE_PATH = "schema_all_variations.yml";

    @Test
    public void testVerifySemantics() throws Exception {
        getTestSchema().verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailMissingFiles() throws Exception {
        Schema schema = getTestSchema();
        schema.files = null;
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailEmptyFiles() throws Exception {
        Schema schema = getTestSchema();
        schema.files = ImmutableMap.of();
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailMissingProviders() throws Exception {
        Schema schema = getTestSchema();
        schema.providers = null;
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailEmptyProviders() throws Exception {
        Schema schema = getTestSchema();
        schema.providers = ImmutableMap.of();
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailMissingTranslators() throws Exception {
        Schema schema = getTestSchema();
        schema.translators = null;
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailEmptyTranslators() throws Exception {
        Schema schema = getTestSchema();
        schema.translators = ImmutableMap.of();
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailMissingFilePath() throws Exception {
        Schema schema = getTestSchema();
        schema.files.values().iterator().next().path = null;
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailMissingProviderFiles() throws Exception {
        Schema schema = getTestSchema();
        schema.providers.values().iterator().next().files = null;
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailEmtpyProviderFiles() throws Exception {
        Schema schema = getTestSchema();
        schema.providers.values().iterator().next().files = ImmutableList.of();
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailMissingTranslatorFormat() throws Exception {
        Schema schema = getTestSchema();
        schema.translators.values().iterator().next().format = null;
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailEmptyTranslatorFormat() throws Exception {
        Schema schema = getTestSchema();
        schema.translators.values().iterator().next().format = ImmutableList.of();
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailMissingTranslatorProviders() throws Exception {
        Schema schema = getTestSchema();
        schema.translators.values().iterator().next().providers = null;
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailEmptyTranslatorProviders() throws Exception {
        Schema schema = getTestSchema();
        schema.translators.values().iterator().next().providers = ImmutableList.of();
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailFormatProviderSizeMismatch() throws Exception {
        Schema schema = getTestSchema();
        Schema.Translator translator = schema.translators.values().iterator().next();
        translator.format =
                ImmutableList.<String>builder().addAll(translator.format).add("apa").build();
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailProviderBitDistributionSizeMismatch() throws Exception {
        Schema schema = getTestSchema();
        Schema.Translator translator = schema.translators.values().iterator().next();
        translator.bit_distribution =
                ImmutableList.<Integer>builder().addAll(translator.bit_distribution).add(1).build();
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailMissingTranslatorBitDistribution() throws Exception {
        Schema schema = getTestSchema();
        Schema.Translator translator = schema.translators.values().iterator().next();
        translator.number_of_bits = 0;
        translator.bit_distribution = null;
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailEmptyTranslatorBitDistribution() throws Exception {
        Schema schema = getTestSchema();
        Schema.Translator translator = schema.translators.values().iterator().next();
        translator.number_of_bits = 0;
        translator.bit_distribution = ImmutableList.of();
        schema.verifySemantics();
    }

    @Test
    public void testVerifySemanticsFailTranslatorSumBitDistributionEqualNumBits()
            throws Exception {
        Schema schema = getTestSchema();
        Schema.Translator translator = schema.translators.values().iterator().next();
        translator.number_of_bits = sum(translator.bit_distribution);
        schema.verifySemantics();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testVerifySemanticsFailEmptyTranslatorSumBitDistributionNotEqualNumBits()
            throws Exception {
        Schema schema = getTestSchema();
        Schema.Translator translator = schema.translators.values().iterator().next();
        translator.number_of_bits = sum(translator.bit_distribution) + 1;
        schema.verifySemantics();
    }

    private static Schema getTestSchema() {
        InputStream inputStream = ClassLoader.getSystemResourceAsStream(SCHEMA_RESOURCE_PATH);
        return new Yaml().loadAs(inputStream, Schema.class);
    }

    private static int sum(List<Integer> integerList) {
        int result = 0;
        for (int i : integerList) {
            result += i;
        }
        return result;
    }
}
