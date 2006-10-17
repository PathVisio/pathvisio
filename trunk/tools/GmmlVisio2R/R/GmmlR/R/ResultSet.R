#######################################################
################## ResultSet class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("ResultSet", contains = "matrix",
	representation(
		name = "character"
	),
	prototype(
		matrix(),
		name = character()
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
	function(name, pathwaySet, stats) {
	if(length(pathwaySet) != nrow(stats)) 
		error("Number of pathways and number of rows in 'stats' matrix don't match")
	results = sapply(pathwaySet, function(x) c(name(x), fileName(x)))
	results = t(results)
	colnames(results) = c("pathway", "fileName")
	data = cbind(results, stats)
	new("ResultSet", data, name = name)
})

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "ResultSet", function(x, ...) x@name)

## Other ##
createMethod("fileNames", "ResultSet", function(x, ...) {
	try(x[,"fileName"])
})

createMethod("pathways", "ResultSet", function(x, ...) {
	try(x[,"pathway"])
})
