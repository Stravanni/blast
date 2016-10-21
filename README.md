![](https://photos-2.dropbox.com/t/2/AADuC-bicWowMCvs1JzwYsmqg_2EqyPjLTCfgLgTH3OAPw/12/58906809/png/32x32/1/_/1/2/blast_logo.png/EJOfxC0Y2YghIAcoBw/Cm4pd260YU4h1jT5w4hVIyMDv09ZVL3J3Z9IxQTdvjc?size=1280x960&size_mode=3)

- Doing entity resolution on *entity resolution* you can find: *record linkage, duplicates detection, entity matching, deduplication, fuzzy matching*.

- Doing entity resolution on *records* you can find: *tuples, entity profiles, semi-structured documents*.

## What does Blast do?
It helps to scale Entity Resolution: it efficiently extracts *loose* schema information, and uses this information to group together records that most likely will match.

Basically, instead of comparing all possible paris of records, you only compare subsets of them.

P.S.: Blast employs only **unsupervised** techniques.

## When to use Blast?
When you have semi-structured data to clean, but you cannot do schema-matching to apply traditional blocking techniques.

## Current Project Version
Here the code of "BLAST: a loosely schema-aware meta-blocking approach for entity resolution", please cite: [[1]](#simonini2016).

The approach is implemented on top of the *Blocking Framework*, A framework for blocking-based Entity Resolution [[2]](#papadakis2013).

## Where to start
Take a look to Experiments -> Test_metablocking.java

## References
<a name="simonini2016"/>
[1] Simonini, Giovanni, Sonia Bergamaschi, and H. V. Jagadish. "BLAST: a loosely schema-aware meta-blocking approach for entity resolution." Proceedings of the VLDB Endowment 9.12 (2016): 1173-1184.
</a>

<a name="papadakis2013"/>
[2] <a href="https://sourceforge.net/projects/erframework/"> sourceforge.net/projects/erframework/ </a>
</a>