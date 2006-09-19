###########################################################
################## GeneProductData class ##################
###########################################################

##########################
#### Class Definition ####
##########################
setClass("GeneProductData",
	representation(
		geneProduct = "GeneProduct",
		data = "list",
		sets = "list"
	),
	prototype = list(
		data = list(),
		sets = list()
	)
)

## Validity:
## 1: length(ids) == length(systems)
setValidity("GeneProductData", function(object) {
	if(!isClass("GeneProduct", geneProduct))
		return("No object of class 'GeneProduct' for slot geneProduct")
	if((length(object@data) != 0 && length(object@sets) != 0) && (length(object@data) != length(object@sets)))
		return("length of 'data' and 'sets' does not match")
})

######################
#### Constructors ####
######################
GeneProductData <- function(geneProduct, data = list(), sets = list()) {
		new("GeneProductData", geneProduct = geneProduct, data = data, sets = sets)
}

#################
#### Methods ####
#################
## Getters ##
createMethod("getGeneProduct", "GeneProductData", function(x) x@geneProduct)
createMethod("getData", "GeneProductData", function(x) x@data)
createMethod("getSets", "GeneProductData", function(x) x@sets)

## Setters ##)
createReplaceMethod("setGeneProduct", "GeneProductData", function(x, value) {
	x@geneProduct = value
	x
})
createReplaceMethod("setData", "GeneProductData", function(x, value) {
	x@data = value
	x
})
createReplaceMethod("setSets", "GeneProductData", function(x, value) {
	x@sets = value
	x
})
