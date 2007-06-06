package org.pathvisio.gpmldiff;

/**
   abstract base class for implementations of similarity functions
*/
abstract class SimilarityFunction
{
	abstract public int getSimScore (PwyElt e1, PwyElt e2);
}