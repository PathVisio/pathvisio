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
setValidity("GeneProduct", function(object) {
	chk = 	length(getIds(object)) == length(getSystems(object))
	if(chk) 	TRUE
	else		"Number of ids and systems does not match"
})

######################
#### Constructors ####
######################
GeneProduct <- function(ids, systems) {
		new("GeneProduct", ids = ids, systems = systems)
}

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

setMethod("print", "GeneProduct", function(x, ...) {
	print(as.matrix(x))
})

createMethod("==", c("GeneProduct", "GeneProduct"), function(e1, e2) {
	if(validObject(e1) && validObject(e2)) {
		ids1 = getIds(e1); ids2 = getIds(e2)
		s1 = getSystems(e1); s2 = getSystems(e2)
		
		for(i in 1:length(ids1)) for(j in length(ids2)) {
			if(ids1[i] == ids2[i] && s1[i] == s2[i]) return(TRUE)
		}
		FALSE
	} else stop(paste("object is not a valid", "GeneProduct"))
})
