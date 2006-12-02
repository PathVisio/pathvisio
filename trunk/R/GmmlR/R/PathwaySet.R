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

## Generic and Primitive implementations ##
setMethod("print", "PathwaySet", function(x, ...) {
	cat("PathwaySet:", paste("\t@name:\n\t\t ", name(x)), "\t@pathways:", sep="\n")
	for(pw in x) cat("\t\t", name(pw), "\n")
})
