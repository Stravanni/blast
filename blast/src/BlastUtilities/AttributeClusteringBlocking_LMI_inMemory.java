/*
 *    This program is free software; you can redistribute it and/or modify
 *    it under the terms of the GNU General Public License as published by
 *    the Free Software Foundation; either version 2 of the License, or
 *    (at your option) any later version.
 *
 *    This program is distributed in the hope that it will be useful,
 *    but WITHOUT ANY WARRANTY; without even the implied warranty of
 *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *    GNU General Public License for more details.
 *
 *    Copyright (C) 2015 George Antony Papadakis (gpapadis@yahoo.gr)
 */

package BlastUtilities;

import Blast_LMI.AbstractAttributeClusteringBlocking_LMI;
import Utilities.RepresentationModel;
import com.google.common.hash.BloomFilter;

/**
 * @author gap2
 */
public class AttributeClusteringBlocking_LMI_inMemory extends AbstractAttributeClusteringBlocking_LMI {

    public AttributeClusteringBlocking_LMI_inMemory(BlastRepresentationModel md, String[] entities) {
        super(md, entities, null);
    }
    public AttributeClusteringBlocking_LMI_inMemory(BlastRepresentationModel md, String[] entities, BloomFilter pairBloom) {
        super(md, entities, null, pairBloom);
    }
    public AttributeClusteringBlocking_LMI_inMemory(BlastRepresentationModel md, String[] entities, String source) {
        super(md, entities, null, source);
    }

    @Override
    protected void setDirectory() {
        setMemoryDirectory();
    }
}
