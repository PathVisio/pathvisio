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

## Calculates the z-score and p-values (via permutation like in GenMAPP)
## for the given pathways and reporters, tested against the
## given subset
permuteP = function(pathway, reporters, set) {
	# Calculate z-score for every pathway
	zscore = function(pw) zscore(pw, reporters, set)
	
	# Calculate non-parametric statistic based on 2000 permutations
	# N and R are constant
	N = length(reporters)
	R = sum(as.logical(set))
		
	nperm = 2000;
	for(np in nperm) {
		# Re-assign genes randomly -> from which set of genes? total reporters? reporters mapping to a pathway? reporters matching criterion?
		# Also vary size? --> NO
		
	}
}
