/************************************************************************
 *         Copyright (C) 2004-2016 The University of Sheffield          *
 *       Developed by Mark Greenwood <m.greenwood@dcs.shef.ac.uk>       *
 *                                                                      *
 * This program is free software; you can redistribute it and/or modify *
 * it under the terms of the GNU Lesser General Public License as       *
 * published by the Free Software Foundation; either version 2.1 of the *
 * License, or (at your option) any later version.                      *
 *                                                                      *
 * This program is distributed in the hope that it will be useful,      *
 * but WITHOUT ANY WARRANTY; without even the implied warranty of       *
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the        *
 * GNU General Public License for more details.                         *
 *                                                                      *
 * You should have received a copy of the GNU Lesser General Public     *
 * License along with this program; if not, write to the Free Software  *
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.            *
 ************************************************************************/

package mark.chunking;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import gate.Annotation;
import gate.AnnotationSet;
import gate.Factory;
import gate.FeatureMap;
import gate.Resource;
import gate.creole.AbstractLanguageAnalyser;
import gate.creole.ExecutionException;
import gate.creole.ResourceInstantiationException;
import gate.creole.ResourceReference;
import gate.creole.metadata.CreoleParameter;
import gate.creole.metadata.CreoleResource;
import gate.creole.metadata.Optional;
import gate.creole.metadata.RunTime;
import gate.util.BomStrippingInputStreamReader;
import gate.util.GateRuntimeException;
import gate.util.OffsetComparator;

@CreoleResource(name = "Noun Phrase Chunker", comment = "Implementation of the Ramshaw and Marcus base noun phrase chunker", helpURL = "http://gate.ac.uk/userguide/sec:parsers:npchunker", icon = "NpChunker")
public class GATEWrapper extends AbstractLanguageAnalyser {

	private static final long serialVersionUID = -801244032207014722L;

	private Chunker c = null;

	private Map<String, String> chunkTags = null;

	private ResourceReference posTagURL;

	@CreoleParameter(defaultValue = "resources/pos_tag_dict", comment = "The URL of the pos_tag_dict file.")
	public void setPosTagURL(ResourceReference posTagURL) {
		this.posTagURL = posTagURL;
	}
	
	@Deprecated
	public void setPosTagURL(URL posTagURL) {
	  try {
      this.setPosTagURL(new ResourceReference(posTagURL));
    } catch(URISyntaxException e) {
      throw new RuntimeException("Error converting URL to ResourceReference",e);
    }
  }

	public ResourceReference getPosTagURL() {
		return posTagURL;
	}

	private ResourceReference rulesURL;

	@CreoleParameter(defaultValue = "resources/rules", comment = "The URL of the rules file.")
	public void setRulesURL(ResourceReference rulesURL) {
		this.rulesURL = rulesURL;
	}
	
	@Deprecated
	public void setRulesURL(URL rulesURL) {
	  try {
      this.setRulesURL(new ResourceReference(rulesURL));
    } catch(URISyntaxException e) {
      throw new RuntimeException("Error converting URL to ResourceReference",e);
    }
  }

	public ResourceReference getRulesURL() {
		return rulesURL;
	}

	private String posFeature;

	@RunTime
	@CreoleParameter(defaultValue = "category", comment = "The name of the feature which holds the POS tag.")
	public void setPosFeature(String posFeature) {
		this.posFeature = posFeature;
	}

	public String getPosFeature() {
		return posFeature;
	}

	private String unknownTag;

	@RunTime
	@CreoleParameter(defaultValue = "I", comment = "The chunk tag to use for an unknown POS tag.")
	public void setUnknownTag(String unknownTag) {
		this.unknownTag = unknownTag;
	}

	public String getUnknownTag() {
		return unknownTag;
	}

	private String inputASName;

	@Optional
	@RunTime
	@CreoleParameter(comment = "The name of the annotation set used for input.")
	public void setInputASName(String inputASName) {
		this.inputASName = inputASName;
	}

	public String getInputASName() {
		return inputASName;
	}

	private String outputASName;

	@Optional
	@RunTime
	@CreoleParameter(comment = "The name of the annotation set used for output.")
	public void setOutputASName(String outputASName) {
		this.outputASName = outputASName;
	}

