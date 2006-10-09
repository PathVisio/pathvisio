######################################################################
################## Statistics similar to MappFinder ##################
######################################################################

## Calculates the z-score for the given pathway where
## 'reporters' contains all measured GeneProducts, and 'set'
## specifies for every geneProduct whether it belongs to the
## set to test against or not (e.g. subset that passed a criterion)
zscore = function(pathway, reporters, set) {
	N = length(reporters)			## Total number of genes measured
	R = sum(as.logical(set))		## Total number of genes belonging to the set
	
	matchPws = sapply(reporters, function(x) match(x, pathway))
	
	n = sum(matchPws)			## Total number of genes in the pathway
	r = sum(matchPws[as.logical(set)])	## Number of genes that are in the subset and on the pathway
	
	cat(paste(N,R,n,r,sep=","))
	(r-n*R/N) / sqrt(n*(R/N)*(1-R/N)*(1-(n-1)/(N-1)))
}
