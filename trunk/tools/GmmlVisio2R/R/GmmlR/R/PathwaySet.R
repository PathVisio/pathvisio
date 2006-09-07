#######################################################
################## PathwaySet class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("PathwaySet", 
	representation(
		name = "character",
		pathways = "list"
	),
	prototype = list(
		name = character(),
		pathways = list()
	)
)

## Validity:
## 1: all elements in @pathways must be instance of class Pathway
setValidity("PathwaySet", function(x) {
	for(p in getPathways(x))
		if(!isClass("Pathway", p)) return("list @pathways contains non-Pathway objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("PathwaySet", 
	function(name = "", pathways) {
		new("PathwaySet", name = name, pathways = pathways)
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("getName", "PathwaySet", function(x) x@name)
createMethod("getPathways", "PathwaySet", function(x) x@pathways)

## Setters ##)
createReplaceMethod("setName", "PathwaySet", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("setPathways", "PathwaySet", function(x, value) {
	##Check if argument is valid
	if(!is.list(value)) stop("value of @pathways must be a list")
	for(p in value)
		if(!isClass("Pathway", p) || !validObject(p))
			stop("One or more object in the list are not a valid object of class 'Pathway'")
	x@pathways = value
	x
})

## Generic and Primitive implementations ##
setMethod("print", "PathwaySet", function(x, ...) {
	print(paste("PathwaySet with name: ", getName(x)))
})
