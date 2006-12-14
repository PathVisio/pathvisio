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
################## PathwaySet class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("PathwaySet", contains = "list",
	representation(
		name = "character"
	),
	prototype(
		name = character()
	)
)

## Validity:
## 1: all elements in @pathways must be instance of class Pathway
setValidity("PathwaySet", function(object) {
	for(p in object)
		if(!isClass("Pathway", p)) return("list @pathways contains non-Pathway objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("PathwaySet", 
	function(name = "", pathways) {
		pws = new("PathwaySet", pathways, name = name)
		# Set rownames of pathways list
		names(pws) = sapply(pws, function(x) name(x))
		validObject(pws, test=TRUE)
		pws
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "PathwaySet", function(x) x@name)

## Setters ##)
createReplaceMethod("name", "PathwaySet", function(x, value) {
	x@name = value
	x
})

## Other ##
createMethod("asEnsembl", "PathwaySet", function(x, ...) {
	egenes = vector()
	for(pw in x) {
		egenes = append(egenes, asEnsembl(pw))
	}
	unique(egenes)
})

## Generic and Primitive implementations ##
setMethod("print", "PathwaySet", function(x, ...) {
	cat("PathwaySet:", paste("\t@name:\n\t\t ", name(x)), "\t@pathways:", sep="\n")
	for(pw in x) cat("\t\t", name(pw), "\n")
})