	public String getOutputASName() {
		return outputASName;
	}

	private String annotationName;

	@RunTime
	@CreoleParameter(defaultValue = "NounChunk", comment = "The name of the annotations added to mark noun chunks.")
	public void setAnnotationName(String annotationName) {
		this.annotationName = annotationName;
	}

	public String getAnnotationName() {
		return annotationName;
	}

	@RunTime
	@Optional
	@CreoleParameter(comment = "Throw an exception when there are none of the required input annotations", defaultValue = "true")
	public void setFailOnMissingInputAnnotations(Boolean fail) {
		failOnMissingInputAnnotations = fail;
	}

	public Boolean getFailOnMissingInputAnnotations() {
		return failOnMissingInputAnnotations;
	}

	protected boolean failOnMissingInputAnnotations = true;

	public Resource init() throws ResourceInstantiationException {
		if (rulesURL == null) {
			throw new ResourceInstantiationException(
					"Rules URL must be specified");
		}

		if (posTagURL == null) {
			throw new ResourceInstantiationException(
					"POS tag dictionary URL must be specified");
		}

		try (BufferedReader in = new BomStrippingInputStreamReader(
				posTagURL.openStream())) {
			// lets create a new Chunker using the URL provided (which we know
			// is not null as we already checked it).
			c = new Chunker(rulesURL.toURL());

			// read in the first line of the file
			String line = in.readLine();

			// create a new empty map to hold the pos and chunk tags
			chunkTags = new HashMap<String, String>();

			while (line != null) {
				// while there is still data in the file...

				// split the current line into two parts
				String[] tags = line.split(" ");

				// put the data in the map, POS tags as key
				// chunk tag as value
				chunkTags.put(tags[0], tags[1]);

				// get the next line from the data file
				line = in.readLine();
			}
		} catch (IOException e) {
			// if an error occurred then throw an exception so that the user
			// knows
			throw new ResourceInstantiationException(
					"Unable to correctly init the chunker: " + e.getMessage());
		}

		// if we get to here then everything has initialised correctly
		// so return this instance
		return this;
	}

