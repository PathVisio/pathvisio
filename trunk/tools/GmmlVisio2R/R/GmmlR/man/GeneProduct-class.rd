\name{GeneProduct-class}
\docType{class}
\alias{GeneProduct-class}
\alias{GeneProduct}
\alias{getIds}
\alias{setIds}
\alias{getSystems}
\alias{setSystems}
\alias{getIds,GeneProduct-method}
\alias{setIds,GeneProduct-method}
\alias{getSystems,GeneProduct-method}
\alias{setSystems,GeneProduct-method}
\alias{print,GeneProduct-method}
\alias{==,GeneProduct-method}
\alias{as.matrix,GeneProduct-method}
\title{Class "GeneProduct"}
\description{A class that represents a gene-product (which may have several cross-references to annotation databases} 
\section{Objects from the Class}{
Objects can be created by calls of the form \code{GeneProduct(ids = c("id1", "id2"), systems = c("s1", "s2"))}. 
}
\section{Slots}{
  \describe{
    \item{\code{ids}:}{Object of class \code{"character"} vector of reference ids that point to an annotation database}
    \item{\code{systems}:}{Object of class \code{"character"} vector of symbols specifying the annotation database for the ids (one of GenMAPP system-codes)}
  }
}
\section{Methods}{
  \describe{
    \item{getIds}{\code{signature(x = "GeneProduct")}: the get function for
      slot \code{ids}}
    \item{setIds}{\code{signature(x = "GeneProduct")}: the set function
      for slot \code{ids}}
    \item{getSystems}{\code{signature(x = "GeneProduct")}: the get function
      for slot \code{systems}}
    \item{setSystems}{\code{signature(x = "GeneProduct")}: the set function
      for slot \code{systems}}
    \item{print}{\code{signature(x = "GeneProduct")}: print the cross-references to the console in human-readable format}
    \item{as.matrix}{\code{signature(object = "GeneProduct")}: get a matrix containing all cross-references as rows and the id, system as columns}
    \item{==}{\code{signature(e1 = "GeneProduct", e2 = "GeneProduct)}: compare two objects of class \code{GeneProduct}, 
    match when at least one id/system combination matches}
  }
}
\author{Thomas Kelder (BiGCaT)}
\examples{
    gp1 = GeneProduct(ids = c("1234", "2345"), systems = c("L", "L"))
    gp2 = GeneProduct(ids = c("9876", "1234"), systems = c("L", "L"))
    gp1 == gp2
    print(gp1)
}
\keyword{classes} 
