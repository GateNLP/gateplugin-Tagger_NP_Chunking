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

import gate.creole.PackagedController;
import gate.creole.metadata.AutoInstance;
import gate.creole.metadata.AutoInstanceParam;
import gate.creole.metadata.CreoleResource;

@CreoleResource(name = "Noun Phrase Chunker", icon = "NpChunker", comment = "Ready-made NP chunking application", autoinstances = @AutoInstance(parameters = {
		@AutoInstanceParam(name = "pipelineURL", value = "resources/np-chunker.xgapp"),
		@AutoInstanceParam(name = "menu", value = "NP Chunking") }))
public class ChunkingApp extends PackagedController {
	private static final long serialVersionUID = -825603646343914432L;
}
