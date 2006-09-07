#######################################################
################## Pathway class ##################
#######################################################

##########################
#### Class Definition ####
##########################
setClass("Pathway", 
	representation(
		name = "character",
		fileName = "character",
		geneProducts = "list"
	),
	prototype = list(
		fileName = character(),
		geneProducts = list()
	)
)

## Validity:
## 1: all elements in @geneProducts must be instance of class GeneProduct
setValidity("Pathway", function(x) {
	for(gp in getGeneProducts(x))
		if(!isClass("GeneProduct", gp)) return("list @geneProduct contains non-GeneProduct objects")
	TRUE
})

######################
#### Constructors ####
######################
setGeneric("Pathway", 
	function(name, fileName = "", geneProducts = list()) {
		new("Pathway", name = name, fileName = fileName, geneProducts = geneProducts)
	}
)

#################
#### Methods ####
#################
## Getters ##
createMethod("getName", "Pathway", function(x) x@name)
createMethod("getFileName", "Pathway", function(x) x@fileName)
createMethod("getGeneProducts", "Pathway", function(x) x@geneProducts)

## Setters ##)
createReplaceMethod("setName", "Pathway", function(x, value) {
	x@name = value
	x
})
createReplaceMethod("setFileName", "Pathway", function(x, value) {
	x@fileName = value
	x
})
createReplaceMethod("setGeneProducts", "Pathway", function(x, value) {
	x@geneProducts = value
	if(!validObject(x)) stop("given GeneProducts list is not valid (check length and class of elements)")
	x
})

## Generic and Primitive implementations ##
setMethod("print", "Pathway", function(x, ...) {
	print(paste("Pathway with name: ",getName(x)));
})

## Other ##
## Check for every GeneProduct in the list gps whether it is 
## in the pathway or not
## Returns a logical vector of the same length as gps
createMethod("hasGeneProduct", c("Pathway", "GeneProduct"), function(x, gps, ...) {
	present = logical(length(gps))
	pgps = getGeneProducts(x)
	for(i in 1:length(gps))
		for(gp in pgps) {
			bool = gp == gps[[i]]
			if(gp == gps[[i]]) {
				present[i] = TRUE
				break()
			}
		}
	present
})
			
