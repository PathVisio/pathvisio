######################################################################
################## Statistics similar to MappFinder ##################
######################################################################

## Calculates the z-score for the given pathway where
## geneProduct contains all measured genes, and pass is
## specifies for every geneProduct if it belong to the
## set to test against or not
zscore = function(pathway, geneProducts, set) {
	N = length(geneProducts)		## Total number of genes measured
	R = sum(as.logical(set))							## Total number of genes belonging to the set
	
	matchFun = function(x, pw) { hasGeneProduct(pw, x) }
	matchPws = sapply(geneProducts, matchFun(pathway = pw))
	
	n = sum(matchPws)	## Total number of genes in the pathway
	r = sum(matchPws[as.logical(set)])## Number of genes that are in the subset and on the pathway
	
	print(paste(N,R,n,r,sep=","))
	(r-n*R/N) / sqrt(n*(R/N)*(1-R/N)*(1-(n-1)/(N-1)))
}

pathway = pathways(myPathways)[[1]];
geneProducts = getGeneProducts(myDataSet);

set = getSetlist(myDataSet, ......);


