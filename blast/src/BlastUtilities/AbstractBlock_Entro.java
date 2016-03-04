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

import BlockProcessing.ComparisonRefinement.AbstractDuplicatePropagation;
import DataStructures.AbstractBlock;
import DataStructures.Comparison;
import Utilities.ComparisonIterator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author G.A.P. II
 */

public abstract class AbstractBlock_Entro extends AbstractBlock {
    protected double entropy;

    public AbstractBlock_Entro() {
        super();
    }

    public int getBlockIndex() {
        return blockIndex;
    }

    public AbstractBlock_Entro(double e) {
        super();
        entropy = e;
    }

    public double getEntropy() {
        return entropy;
    }
}