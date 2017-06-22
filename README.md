NP Chunker v1.1
Copyright (C) 2003-2005 The University of Sheffield

=======================================================================
COPYRIGHT AND WARRANTY INFORMATION
=======================================================================

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU Lesser General Public License as
published by the Free Software Foundation; either version 2.1 of the
License, or (at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU Lesser General Public
License along with this program; if not, write to the Free Software
Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.

=======================================================================
DESCRIPTION
=======================================================================

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

=======================================================================
DIFFERENCES FROM THE ORIGINAL
=======================================================================

The major difference is the assumption is made that if a POS tag is
is not in the mapping file then it is tagged as 'I'. The original
version simply failed if an unknown POS tag was encountered.
When using the GATE wrapper the unknown chunk tag can be changed from
'I' to any other legal tag (B or O).

=======================================================================
OBTAINING THE SOFTWARE
=======================================================================

This version of the chunker is distributed as part of GATE, in the
NP_Chunking plugin.

=======================================================================
USING THE CHUNKER WITHIN GATE
=======================================================================

Using the GATE wrapper should be really simple.  When you run the
GATE GUI register this directory (use the '-d URL' command line switch
or the plugin management console) and then simply add the "Noun Phrase
Chunker" to you pipeline. The two mandatory options are simply urls
pointing at the required resource files.  The default parameter values
should work in 99.9% of cases. For anybody who has used GATE before the rest
should be straightforward.

To use the chunker once created it has to be part of a pipeline in which
the document has already been tokenized, sentence split and had a POS tag
associated with each token.
