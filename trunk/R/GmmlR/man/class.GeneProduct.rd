\name{GeneProduct-class}
\docType{class}
\alias{GeneProduct-class}
\alias{class:GeneProduct}
\alias{GeneProduct}
\alias{name,GeneProduct-method}
\alias{name<-,GeneProduct-method}
\alias{addReference<-,GeneProduct-method}
\alias{==,GeneProduct-method}
\title{Class "GeneProduct"}
\description{A class that represents a gene-product (which may have several cross-references to annotation databases} 

\section{Extends}{
   Directly extends class \code{\link{matrix}}.
}

\section{Creating objects}{
Objects can be created by calls of the form 
\code{GeneProduct(refs)} where \code{refs} is a \code{\link{matrix}} with ids in the first column an database codes in the second
}

\section{Methods}{
\describe{
	\item{addReference}{ 
	\code{signature(e1= "GeneProduct", e2 = "character")}: add a new reference to this \code{GeneProduct}. \code{e2} can be a vector of the form \code{c(id, code)} or a string of the form \code{"id:code"}\cr
	\code{signature(e1= "GeneProduct", e2 = "matrix")}: add a new reference to this \code{GeneProduct}. \code{e2} has to be a \code{\link{matrix}} with the ids in the first column and database codes in the second
	}}
}

\author{Thomas Kelder (BiGCaT)}
\examples{
    gp1 = GeneProduct(cbind(c("1234", "2345"), c("L", "L")))
    gp2 = GeneProduct(cbind(c("9876", "1234"), c("U", "L")))
    gp1 == gp2
    gp1 == "L:2345"
    gp1[,'id']
    gp1[,'code']
    addReference(gp1) = c("1234_at","X")
    addReference(gp1) = "En:ENSG0001"

}
\keyword{methods}
\keyword{classes} 
