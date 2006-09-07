######################################################################
################## Statistics similar to MappFinder ##################
######################################################################

## Calculates the z-score for the given pathway where
## geneProduct contains all measured genes, and pass is
## specifies for every geneProduct if it belong to the
## subset to test against or not
zscore = function(pathway, geneProducts, subset) {
	N = length(geneProducts)						## Total number of genes measured
	R = sum(subset)							## Total number of genes belonging to the set
	n = sum(hasGeneProduct(pathway, geneProducts))	## Total number of genes in the pathway
	r = sum(hasGeneProduct(pathway, geneProducts[as.logical(subset)]))## Number of genes that are in the subset and on the pathway
	print(paste(N,R,n,r,sep=","))
	(r-n*R/N) / sqrt(n*(R/N)*(1-R/N)*(1-(n-1)/(N-1)))
}


