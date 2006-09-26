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
	chk = 	length(ids(object)) == length(systems(object))
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
createMethod("ids", "GeneProduct", function(x) x@ids)
createMethod("systems", "GeneProduct", function(x) x@systems)

## Setters ##)
createReplaceMethod("ids", "GeneProduct", function(x, value) {
	x@ids = value
	x
})
createReplaceMethod("systems", "GeneProduct", function(x, value) {
	x@systems = value
	x
})

## Other ##
createMethod("name", "GeneProduct", function(x) {
	ids = ids(x)
	sys = systems(x)
	nm = character()
	for(i in 1:length(ids)) {
		if(length(nm) == 0) nm = paste(ids[i],sys[i],sep=":")
		else nm = paste(nm,":",ids[i],sys[i],sep=":")
	}
	nm
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
	cat(as.matrix(x))
})

createMethod("==", c("GeneProduct", "GeneProduct"), function(e1, e2) {
	if(validObject(e1) && validObject(e2)) {
		ids1 = ids(e1); ids2 = ids(e2)
		s1 = systems(e1); s2 = systems(e2)
		
		for(i in 1:length(ids1)) for(j in length(ids2)) {
			if(ids1[i] == ids2[i] && s1[i] == s2[i]) return(TRUE)
		}
		FALSE
	} else stop(paste("object is not a valid", "GeneProduct"))
})
