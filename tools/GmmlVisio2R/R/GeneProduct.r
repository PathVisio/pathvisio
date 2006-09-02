
source("util.r")

#######################################################
################## GeneProduct class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("GeneProduct", 
	representation(
		ids = 	"character",
		systems = "character"
	),
	prototype = list(
		ids = 	character(),
		systems = character()
	)
)

## Validity:
## 1: length(ids) == length(systems)
setValidity("GeneProduct", function(x) {
	chk = 	length(getIds(x)) == length(getSystems(x))
	if(chk) 	TRUE
	else		"Number of ids and systems does not match"
})

######################
#### Constructors ####
######################
setGeneric("GeneProduct", 
	function(ids, systems) {
		new("GeneProduct", ids = ids, systems = systems)
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("getIds", "GeneProduct", function(x) x@ids)
createMethod("getSystems", "GeneProduct", function(x) x@systems)

## Setters ##)
createReplaceMethod("setIds", "GeneProduct", function(x, value) {
	x@ids = value
	x
})
createReplaceMethod("setSystems", "GeneProduct", function(x, value) {
	x@systems = value
	x
})

## Generic and Primitive implementations ##
createMethod("as.matrix", "GeneProduct", function(x) {
	if(validObject(x)) {
		m = cbind(x@ids, x@systems)
		colnames(m) = c("Id", "System")
		rownames(m) = x@ids
		m
	} else stop(paste("object is not a valid", "GeneProduct"))
})

createMethod("print", "GeneProduct", function(x, ...) {
	print(as.matrix(x))
})

createMethod("==", c("GeneProduct", "GeneProduct"), function(e1, e2) {
	if(validObject(e1) && validObject(e2)) {
		for(i in getIds(e1)) for(j in getIds(e2)) {
			if(i == j) return(TRUE)
		}
		FALSE
	} else stop(paste("object is not a valid", "GeneProduct"))
})
