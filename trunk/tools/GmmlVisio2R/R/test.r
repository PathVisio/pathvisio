###############################################################################
################## Test script for Pathway statistic classes ##################
###############################################################################
source("GeneProduct.r")
source("Pathway.r")
print("source pathwayset")
source("PathwaySet.r")
source("DataSet.r")

## Create some genes
print("creating genes")
ids = list(
	c("ENSG0001", "961", "P0032"),
	"ENSG0002",
	"832")
systems =  list(
	c("En", "L", "S"),
	"En",
	"L")
labels = list(
	c("MAPK1", "MAPK1_LL", "MAPK1_SP"),
	"TNFa",
	"P53")

genes = list()
for(i in 1:length(ids)) genes[[i]] = GeneProduct(ids[[i]], systems[[i]], labels[[i]])
 
## Create some pathways
print("creating pathways")
geneProducts = list(
	list(genes[[1]], genes[[3]]),
	list(genes[[1]]))
names = c("Pathway1", "Pathway2")
pathways = list()
for(i in 1:length(names)) pathways[[i]] = Pathway(name = names[i], geneProducts = geneProducts[[i]])

## Create a pathway set
pathwaySet = PathwaySet(name = "testset", pathways = pathways)

## Create a dataset
print("creating dataset")
rnd = runif(30);
data = matrix(rnd, nrow = 10, ncol = 3)
colnames(data) = c("t1", "t2", "t3")
subsets = matrix(as.numeric(data < 0.5), nrow = 10, ncol = 2)
colnames(subsets) = c("t1<0.5", "t2<0.5")
gpfoo = GeneProduct("foo", "foo", "foo")
geneProducts = list()
for(i in 1:10) geneProducts[[i]] = gpfoo
geneProducts[[1]] = genes[[1]]
geneProducts[[2]] = genes[[2]]
geneProducts[[3]] = genes[[3]]
dataSet = DataSet(name = "testdata", geneProducts = geneProducts, data = data, subsets = subsets)


