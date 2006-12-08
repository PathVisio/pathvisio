# PathVisio,
# a tool for data visualization and analysis using Biological Pathways
# Copyright 2006-2007 BiGCaT Bioinformatics
#
# Licensed under the Apache License, Version 2.0 (the "License"); 
# you may not use this file except in compliance with the License. 
# You may obtain a copy of the License at 
# 
# http://www.apache.org/licenses/LICENSE-2.0 
#  
# Unless required by applicable law or agreed to in writing, software 
# distributed under the License is distributed on an "AS IS" BASIS, 
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
# See the License for the specific language governing permissions and 
# limitations under the License.
#
######################################################################
################## Statistics similar to MappFinder ##################
######################################################################
require(pathVisio)

.zscore_impl = function(pathwaySet, dataSet, sets) {
	#Check arguments
	if(class(pathwaySet) != "PathwaySet")
		for(pw in pathwaySet) if(class(pw) != "Pathway") 
			stop("pathwaySet list contains non-Pathway elements")	
	if(class(dataSet) != "DataSet") 
		stop("dataSet argument is not of class 'DataSet'")
		
	if(is.null(dim(sets))) sets = cbind(sets)
	if(nrow(sets) != nrow(dataSet)) 
		stop("set argument should have the same length (or number of rows) as number of reporters in dataset")

	setnames = colnames(sets)
	if(is.null(setnames)) colnames(sets) = .setNames(sets, name(dataSet))
	
	#Progress reporting variables
	reporters = reporters(dataSet)	
	total = length(pathwaySet)
	current = 1
	
	#Give .calcStats access to my objects
	environment(.calcStats) = environment()
	environment(.matchPathways) = environment()
	
	#Match reporters with pathways
	cat("Matching reporters with pathways\n")
	pwMatches = .matchPathways(pathwaySet, reporters)
	
	cat("Calculating z-scores\n")
	results = list()
	for(i in 1:ncol(sets)) {
		zscores = t(sapply(names(pathwaySet), function(pwname) {
			.calcStats(sets[,i], pwMatches[[pwname]], pathwaySet[[pwname]])
		}))
        colnames(zscores) = .statNames()
        
        print(zscores)
	
		##Create a ResultSet to return
		results[[i]] = ResultSet(name = colnames(sets)[i], pathwaySet = pathwaySet, stats = zscores)
	}
	if(length(results) == 1) return(results[[1]])
	else return(results)
}

## For every pathway match with reporters
## Returns:
## A named list (names = names(pathwaySet)) where every element is a logical vector (length = length(reporters))
## that specifies whether a reporter is present in the pathway (TRUE) or not (FALSE)
.matchPathways = function(pathwaySet, reporters) {
	matchResult = lapply(pathwaySet, function(pathway) {
		## Progress reporting
		cat("\tprocessing pathway '", name(pathway),"' ", " (", 
			current, " of ", total, ")\n", sep="");
		assign("current", current + 1, envir=parent.env(environment()))
		
		## Apply match between this pathway and all reporters
		matchReferences(reporters, pathway)
	})
	names(matchResult) = names(pathwaySet)
	matchResult			
}

.setNames = function(sets, dataSetName) {
	sapply(1:ncol(sets), function(x) paste("zscore",dataSetName,"criterion",x,sep="-"))
}

.calcStats = function(set, reporterMatch, pathway) {	
	N = length(reporters)			## Total number of genes measured
	R = sum(as.logical(set))		## Total number of genes belonging to the set
				
	n = sum(reporterMatch)			## Total number of measured(!) genes in the pathway
	r = sum(reporterMatch[as.logical(set)])	## Number of genes that are in the subset and on the pathway
    
    zscore = .z(N, R, n, r)

    P = length(pathway)     ## Total number of genes in the pathway
    
   c(P, n, r, zscore)
}

.statNames = function() { c("on pathway", "measured", "in set", "z-score") }

.z = function(N, R, n, r) {
    RoverN = R / N
	num = r - n*RoverN
	den = sqrt(n * RoverN * (1 - RoverN) * (1 - (n - 1)/(N - 1)))
    num/den
}

zscore = VisioFunction(.zscore_impl,	name = "Z-score", 
	description = "Calculate z-score for the given pathways where 'sets' are one or more vectors which specify for each reporter whether it meets a criterion or not", 
	arg_descr = c(	"The pathways to calculate the z-scores for (instance of class 'PathwaySet')",
			"The measured data (instance of class 'DataSet')",
			"A logical vector that specifies for each reporter whether it meets a criterion or not, or a matrix (where the columns is a criterion)"),
	arg_class = c("PathwaySet", "DataSet", "matrix")
)
