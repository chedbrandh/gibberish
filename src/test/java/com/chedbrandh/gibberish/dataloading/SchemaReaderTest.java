package com.chedbrandh.gibberish.dataloading;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.chedbrandh.gibberish.PhraseTranslator;
import com.chedbrandh.gibberish.exceptions.DuplicateWordException;
import org.junit.Test;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.ConstructorException;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;
import java.util.Random;
import java.util.regex.Pattern;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;


public class SchemaReaderTest {

    private static final String ALPHA_NUM_GROUP = "(\\p{Alnum}+)";
    private static final String SCHEMA_PATH = "src/test/resources/schema_all_variations.yml";
    private static final String WORD_FILE_DUPLICATES_PATH = "src/test/resources/word_file_duplicates.txt";

    @Test
    public void testGetTranslators() throws Exception {
        SchemaReader schemaReader = new SchemaReader(getTestSchema());
        assertEquals(3, schemaReader.getTranslators().size());
        assertTrue(schemaReader.getTranslators().containsKey("foo_bar_baz"));
        Pattern pattern = toPattern(ImmutableList.of("foo ", " bar ", " baz"));
        PhraseTranslator translator = schemaReader.getTranslators().get("foo_bar_baz");
        assertTrue(pattern.matcher(translator.fromBytes(randomBytes(), 0, 5)).matches());
    }

    @Test
    public void testConstructor() throws Exception {
        SchemaReader schemaReader = new SchemaReader(new FileInputStream(SCHEMA_PATH));
        assertEquals(3, schemaReader.getTranslators().size());
    }

    @Test(expected = ConstructorException.class)
    public void testConstructorFail() throws Exception {
        new SchemaReader(new ByteArrayInputStream("foo".getBytes()));
    }

    @Test(expected = DuplicateWordException.class)
    public void testWordFileParseException() throws Exception {
        Schema schema = getTestSchema();
        schema.files.values().iterator().next().path = WORD_FILE_DUPLICATES_PATH;
        new SchemaReader(schema);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testChecksumVerification() throws Exception {
        Schema schema = getTestSchema();
        schema.translators.get("checksum_translator").checksum = "foo";
        new SchemaReader(schema);
    }

    private static Schema getTestSchema() throws FileNotFoundException {
        InputStream inputStream = new FileInputStream(SCHEMA_PATH);
        return new Yaml().loadAs(inputStream, Schema.class);
    }

    private static byte[] randomBytes() {
        byte[] result = new byte[16];
        new Random().nextBytes(result);
        return result;
    }

    private static Pattern toPattern(List<String> format) {
        return Pattern.compile(Joiner.on(ALPHA_NUM_GROUP).join(format));
    }
}
