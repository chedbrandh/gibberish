# Gibberish is a bit-sequence-to-phrase translator.

## Background

The idea is completely stolen from [Asana](http://blog.asana.com/2011/09/6-sad-squid-snuggle-softly/).

The Gibberish library translates sequences of bits to and from phrases.

E.g. ``[10100100011000000111101000100110] <=> “6 sad squid snuggle softly”``

Asana uses this to create their "unique error phrases" (see blog entry for
details), but it could also be used to [create passphrases](http://xkcd.com/936) or just random
sentences for fun.

## How Does It Work?

A bit sequence is divided up into smaller sequences.

E.g. `[0100011] => [010], [0011]`

These smaller bit sequences are interpreted as (little-endian) binary numbers.

E.g. `[010], [0011] => 2, 3`

The numbers are then used as indices in long lists of words.

E.g.

`{"happy", "old", "sad", "soft", "mad"}[2] => "sad"`  
`{"bats", "cows", "owls", "squid", "seals"}[3] => "squid"`

Finally the indexed words are combined.

`"sad" + "squid" => "sad squid"`

## Fancy Features

### Configurability
The Gibberish library is focused on making the setup process of these phrase 
translators as easy as possible. Creating a translator has therefore been made 
as easy as putting some words in one or more files, and defining a schema. 
Translators are created by reading this schema. See the
[Getting Started](#getting-started) section for more detail.

Note that the "words" in these data files don't actually have to be words in a
technical sense. The files are allowed to contain any characters including
non-alphanumeric characters. They are read line by line meaning that all
characters on any given line will be treated as a "word".

### Optimized bit distribution
It could be tricky to determine how many bits from the sequence each word list
is assigned. E.g. if a word list with only 6 words are assigned 3 bits, an
index `[111] => 7` is not on that list. An optimizer can therefore be used to
optimize bit distribution for the minimum total mean word length. The
algorithm used is a form of steepest descent with equality constraints. (The 
equality constraint comes from the requirement that the total number of bits 
is fixed)

### Checksum check
In order to ensure that translation from bits to phrase (and vice versa) will
never change, the files with words must never change. An optional checksum for
a translator can therefore be provided to ensure that word lists have not been
tampered with.

## <a name="getting-started"></a>Getting Started

To try it out, simply load the example schema, get a translator, and give it
some random input.

```java
// read schema file
InputStream schemaInputStream =
    ClassLoader.getSystemResourceAsStream("examples/schema_examples.yml");
// create schema reader
SchemaReader schemaReader =
    new SchemaReader(schemaInputStream);
// get translator from schema
PhraseTranslator translator =
    schemaReader.getTranslators().get("animal_sentence");
// print a random phrase
System.out.println(translator.fromLong(Math.abs(new Random().nextLong())));
```

For more example code, check out the `com.chedbrandh.gibberish.example`
package.

## Pro Tip

When creating your own schema, use `PhraseTranslator.totalWordProviderBitCoverage()`
to find out how many bits can be used for some translator.
