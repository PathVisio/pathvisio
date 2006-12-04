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
################## VisioFunction class #################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("VisioFunction", contains = "function",
	representation(
		name = "character",
		description = "character",
		arg_descr = "character",
		arg_class = "character"),
	prototype(
		name = character(),
		description = character(),
		arg_descr = character(),
		arg_class = character())
)

## Validity:
setValidity("GeneProduct", function(object) {
	TRUE
})

######################
#### Constructors ####
######################
VisioFunction = function(fn, name = character(), description = character(), 
		arg_descr = character(length(formals(fn))), arg_class = character(length(formals(fn)))) {
	new("VisioFunction", fn, name=name, description=description, 
		arg_descr=arg_descr, arg_class=arg_class)
}

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "VisioFunction", function(x) x@name)
createMethod("description", "VisioFunction", function(x) x@description)
createMethod("arg_descr", "VisioFunction", function(x) x@arg_descr)
createMethod("arg_class", "VisioFunction", function(x) x@arg_class)

## Other ##
createMethod("getArgs", "VisioFunction", function(x) {
	names(formals(x))
})

createMethod("getDefaults", "VisioFunction", function(x) {
	as.character(formals(x))
})
