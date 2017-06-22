# NP Chunker
Copyright (C) 2003-2017 The University of Sheffield

## DESCRIPTION
The application is a Java implementation of a the Ramshaw and Marcaus
BaseNP chunker (in fact the files in the resources directory are
taken straight from their original distribution) which attempts to
insert brackets marking noun phrases in text which has been marked
with POS tags in the same format as the output of Eric Brill's
transformational tagger. The output from this version should be
identical to the output of the oringinal C++/Perl version released
by Ramshaw and Marcus.

A wrapper is also included which allows the easy use of this chunker
within the GATE framework (http://gate.ac.uk).

For more information about baseNP structures and the use of
tranformation-based learning to derive them, see "Text Chunking
Using Transformation-Based Learning", Lance Ramshaw & Mitchell Marcus,
Proceedings of the Third ACL Workshop on Very Large Corpora, MIT,
June, 1995 (http://xxx.lanl.gov/e-print/cmp-lg/9505040)

## DIFFERENCES FROM THE ORIGINAL
The major difference is the assumption is made that if a POS tag is
is not in the mapping file then it is tagged as 'I'. The original
version simply failed if an unknown POS tag was encountered.
When using the GATE wrapper the unknown chunk tag can be changed from
'I' to any other legal tag (B or O).
