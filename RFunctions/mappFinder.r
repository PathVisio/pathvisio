######################################################################
################## Statistics similar to MappFinder ##################
######################################################################
require(GmmlR)

.zscore_impl = function(pathwaySet, dataSet, set) {
	if(class(pathwaySet) != "PathwaySet")
		for(pw in pathwaySet) if(class(pw) != "Pathway") 
			stop("pathwaySet list contains non-Pathway elements")	
	if(class(dataSet) != "DataSet") 
		stop("dataSet argument is not of class 'DataSet'")
	if(length(set) != nrow(dataSet)) 
		stop("set argument should be a logical vector with same length as number of reporters in dataset")

	reporters = reporters(dataSet)
	
	total = length(pathwaySet)
	current = 1
	environment(.calcZscore) = environment()
	zscores = as.matrix(sapply(pathwaySet, function(x) .calcZscore(x, reporters, set)))
	colnames(zscores) = "z-score"
	
	##Create a ResultSet to return
	name = paste("zscore",name(dataSet),sep="-")
	ResultSet(name = name, pathwaySet = pathwaySet, stats = zscores)
}

## Calculates the z-score for the given pathway where
## 'reporters' contains all measured GeneProducts, and 'set'
## specifies for every geneProduct whether it belongs to the
## set to test against or not (e.g. subset that passed a criterion)
.calcZscore = function(pathway, reporters, set) {	
	cat("Processing pathway '", name(pathway),"' ", " (", current, " of ", total, ")\n", sep="");
	assign("current", current + 1, envir=parent.env(environment()))
	
	
	N = length(reporters)			## Total number of genes measured
	R = sum(as.logical(set))		## Total number of genes belonging to the set
	
	if(R == N) return(NaN) # All reporters matching criterion
	if(R == 0) return(0)   # No reporters matching criterion
	
	matchPws = sapply(reporters, function(y) match(y, pathway))
	
	n = sum(matchPws)			## Total number of genes in the pathway
	r = sum(matchPws[as.logical(set)])	## Number of genes that are in the subset and on the pathway
	
	RoverN = R / N
	num = r - n*RoverN
	den = sqrt(n * RoverN * (1 - RoverN) * (1 - (n - 1)/(N - 1)))
	z = num / den
	cat("\tzscore:", z, "\n")
	z
}

zscore = GmmlFunction(.zscore_impl,	name = "Z-score", 
	description = "Calculate z-score for the given pathways where 'set' specifies for each reporter whether it meets a criterion or not", 
	arg_descr = c(	"The pathways to calculate the z-scores for (instance of class 'PathwaySet')",
			"The measured data (instance of class 'DataSet')",
			"A logical vector that specifies for each reporter whether it meets a criterion or not"),
	arg_class = c("PathwaySet", "DataSet", "logical")
)
