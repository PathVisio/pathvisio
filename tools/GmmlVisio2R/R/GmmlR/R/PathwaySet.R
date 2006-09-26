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
	for(p in pathways(x))
		if(!isClass("Pathway", p)) return("list @pathways contains non-Pathway objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("PathwaySet", 
	function(name = "", pathways) {
		pws = new("PathwaySet", name = name, pathways = pathways)
		# Set rownames of pathways list
		names(pathways(pws)) = sapply(pathways(pws), function(x) name(x))
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("name", "PathwaySet", function(x) x@name)
createMethod("pathways", "PathwaySet", function(x) x@pathways)

## Setters ##)
createReplaceMethod("name", "PathwaySet", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("pathways", "PathwaySet", function(x, value) {
	##Check if argument is valid
	if(!is.list(value)) stop("value of @pathways must be a list")
	for(p in value)
		if(!isClass("Pathway", p) || !validObject(p))
			stop("One or more object in the list are not a valid object of class 'Pathway'")
	x@pathways = value
	x
})

## Generic and Primitive implementations ##
createMethod("print", "PathwaySet", function(x, ...) {
	cat("PathwaySet:", paste("\t@name:\n\t\t ", name(x)), "\t@pathways:", sep="\n")
	for(pw in pathways(x)) cat("\t\t", name(pw), "\n")
})

createMethod("as.list", "PathwaySet", function(x) pathways(x))
