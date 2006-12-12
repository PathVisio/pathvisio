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
#######################################################
################## ResultSet class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("ResultSet", contains = "matrix",
	representation(
		name = "character",
		globals = "matrix"
	),
	prototype(
		matrix(),
		name = character(),
		globals = matrix()
	)
)

## Validity:
setValidity("ResultSet", function(object) {
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("ResultSet", 
	function(name, pathwaySet, globals, stats) {
	if(length(pathwaySet) != nrow(stats)) 
		error("Number of pathways and number of rows in 'stats' matrix don't match")
	results = sapply(pathwaySet, function(x) c(name(x), fileName(x)))
	results = t(results)
	colnames(results) = c("pathway", "fileName")
	data = cbind(results, stats)
	new("ResultSet", data, name = name, globals = globals)
})

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "ResultSet", function(x, ...) x@name)
createMethod("globals", "ResultSet", function(x, ...) x@globals)

## Other ##
createMethod("fileNames", "ResultSet", function(x, ...) {
	try(x[,"fileName"])
})

createMethod("pathways", "ResultSet", function(x, ...) {
	try(x[,"pathway"])
})
