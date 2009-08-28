Here is a list of all tools subdirectories, with a description.

AtlastMapper
    maps data on pathways by connecting WikiPathways webservice with 
	Atlas webservice.
    See also http://atlas.wikipathways.org/

ComponentTest
    This was a test to see how well swing handles mouseover events, 
	to support the plan to make swing components out of all gpml objects.
    status: not in active use

convert
    Scripts to convert a batch of pathways automatically, and 
	even test roundtrip conversion of GPML<->GenMAPP.
    status: useful but outdated. Needs work.

cytoscape-gpml
    Cytoscape plugin to load GPML pathways as Cytoscape networks
    status: active. You need cytoscape libraries to compile this.

dailybuild
    used to contain continuous build script. Continous build script 
	has moved to http://svn.bigcat.unimaas.nl/buildsystem
    Now only contains a script to get a few code metrics 
	(file size, lines of code) and a script to fix license headers.
    status: in active use

debian
    attempt to create ubuntu / debian package for pathvisio
    status: work in progress

downloader
    webstart that downloads 
	Derby databases and places them in the correct folder. 
	Used on http://www.pathvisio.org/Download

get_defaults
    script that reads GPML.xsd and generates the attribute 
	table for PathwayElement.java
    Use this whenever GPML.xsd is modified.

gexview
    Attempt for a program to view imported microarray data as a heatmap.
    May not compile, don't worry about it.
    status: work in progress.

gpmldiff
    Scripts and testcases for generating / applying a "diff" of 
	two gpml files

KeggConverter
    converts Kegg to GPML

lucene-indexer
    program to maintain a cache of wikipathways pathways 
	and have it indexed

makeGdb
    program to make Derby databases based amongst others on HMDB
    status: I'll move this to the bridgedb repo, where it belongs

mappdiff
    script to compate two GenMAPP MAPP files, 
	used as a test-case for roundtrip conversion
    status: should be merged with the "convert" directory, 
	they belong together.

path2java
    program to convert an SVG path to a series of java Graphics2D calls

pathway_metabolizer
    script by andra to convert labels to metabolites 
	by guessing the right metabolite ID

pathwaywiki
    This script was used to generate the first version of WikiPathways!
    status: won't even work again. can I delete this?

perl
    perl library with some useful routines for dealing with GPML files. 
	Now also contains script to do pathway inference
    Pathway inference script should really be in a separate directory.
	various perl scripts for dealing with pathways,
	including 
		gpmlbuilder
			mappbuilder-like tool to make gpml 
			based on a list of genes
		webservice_example example script 
			that makes use of the wikipathways webservice

php_pathvisio
	gpml to svg converter in php, written by Andra

project2008
    project by a student group. This will be moved to a plugin
    status: work in progress.

ReactomeConverter
    converts reactome pathways to GPML. Doesnt work very well,
	but it takes layout info directly from Reactome, so
	it might still be useful in addition to
	BioPAX conversion.

superpathways
    Cytoscape plugin by Helen, merges multiple pathways from 
	wikipathways into a Cytoscape network

syscodeTable
    script to generate a html table from our syscode file
    status: will be moved to bridgedb or deleted

wikipathways-maintenance
    scripts for maintenance of pathways, using the webservice
    status: useful as example code

wikipathways-search
	this is the code for http://search.wikipathways.org