	public void execute() throws ExecutionException {
		// lets get the AnnotationSet we are using as input. Get either the
		// set the user has asked for or if they haven't specified use the
		// default set
		if (inputASName != null && inputASName.equals(""))
			inputASName = null;
		AnnotationSet inputAS = (inputASName == null) ? document
				.getAnnotations() : document.getAnnotations(inputASName);

		// lets get the AnnotationSet we are using as output. Get either the
		// set the user has asked for or if they haven't specified use the
		// default set
		if (outputASName != null && outputASName.equals(""))
			outputASName = null;
		AnnotationSet outputAS = (outputASName == null) ? document
				.getAnnotations() : document.getAnnotations(outputASName);

		// Get the set of sentences contained within the current document
		AnnotationSet sentences = inputAS.get(SENTENCE_ANNOTATION_TYPE);

		// All annotations of type tokens
		AnnotationSet tokenas = inputAS.get(TOKEN_ANNOTATION_TYPE);

		if (sentences != null && sentences.size() > 0) {
			// assuming there are sentences...

			// get the current time to use as part of the progress feedback
			long startTime = System.currentTimeMillis();

			// tell the user we are just starting to chunk the document
			fireStatusChanged("Chunking " + document.getName());
			fireProgressChanged(0);

			// we are just starting so we haven't processed a document yet
			// so remember this ready for the progress feedback
			int i = 0;

			// Loop through all the sentences
			Iterator<Annotation> sit = sentences.iterator();
			while (sit.hasNext()) {
				// get the current sentence to process
				Annotation sentence = sit.next();

				// Get a sorted list of the tokens within the current sentence
				List<Annotation> tokens = new ArrayList<Annotation>();
				tokens.addAll(tokenas.getContained(sentence.getStartNode()
						.getOffset(), sentence.getEndNode().getOffset()));
				Collections.sort(tokens, new OffsetComparator());

				// Create three empty lists to hold the words, pos and chunk
				// tags of the tokens in the current sentence
				List<String> wl = new ArrayList<String>();
				List<String> tl = new ArrayList<String>();
				List<String> pl = new ArrayList<String>();

				// Loop through all the tokens in the current sentence
				Iterator<Annotation> tit = tokens.iterator();
				while (tit.hasNext()) {
					// get the current token to process
					Annotation token = tit.next();

					// add the string spanned by the current token to the list
					// of
					// words
					wl.add((String) token.getFeatures().get("string"));

					// get the POS tag for the current token
					String pos = (String) token.getFeatures().get(posFeature);

					// add the POS tag to the list of POS tags
					pl.add(pos);

					// get the initial chunk tag for this POS tag
					String chunkTag = chunkTags.get(pos);

					// if the chunk tag is null then use the unknown chunk tag
					if (chunkTag == null)
						chunkTag = unknownTag;

					// now add the chunk tag to the list of chunk tags
					tl.add(chunkTag);
				}

				// run the chunker over the current sentence and get back
				// an updated list of chunk tags
				tl = c.chunkSentence(wl, tl, pl);

				// a variable to hold the index of the token which
				// starts the current noun chunk
				int start = 0;

				// a flag so we know if we are in an NP or not
				boolean inBaseNP = false;

				// Loop through all the chunk tags in the current sentence
				// so we can find the noun chunks
				for (int tIndex = 0; tIndex < tl.size(); ++tIndex) {
					// get the current chunk tag
					String ct = tl.get(tIndex);

					if (inBaseNP) {
						// if we are currently inside a noun chunk then...

						if (ct.equals("B")) {
							// if the chunk tag is "B" then we are about to
							// start a
							// new chunk so record the one that has just
							// finished
							addAnnotation(outputAS, tokens, start, tIndex - 1);

							// now reset the beginning of the chunk to the
							// current
							// token
							start = tIndex;
						} else if (ct.equals("O")) {
							// if the chunk tag is "O" then we have dropped out
							// the end of a chunk so add the chunk we just
							// finished
							addAnnotation(outputAS, tokens, start, tIndex - 1);

							// now flag that we are outside of any chunk
							inBaseNP = false;
						}
					} else {
						// we aren't currently in a noun chunk so...

						if (ct.equals("B") || ct.equals("I")) {
							// if the chunk tag is "B" or "I" then we have found
							// the beginning of a chunk, so....

							// record the start index
							start = tIndex;

							// and flag that we are now inside a chunk
							inBaseNP = true;
						}
					}
				}

				if (inBaseNP) {
					// if we got to the end of a sentence and we are still in a
					// noun chunk then we need to close the end and add the
					// annotation
					addAnnotation(outputAS, tokens, start, tl.size() - 1);
				}

				// update the progress stuff to show the precentage of sentences
				// we have processed so far
				fireProgressChanged(i++ * 100 / sentences.size());
			}

			// we have finished! so update the progress and tell
			// the user how long it took to chunk the document
			fireProcessFinished();
			fireStatusChanged(document.getName()
					+ " chunked in "
					+ NumberFormat
							.getInstance()
							.format((double) (System.currentTimeMillis() - startTime) / 1000)
					+ " seconds!");
		} else if (failOnMissingInputAnnotations) {
			// if there are no sentence annotations then throw an exception as
			// theres
			// not much we can do
			throw new GateRuntimeException(
					"No sentences to process! Please run a sentence splitter first!");
		}
	}

	private void addAnnotation(AnnotationSet outputAS, List<Annotation> tokens,
			int start, int end) {
		// Create a new FeatureMap to act as the features for the new
		// annotation
		// but we will leave it blank for now as we don't have anything to
		// add
		FeatureMap params = Factory.newFeatureMap();

		// Get the token annotation from the beginning of the chunk
		Annotation aStart = tokens.get(start);

		// Get the token annotation from the end of the chunk
		Annotation aEnd = tokens.get(end);

		// This spots errors where the start is after the end. What
		// we should do is figure out why this occurs in the first place
		if (aStart.getStartNode().getOffset().longValue() >= aEnd.getEndNode()
				.getOffset().longValue())
			return;

		// add a new annotation to mark the noun chunk
		outputAS.add(aStart.getStartNode(), aEnd.getEndNode(), annotationName,
				params);
	}
}
