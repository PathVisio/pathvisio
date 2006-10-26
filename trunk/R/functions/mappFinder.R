######################################################################
################## Statistics similar to MappFinder ##################
######################################################################
require(GmmlR)

.zscore_impl = function(pathwaySet, dataSet, sets) {
	if(class(pathwaySet) != "PathwaySet")
		for(pw in pathwaySet) if(class(pw) != "Pathway") 
			stop("pathwaySet list contains non-Pathway elements")	
	if(class(dataSet) != "DataSet") 
		stop("dataSet argument is not of class 'DataSet'")
		
	if(is.null(dim(sets))) sets = cbind(sets)
	if(nrow(sets) != nrow(dataSet)) 
		stop("set argument should have the same length (or number of rows) as number of reporters in dataset")

	setnames = colnames(sets)
	if(is.null(setnames)) colnames(sets) = .genSetNames(sets, name(dataSet))
	
	#Progress reporting
	reporters = reporters(dataSet)	
	total = length(pathwaySet)
	current = 1
	
	#Give .calcZscore access to my objects
	environment(.calcZscore) = environment()
	environment(.matchPathways) = environment()
	
	#Match pathways
	cat("Matching reporters with pathways\n")
	pwMatches = .matchPathways(pathwaySet, reporters)
	
	cat("Calculating z-scores\n")
	results = list()
	for(i in 1:ncol(sets)) {
		zscores = as.matrix(sapply(pwMatches, function(x) .calcZscore(x, sets[,i])))
		colnames(zscores) = "z-score"
	
		##Create a ResultSet to return
		results[[i]] = ResultSet(name = colnames(sets)[i], pathwaySet = pathwaySet, stats = zscores)
	}
	if(length(results) == 1) return(results[[1]])
	else return(results)
}

.matchPathways = function(pathwaySet, reporters) {
	matchResult = lapply(pathwaySet, function(pathway) {
		cat("\tprocessing pathway '", name(pathway),"' ", " (", 
			current, " of ", total, ")\n", sep="");
		assign("current", current + 1, envir=parent.env(environment()))
		sapply(reporters, function(y) match(y, pathway))
	})
	
	matchResult			
}

.genSetNames = function(sets, dataSetName) {
	sapply(1:ncol(sets), function(x) paste("zscore",dataSetName,"criterion",x,sep="-"))
}

.calcZscore = function(reporterMatch, set) {	
	N = length(reporterMatch)		## Total number of genes measured
	R = sum(as.logical(set))		## Total number of genes belonging to the set
	
	if(R == N) return(NaN) # All reporters matching criterion
	if(R == 0) return(0)   # No reporters matching criterion
		
	n = sum(reporterMatch)			## Total number of genes in the pathway
	r = sum(reporterMatch[as.logical(set)])	## Number of genes that are in the subset and on the pathway
	
	RoverN = R / N
	num = r - n*RoverN
	den = sqrt(n * RoverN * (1 - RoverN) * (1 - (n - 1)/(N - 1)))
	num / den
}

zscore = GmmlFunction(.zscore_impl,	name = "Z-score", 
	description = "Calculate z-score for the given pathways where 'sets' are one or more vectors which specify for each reporter whether it meets a criterion or not", 
	arg_descr = c(	"The pathways to calculate the z-scores for (instance of class 'PathwaySet')",
			"The measured data (instance of class 'DataSet')",
			"A logical vector that specifies for each reporter whether it meets a criterion or not, or a matrix (where the columns is a criterion)"),
	arg_class = c("PathwaySet", "DataSet", "matrix")
)
