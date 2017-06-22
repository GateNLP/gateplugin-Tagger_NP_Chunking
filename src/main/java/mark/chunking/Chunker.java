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

import gate.util.BomStrippingInputStreamReader;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Chunker implements Serializable {

	private static final long serialVersionUID = 9147365383638459068L;

	private List<Rule> rules = new ArrayList<Rule>();

	/**
	 * The only constructor that reads the rules from a URL.
	 * 
	 * @param u
	 *            the URL of the rules file.
	 **/
	public Chunker(URL u) throws IOException {
		// Open up the rules file read for reading
		try (BufferedReader in = new BomStrippingInputStreamReader(
				u.openStream())) {

			// read in the first rule from the file
			String rule = in.readLine();

			while (rule != null) {
				// while there are still rules to process...

				if (!rule.trim().equals("")) {
					// create and add a rule to the list of rules
					rules.add(new Rule(rule));
				}

				// read in the next rule;
				rule = in.readLine();
			}
		}
	}

	/**
	 * This is the method which does all the work and returns an updated set of
	 * chunk tags.
	 * 
	 * @param words
	 *            an ordered List of the words within the sentence.
	 * @param tags
	 *            an ordered List of the chunk tags within the sentence.
	 * @param pos
	 *            an ordered List of the POS tags within the sentence.
	 * @return an ordered List of the updated chunk tags for the sentence.
	 **/
	public List<String> chunkSentence(List<String> words, List<String> tags,
			List<String> pos) {
		// add the word/pos/tag that represents the end of
		// the sentence, cos some of the rules match against
		// the end of the sentence
		words.add("ZZZ");
		pos.add("ZZZ");
		tags.add("Z");

		// Get an iterator over the rules and loop
		// through them...
		Iterator<Rule> it = rules.iterator();
		while (it.hasNext()) {
			// create an empty list to hold the new
			// chunk tags for this iterations
			List<String> newTags = new ArrayList<String>();

			// get the next rule we are going to apply
			Rule r = it.next();

			// loop over all the words in the sentence
			for (int i = 0; i < words.size(); ++i) {
				if (r.match(i, words, tags, pos)) {
					// if the rule matches against the current
					// word in the sentence then and the new tag
					// from the rule to the new tag list
					newTags.add(r.getNewTag());
				} else {
					// the rule didn't match so simply copy the
					// chunk tag that was already assigned
					newTags.add(tags.get(i));
				}
			}

			// now replace the old tags with the new ones ready
			// for running the next rule, this stops rule-chaining
			tags = newTags;
		}

		// remove the last token from each list as these
		// are not part of the original input sentence
		words.remove(words.size() - 1);
		pos.remove(pos.size() - 1);
		tags.remove(tags.size() - 1);

		// return the final updated chunk tag lists
		return tags;
	}
}