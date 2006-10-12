######################################################################
################## Statistics similar to MappFinder ##################
######################################################################
require(GmmlR)

zscore = function(pathwaySet, dataSet, set) {
	if(class(pathwaySet) != "PathwaySet") stop("pathwaySet argument is not of class 'PathwaySet'")
	if(class(dataSet) != "DataSet") stop("dataSet argument is not of class 'DataSet'")
	if(length(set) != nrow(dataSet)) stop("set argument should be a logical vector with same length as number of reporters in dataset")

	reporters = reporters(dataSet)
	
	sapply(pathwaySet, function(x) .calcZscore(x, reporters, set))
}

## Calculates the z-score for the given pathway where
## 'reporters' contains all measured GeneProducts, and 'set'
## specifies for every geneProduct whether it belongs to the
## set to test against or not (e.g. subset that passed a criterion)
.calcZscore = function(pathway, reporters, set) {
	cat("Processing pathway '", name(pathway), "'\n", sep="");
	N = length(reporters)			## Total number of genes measured
	R = sum(as.logical(set))		## Total number of genes belonging to the set
	
	if(R == N) return(NaN) # All reporters matching criterion
	if(R == 0) return(0)   # No reporters matching criterion
	
	matchPws = sapply(reporters, function(x) domatch(x, pathway))
	
	n = sum(matchPws)			## Total number of genes in the pathway
	r = sum(matchPws[as.logical(set)])	## Number of genes that are in the subset and on the pathway
	
	RoverN = R / N
	num = r - n*RoverN
	den = sqrt(n * RoverN * (1 - RoverN) * (1 - (n - 1)/(N - 1)))
	z = num / den
	cat("\tzscore:", z, "\n")
	z
}
